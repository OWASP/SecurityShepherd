"""Generates the level catalogue tables in docs/levels.md and docs/mobile-levels.md.

The levels come from the module INSERT statements in src/main/resources/database/coreSchema.sql,
and each topic's OWASP Top 10 category and CWE from .github/scripts/level-classification.toml.
Only the block between the BEGIN and END markers in each page is replaced, so the rest of the page
stays hand-written.

    uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py          # rewrite
    uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py --check  # CI

--check exits with status 1, and changes nothing, when a page isn't what the generator would write.
"""

import re
import sys
import tomllib
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "src/main/resources/database/coreSchema.sql"
CLASSIFICATION = ROOT / ".github/scripts/level-classification.toml"
PAGES = {"web": ROOT / "docs/levels.md", "mobile": ROOT / "docs/mobile-levels.md"}

BEGIN = "<!-- BEGIN GENERATED: level catalogue. Edit .github/scripts/level-classification.toml or coreSchema.sql, then run .github/scripts/generate-level-catalogue.py. -->"
END = "<!-- END GENERATED: level catalogue -->"

COLUMNS = (
    "moduleId", "moduleName", "moduleNameLangPointer", "moduleType", "moduleCategory",
    "moduleCategoryLangPointer", "moduleResult", "moduleHash", "moduleStatus", "incrementalRank",
    "scoreValue", "hardcodedKey", "isUnsafe",
)
INSERT = re.compile(
    r"^INSERT INTO modules \((?P<columns>[^)]*)\) VALUES \((?P<values>.*)\);\s*$", re.MULTILINE
)
VALUE = re.compile(r"'((?:[^'\\]|\\.|'')*)'|(-?\d+)")

# Tournament Mode's bands, from Getter.getTounnamentSectionFromRankNumber: a level is in the first
# band whose cap is above its incrementalRank.
BANDS = (
    (45, "Field Training"), (80, "Private"), (105, "Corporal"), (130, "Sergeant"),
    (145, "Lieutenant"), (175, "Major"), (None, "Admiral"),
)


def fail(message):
    sys.exit(f"generate-level-catalogue: {message}")


def read_levels():
    levels = []
    for match in INSERT.finditer(SCHEMA.read_text(encoding="utf-8")):
        columns = tuple(c.strip().strip("`") for c in match["columns"].split(","))
        if columns != COLUMNS:
            fail(f"unexpected columns in a modules INSERT: {columns}")
        values = [m[1] if m[1] is not None else m[2] for m in VALUE.finditer(match["values"])]
        if len(values) != len(COLUMNS):
            fail(f"couldn't parse a modules INSERT: {match.group(0)[:120]}")
        level = dict(zip(COLUMNS, values))
        level["incrementalRank"] = int(level["incrementalRank"])
        level["scoreValue"] = int(level["scoreValue"])
        level["isUnsafe"] = level["isUnsafe"] == "1"
        levels.append(level)
    if not levels:
        fail(f"no modules INSERT statements found in {SCHEMA}")
    return levels


def band(rank):
    return next(name for cap, name in BANDS if cap is None or rank < cap)


def slug(heading):
    """The anchor that both Zensical and GitHub give a heading."""
    return re.sub(r"[\s]+", "-", re.sub(r"[^\w\s-]", "", heading).strip().lower())


def classify(levels, classification):
    topics, overrides = classification["topics"], classification.get("levels", {})
    for name in overrides:
        if not any(level["moduleName"] == name for level in levels):
            fail(f"level-classification.toml has an entry for '{name}', which isn't a level")
    for level in levels:
        topic = topics.get(level["moduleCategory"])
        if topic is None:
            fail(f"topic '{level['moduleCategory']}' of '{level['moduleName']}' isn't in level-classification.toml")
        override = overrides.get(level["moduleName"], {})
        level["topic"] = topic["name"]
        level["top10"] = override.get("top10", topic["top10"])
        level["cwe"] = override.get("cwe", topic["cwe"])
        level["mobile"] = level["moduleCategory"].startswith("Mobile ")


def top10_cell(code, top10):
    entry = top10[code]
    return f"[{code}]({entry['url']})" if "url" in entry else code


def render(levels, top10, edition):
    """The overview by Top 10 category, then a table per topic, easiest topic first."""
    by_topic = defaultdict(list)
    for level in sorted(levels, key=lambda l: (l["incrementalRank"], l["moduleName"])):
        by_topic[level["topic"]].append(level)

    lines = [f"### By {edition}", "", "| Category | Topics | Levels |", "| --- | --- | --- |"]
    for code, entry in top10.items():
        in_category = [l for l in levels if l["top10"] == code]
        if not in_category:
            continue
        parts = []
        for topic, topic_levels in by_topic.items():
            matching = [l["moduleName"] for l in topic_levels if l["top10"] == code]
            others = [l["moduleName"] for l in topic_levels if l["top10"] != code]
            link = f"[{topic}](#{slug(topic)})"
            if matching and not others:
                parts.append(link)
            elif len(matching) > len(others):
                parts.append(f"{link} (all except {', '.join(others)})")
            elif matching:
                parts.append(f"{link} ({', '.join(matching)})")
        name = f"[{entry['name']}]({entry['url']})" if "url" in entry else entry["name"]
        lines.append(f"| {name} | {', '.join(parts)} | {len(in_category)} |")
    lines.append("")

    for topic, topic_levels in by_topic.items():
        lines += [f"### {topic}", "", "| Level | Kind | Points | Tournament band | Top 10 | CWE |",
                  "| --- | --- | --- | --- | --- | --- |"]
        for level in topic_levels:
            name = level["moduleName"] + (" (unsafe)" if level["isUnsafe"] else "")
            kind = level["moduleType"].capitalize()
            cwe = f"[CWE-{level['cwe']}](https://cwe.mitre.org/data/definitions/{level['cwe']}.html)"
            lines.append(f"| {name} | {kind} | {level['scoreValue']} | {band(level['incrementalRank'])} "
                         f"| {top10_cell(level['top10'], top10)} | {cwe} |")
        lines.append("")
    return "\n".join(lines)


def replace_block(page, block):
    text = page.read_text(encoding="utf-8")
    start, end = text.find(BEGIN), text.find(END)
    if start == -1 or end == -1 or end < start:
        fail(f"{page.relative_to(ROOT)} needs the BEGIN and END markers, in that order")
    return text[: start + len(BEGIN)] + "\n\n" + block + "\n" + text[end:]


def main():
    check = sys.argv[1:] == ["--check"]
    if sys.argv[1:] and not check:
        fail("usage: generate-level-catalogue.py [--check]")
    levels = read_levels()
    classification = tomllib.loads(CLASSIFICATION.read_text(encoding="utf-8"))
    classify(levels, classification)
    editions = {"web": "OWASP Top 10 2025", "mobile": "OWASP Mobile Top 10 2024"}
    stale = []
    for kind, page in PAGES.items():
        selected = [l for l in levels if l["mobile"] == (kind == "mobile")]
        new = replace_block(page, render(selected, classification["top10"][kind], editions[kind]))
        if new != page.read_text(encoding="utf-8"):
            stale.append(page.relative_to(ROOT))
            if not check:
                page.write_text(new, encoding="utf-8")
    if check and stale:
        print("These pages don't match coreSchema.sql and level-classification.toml:", file=sys.stderr)
        for page in stale:
            print(f"  {page}", file=sys.stderr)
        print("Run: uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py", file=sys.stderr)
        sys.exit(1)
    if not check:
        print("Updated: " + (", ".join(map(str, stale)) if stale else "nothing, already up to date"))


if __name__ == "__main__":
    main()
