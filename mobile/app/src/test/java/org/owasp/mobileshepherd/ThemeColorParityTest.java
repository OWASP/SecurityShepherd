package org.owasp.mobileshepherd;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Guards light/dark consistency: every colour that is used must either have a
 * values-night counterpart or be explicitly listed as identical in both modes,
 * and layouts/drawables must not hard-code hex colours.
 */
public class ThemeColorParityTest {

    private static final Path RES = Paths.get("src", "main", "res");
    private static final Path JAVA = Paths.get("src", "main", "java");

    private static final Pattern COLOR_DEF =
            Pattern.compile("<color\\s+name=\"([^\"]+)\"\\s*>([^<]*)</color>");
    private static final Pattern COLOR_REF = Pattern.compile("(?:@color/|R\\.color\\.)([A-Za-z0-9_]+)");
    private static final Pattern HEX_LITERAL = Pattern.compile("\"#[0-9A-Fa-f]{3,8}\"");

    /** Colours that intentionally look the same in light and dark mode. */
    private static final Set<String> SAME_IN_BOTH_MODES = new HashSet<>(Arrays.asList(
            "white", "black",
            "medal_gold", "medal_silver", "medal_bronze",
            "scoreboard_bar_purple",
            "primary_dark_blue", "shepherd_green",
            "status_bar_light", "status_bar_dark"));

    /** Resource files allowed to contain hex literals (artwork that is dark in both modes). */
    private static final Set<String> HEX_ALLOWED_FILES = new HashSet<>(Arrays.asList(
            "nav_header_background.xml", "nav_header_main.xml",
            "ic_launcher_background.xml", "ic_launcher_foreground.xml"));

    private static Set<String> definedColors(Path file) throws IOException {
        Set<String> names = new TreeSet<>();
        if (!Files.exists(file)) {
            return names;
        }
        Matcher m = COLOR_DEF.matcher(read(file));
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static List<Path> files(Path root, String suffix, String... excludedDirPrefixes)
            throws IOException {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(suffix))
                    .filter(p -> {
                        String dir = p.getParent().getFileName().toString();
                        for (String ex : excludedDirPrefixes) {
                            if (dir.startsWith(ex)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static Set<String> usedColors() throws IOException {
        Set<String> used = new TreeSet<>();
        for (Path p : files(RES, ".xml", "mipmap")) {
            if (p.getFileName().toString().equals("colors.xml")) {
                continue;
            }
            Matcher m = COLOR_REF.matcher(read(p));
            while (m.find()) {
                used.add(m.group(1));
            }
        }
        for (Path p : files(JAVA, ".java")) {
            Matcher m = COLOR_REF.matcher(read(p));
            while (m.find()) {
                used.add(m.group(1));
            }
        }
        return used;
    }

    @Test
    public void everyUsedColorHasNightCounterpartOrIsListedAsShared() throws IOException {
        Set<String> day = definedColors(RES.resolve("values/colors.xml"));
        Set<String> night = definedColors(RES.resolve("values-night/colors.xml"));

        Set<String> missing = new TreeSet<>();
        for (String name : usedColors()) {
            if (day.contains(name) && !night.contains(name) && !SAME_IN_BOTH_MODES.contains(name)) {
                missing.add(name);
            }
        }
        assertTrue("Used colours without a values-night override (add one, or list it in "
                + "SAME_IN_BOTH_MODES if it is meant to look identical): " + missing,
                missing.isEmpty());
    }

    @Test
    public void nightOverridesDoNotDefineUnknownColors() throws IOException {
        Set<String> day = definedColors(RES.resolve("values/colors.xml"));
        Set<String> night = definedColors(RES.resolve("values-night/colors.xml"));
        Set<String> used = usedColors();

        Set<String> nightOnlyAndUnused = new TreeSet<>();
        for (String name : night) {
            if (!day.contains(name) && !used.contains(name)) {
                nightOnlyAndUnused.add(name);
            }
        }
        assertTrue("values-night colours with no light counterpart and no usage: "
                + nightOnlyAndUnused, nightOnlyAndUnused.isEmpty());
    }

    @Test
    public void layoutsAndDrawablesDoNotHardcodeHexColors() throws IOException {
        Set<String> offenders = new TreeSet<>();
        for (Path p : files(RES, ".xml", "values", "mipmap", "xml", "navigation", "menu")) {
            String name = p.getFileName().toString();
            if (HEX_ALLOWED_FILES.contains(name)) {
                continue;
            }
            String dir = p.getParent().getFileName().toString();
            if (!dir.startsWith("layout") && !dir.startsWith("drawable") && !dir.startsWith("color")) {
                continue;
            }
            Matcher m = HEX_LITERAL.matcher(read(p));
            if (m.find()) {
                offenders.add(dir + "/" + name + " -> " + m.group());
            }
        }
        assertTrue("Hard-coded hex colours do not follow the light/dark theme; use a "
                + "@color/ resource or theme attribute instead: " + offenders,
                offenders.isEmpty());
    }
}
