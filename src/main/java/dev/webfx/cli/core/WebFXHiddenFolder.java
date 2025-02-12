package dev.webfx.cli.core;

import dev.webfx.cli.util.splitfiles.SplitFiles;
import dev.webfx.lib.reusablestream.ReusableStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterators;

/**
 * @author Bruno Salmon
 */
public final class WebFXHiddenFolder {

    private static final Path WEBFX_HIDDEN_FOLDER = Path.of(System.getProperty("user.home"), ".webfx");

    public static Path getCliFolder() {
        return WEBFX_HIDDEN_FOLDER.resolve("cli");
    }

    public static Path getCliSubFolder(String name) {
        return getCliFolder().resolve(name);
    }

    public static Path getMavenWorkspace() {
        return getCliSubFolder("maven-workspace");
    }

    public static Path getGraalVmHome() {
        Path hiddenVmFolder = getCliSubFolder("graalvm");
        Path binPath = ReusableStream.create(() -> Files.exists(hiddenVmFolder) ? SplitFiles.uncheckedWalk(hiddenVmFolder) : Spliterators.emptySpliterator())
                .filter(path -> path.toFile().isDirectory() && "bin".equals(path.getFileName().toString()))
                .findFirst()
                .orElse(null);
        return binPath == null ? null : binPath.getParent();
    }
}
