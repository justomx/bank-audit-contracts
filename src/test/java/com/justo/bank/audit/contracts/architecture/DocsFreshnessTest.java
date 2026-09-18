package com.justo.bank.audit.contracts.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Docs are part of the agent harness: a stale path or link misdirects an agent, so
 * freshness is checked the same way code is.
 */
class DocsFreshnessTest {

    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[[^\\]]*]\\(([^)]*)\\)");
    private static final Pattern BACKTICK_TOKEN = Pattern.compile("`([^`]*)`");
    private static final List<String> REPO_PATH_PREFIXES = List.of(
            "src/", "docs/", "scripts/", "openspec/", ".github/", ".githooks/");
    private static final List<String> PLACEHOLDER_CHARS = List.of("<", ">", "*", "{", "}", "…", "...");

    @Test
    void every_relative_markdown_link_target_exists() {
        Path repoRoot = findRepoRoot();
        List<String> offenders = new ArrayList<>();
        for (Path file : scannedFiles(repoRoot)) {
            forEachUnfencedLine(file, (lineNo, line) -> {
                Matcher matcher = MARKDOWN_LINK.matcher(line);
                while (matcher.find()) {
                    String target = matcher.group(1).trim();
                    if (target.isEmpty() || isExternalOrAnchor(target)) {
                        continue;
                    }
                    String withoutFragment = stripFragment(target);
                    if (withoutFragment.isEmpty()) {
                        continue;
                    }
                    Path resolved = file.getParent().resolve(withoutFragment).normalize();
                    if (!Files.exists(resolved)) {
                        offenders.add(relative(repoRoot, file) + ":" + lineNo + " -> " + target);
                    }
                }
            });
        }
        assertTrue(offenders.isEmpty(),
                () -> "broken relative markdown links (file:line -> target): " + offenders);
    }

    @Test
    void every_backticked_repo_path_exists() {
        Path repoRoot = findRepoRoot();
        List<String> offenders = new ArrayList<>();
        for (Path file : scannedFiles(repoRoot)) {
            forEachUnfencedLine(file, (lineNo, line) -> {
                Matcher matcher = BACKTICK_TOKEN.matcher(line);
                while (matcher.find()) {
                    String token = matcher.group(1).trim();
                    if (!looksLikeRepoPath(token)) {
                        continue;
                    }
                    Path resolved = repoRoot.resolve(token).normalize();
                    if (!Files.exists(resolved)) {
                        offenders.add(relative(repoRoot, file) + ":" + lineNo + " -> " + token);
                    }
                }
            });
        }
        assertTrue(offenders.isEmpty(),
                () -> "backticked repo paths that do not exist (file:line -> target): " + offenders);
    }

    private interface LineConsumer {
        void accept(int lineNumber, String line);
    }

    /** Fenced code blocks (``` or ~~~) are illustrative examples, not live doc content. */
    private void forEachUnfencedLine(Path file, LineConsumer consumer) {
        List<String> lines = readLines(file);
        boolean inFence = false;
        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).stripLeading();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inFence = !inFence;
                continue;
            }
            if (inFence) {
                continue;
            }
            consumer.accept(i + 1, lines.get(i));
        }
    }

    private boolean looksLikeRepoPath(String token) {
        if (!token.contains("/") || token.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }
        for (String bad : PLACEHOLDER_CHARS) {
            if (token.contains(bad)) {
                return false;
            }
        }
        return REPO_PATH_PREFIXES.stream().anyMatch(token::startsWith);
    }

    private boolean isExternalOrAnchor(String target) {
        return target.startsWith("http:") || target.startsWith("https:")
                || target.startsWith("mailto:") || target.startsWith("#");
    }

    private String stripFragment(String target) {
        int hash = target.indexOf('#');
        return hash >= 0 ? target.substring(0, hash) : target;
    }

    private String relative(Path repoRoot, Path file) {
        return repoRoot.relativize(file).toString().replace('\\', '/');
    }

    private List<Path> scannedFiles(Path repoRoot) {
        List<Path> files = new ArrayList<>();
        for (String fixed : List.of("AGENTS.md", "ARCHITECTURE.md", "README.md", "CONTEXT.md",
                ".github/workflows/README.md")) {
            Path candidate = repoRoot.resolve(fixed);
            if (Files.isRegularFile(candidate)) {
                files.add(candidate);
            }
        }
        Path docsDir = repoRoot.resolve("docs");
        if (Files.isDirectory(docsDir)) {
            try (Stream<Path> walk = Files.walk(docsDir)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .forEach(files::add);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return files;
    }

    private List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Walks up from the working directory until a directory has both AGENTS.md and pom.xml. */
    private Path findRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path agents = dir.resolve("AGENTS.md");
            Path pom = dir.resolve("pom.xml");
            if (Files.isRegularFile(agents) && Files.isRegularFile(pom)) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Could not locate repo root (AGENTS.md + pom.xml)");
    }
}
