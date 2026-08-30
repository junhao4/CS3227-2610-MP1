package cs3227.moneymap.persistence;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves the stable directory beside the packaged application or a configured development base. */
public final class ApplicationDirectoryResolver {
    /** Optional JVM property used to configure a stable development or test base directory. */
    public static final String BASE_DIRECTORY_PROPERTY = "moneymap.baseDir";

    private ApplicationDirectoryResolver() {
    }

    /**
     * Resolves the current application base directory.
     *
     * @return configured development base or the directory containing the packaged JAR
     */
    public static Path resolve() {
        try {
            Path codeLocation = Path.of(ApplicationDirectoryResolver.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return resolve(codeLocation, System.getProperty(BASE_DIRECTORY_PROPERTY));
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Could not resolve the MoneyMap application directory.", exception);
        }
    }

    static Path resolve(Path codeLocation, String configuredDirectory) {
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            return Path.of(configuredDirectory).toAbsolutePath().normalize();
        }
        Path normalizedLocation = codeLocation.toAbsolutePath().normalize();
        if (normalizedLocation.getFileName() != null
                && normalizedLocation.getFileName().toString().toLowerCase().endsWith(".jar")) {
            return normalizedLocation.getParent();
        }
        for (Path candidate = normalizedLocation; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("build.gradle"))) {
                return candidate;
            }
        }
        return normalizedLocation;
    }
}
