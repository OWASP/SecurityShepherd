"""Checks the documentation site for mistakes that `zensical build --strict` lets through (#1070).

Zensical's strict mode fails on links to pages or anchors that don't exist, but not on:
- a `nav` entry in zensical.toml whose file doesn't exist (the menu gets a dead link),
- a page in docs/ that isn't in `nav` (it's published, but nobody can find it),
- an image or other file that a page links to but that doesn't exist.

Run after the build, from the project root:
    uv run --locked --no-sync python .github/scripts/check-docs-site.py
"""

import fnmatch
import re
import sys
import tomllib
from pathlib import Path
from urllib.parse import unquote, urlsplit

config = tomllib.loads(Path("zensical.toml").read_text())["project"]
docs = Path(config.get("docs_dir", "docs"))
site = Path(config.get("site_dir", "site"))
# Absolute links in the built site start with the path of site_url, e.g. /SecurityShepherd/
site_path = urlsplit(config["site_url"]).path
excluded = [line.strip() for line in config.get("exclude_docs", "").splitlines() if line.strip()]
errors = []


def nav_files(entries):
    for entry in entries:
        for value in entry.values() if isinstance(entry, dict) else [entry]:
            if isinstance(value, list):
                yield from nav_files(value)
            elif not re.match(r"[a-z]+://", value):
                yield value


# 1. Every nav entry exists, and every page is in nav
in_nav = set(nav_files(config["nav"]))
for page in sorted(in_nav):
    if not (docs / page).is_file():
        errors.append(f"zensical.toml: nav entry {page} doesn't exist in {docs}/")
for page in sorted(docs.rglob("*.md")):
    name = page.relative_to(docs).as_posix()
    if name not in in_nav and not any(fnmatch.fnmatch(name, pattern) for pattern in excluded):
        errors.append(f"{docs}/{name} isn't in nav in zensical.toml, so it can't be reached")

# 2. Every local link and file reference in the built site resolves
if not site.is_dir():
    sys.exit(f"{site}/ doesn't exist: run `zensical build` first")
for html in sorted(site.rglob("*.html")):
    for url in re.findall(r'(?:href|src)="([^"]*)"', html.read_text()):
        if not url or re.match(r"[a-z][a-z0-9+.-]*:|//|#", url):
            continue  # external, or an anchor on the same page (strict mode checks those)
        path = unquote(urlsplit(url).path)
        if path.startswith("/"):
            if not path.startswith(site_path):
                errors.append(f"{html}: {url} is outside the site ({site_path})")
                continue
            target = site / path[len(site_path) :]
        else:
            target = html.parent / path
        if target.is_dir():
            target = target / "index.html"
        if not target.is_file():
            errors.append(f"{html}: {url} doesn't exist")

for error in dict.fromkeys(errors):
    print(f"ERROR: {error}")
if errors:
    sys.exit(1)
print(f"OK: {len(in_nav)} pages in nav, and every local link in {site}/ resolves")
