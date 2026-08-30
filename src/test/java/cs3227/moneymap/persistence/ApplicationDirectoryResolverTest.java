package cs3227.moneymap.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationDirectoryResolverTest {
    @TempDir
    Path tempDirectory;

    @Test
    void resolve_configuredDevelopmentDirectory_usesConfiguredPath() {
        Path configured = tempDirectory.resolve("configured");

        assertEquals(configured.toAbsolutePath().normalize(),
                ApplicationDirectoryResolver.resolve(tempDirectory.resolve("classes"), configured.toString()));
    }

    @Test
    void resolve_packagedJar_usesJarParent() {
        Path jar = tempDirectory.resolve("MoneyMap.jar");

        assertEquals(tempDirectory.toAbsolutePath().normalize(),
                ApplicationDirectoryResolver.resolve(jar, null));
    }
}
