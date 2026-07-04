package io.github.joseetoon.genlib;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeMixinSanityTest {

    private static final String CONFIG = "catlib.forge.mixins.json";
    private static final String MIXIN_PACKAGE = "io.github.joseetoon.genlib.mixin";

    @Test
    void declaredMixinsMatchAnnotatedMixinClasses() throws Exception {
        final MixinConfig config = readMixinConfig(CONFIG);
        final Set<String> sourceMixins = findSourceMixinClasses(moduleDir(), config.packageName());
        assertEquals(config.mixins(), sourceMixins, "NeoForge mixin config drift detected");
    }

    @Test
    void mappedRegistryRegisterSignatureStillMatchesMixin() {
        final Method register = findMethod(
            MappedRegistry.class,
            "register",
            new Class<?>[]{ResourceKey.class, Object.class, RegistrationInfo.class}
        );
        assertNotNull(register, "MappedRegistry.register(ResourceKey,Object,RegistrationInfo) no longer exists");
    }

    private static MixinConfig readMixinConfig(final String resource) throws IOException {
        final String json = readResource(resource);
        final Matcher packageMatcher = Pattern.compile("\"package\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(packageMatcher.find(), "Missing package declaration in " + resource);
        final Pattern sectionPattern = Pattern.compile("\"(?:mixins|client|server)\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        final Pattern entryPattern = Pattern.compile("\"([^\"]+)\"");
        final Set<String> names = new LinkedHashSet<>();
        final Matcher sectionMatcher = sectionPattern.matcher(json);
        while (sectionMatcher.find()) {
            final Matcher entryMatcher = entryPattern.matcher(sectionMatcher.group(1));
            while (entryMatcher.find()) {
                names.add(entryMatcher.group(1));
            }
        }
        return new MixinConfig(packageMatcher.group(1), names);
    }

    private static String readResource(final String resource) throws IOException {
        try (InputStream in = NeoForgeMixinSanityTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Missing test resource: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Set<String> findSourceMixinClasses(final Path moduleDir, final String packageName) throws IOException {
        final Path packageDir = moduleDir.resolve("src/main/java").resolve(packageName.replace('.', '/'));
        assertTrue(Files.isDirectory(packageDir), "Missing source package dir: " + packageDir);
        final Set<String> classes = new LinkedHashSet<>();
        for (final Path path : Files.list(packageDir).toList()) {
            if (!path.getFileName().toString().endsWith(".java")) {
                continue;
            }
            final String content = Files.readString(path);
            if (content.contains("@Mixin(")) {
                classes.add(path.getFileName().toString().replace(".java", ""));
            }
        }
        return classes;
    }

    private static Method findMethod(Class<?> type, final String name, final Class<?>[] params) {
        while (type != null) {
            try {
                return type.getDeclaredMethod(name, params);
            } catch (final NoSuchMethodException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static Path moduleDir() {
        final Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(cwd.resolve("src/main/java"))) {
            return cwd;
        }
        final Path nested = cwd.resolve("neoforge");
        assertTrue(Files.isDirectory(nested.resolve("src/main/java")), "Cannot locate neoforge module dir from " + cwd);
        return nested;
    }

    private record MixinConfig(String packageName, Set<String> mixins) {
    }
}
