package io.github.joseetoon.osv;

import io.github.joseetoon.osv.mixin.BlockBehaviourInvoker;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonMixinSanityTest {

    private static final List<String> CONFIGS = List.of(
        "osv.mixins.json",
        "catlib.common.mixins.json"
    );

    @Test
    void declaredMixinsMatchAnnotatedMixinClasses() throws Exception {
        final Set<String> declared = new LinkedHashSet<>();
        final Set<String> sourceMixins = new LinkedHashSet<>();
        for (final String config : CONFIGS) {
            final MixinConfig mixinConfig = readMixinConfig(config);
            declared.addAll(mixinConfig.mixins());
            sourceMixins.addAll(findSourceMixinClasses(moduleDir(), mixinConfig.packageName()));
        }
        assertEquals(declared, sourceMixins, "Common mixin config drift detected");
    }

    @Test
    void blockBehaviourInvokerTargetsStillExist() {
        for (final Method method : BlockBehaviourInvoker.class.getDeclaredMethods()) {
            final String targetName = inferInvokerTargetName(method.getName());
            final Method targetMethod = findMethod(BlockBehaviour.class, targetName, method.getParameterTypes());
            assertNotNull(
                targetMethod,
                () -> "BlockBehaviourInvoker target missing: " + targetName + Arrays.toString(method.getParameterTypes())
            );
        }
    }

    @Test
    void worldGenerationContextConstructorStillMatchesMixin() {
        try {
            final var ctor = WorldGenerationContext.class.getDeclaredConstructor(ChunkGenerator.class, LevelHeightAccessor.class);
            assertNotNull(ctor);
        } catch (final NoSuchMethodException e) {
            throw new AssertionError("WorldGenerationContext constructor no longer matches mixin target", e);
        }
    }

    @Test
    void titleScreenInitMethodExists() {
        final Method init = findMethod(TitleScreen.class, "init", new Class<?>[0]);
        assertNotNull(init, "TitleScreen.init() no longer matches mixin target");
    }

    @Test
    void serverReadyPersistenceMovedIntoBootstrap() throws Exception {
        final String catLib = Files.readString(
            moduleDir().getParent().resolve("neoforge/src/main/java/io/github/joseetoon/genlib/CatLib.java")
        );
        assertTrue(catLib.contains("GameReadyEvent.SERVER.invoker().run();"));
        assertTrue(catLib.contains("LibErrorContext.outputServerErrors(true);"));
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
        try (InputStream in = CommonMixinSanityTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Missing test resource: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
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

    private static String inferInvokerTargetName(final String methodName) {
        if (methodName.startsWith("invoke")) {
            return decapitalize(methodName.substring(6));
        }
        if (methodName.startsWith("call")) {
            return decapitalize(methodName.substring(4));
        }
        return methodName;
    }

    private static String decapitalize(final String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static Path moduleDir() {
        final Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(cwd.resolve("src/main/java"))) {
            return cwd;
        }
        final Path nested = cwd.resolve("common");
        assertTrue(Files.isDirectory(nested.resolve("src/main/java")), "Cannot locate common module dir from " + cwd);
        return nested;
    }

    private record MixinConfig(String packageName, Set<String> mixins) {
    }
}
