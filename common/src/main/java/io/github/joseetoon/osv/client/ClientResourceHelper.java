package io.github.joseetoon.osv.client;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import io.github.joseetoon.genlib.util.PathUtils;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.io.ResourceHelper;
import io.github.joseetoon.osv.util.RlUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ClientResourceHelper {

    private static final Set<String> MISSING_LOGGED = ConcurrentHashMap.newKeySet();
    private static final Set<String> MANAGER_FAIL_LOGGED = ConcurrentHashMap.newKeySet();

    public static boolean hasResource(final String path) {
        if (!Cfg.assetsFromRP()) {
            return ResourceHelper.hasResource(path);
        }
        return locateResource(path).map(is -> {
            closeQuietly(is);
            return true;
        }).orElse(false);
    }

    public static Optional<InputStream> locateResource(final String path) {
        if (!Cfg.assetsFromRP()) {
            return ResourceHelper.getResource(path);
        }
        final ResourceLocation id = PathUtils.getResourceLocation(path);
        final Optional<InputStream> fromClient = locateFromClient(id);
        if (fromClient.isPresent()) {
            return fromClient;
        }
        final Optional<InputStream> fallback = ResourceHelper.getResource(path);
        if (!fallback.isPresent() && !isVanilla(id)) {
            logMissingOnce(id);
        }
        return fallback;
    }

    private static Optional<InputStream> locateFromClient(final ResourceLocation id) {
        final Optional<InputStream> fromManager = locateFromResourceManager(id);
        if (fromManager.isPresent()) {
            return fromManager;
        }

        final Optional<PackRepository> repository = getReadyRepository();
        if (repository.isPresent()) {
            final Optional<InputStream> fromSelected = locateInPacks(
                getDescendingPackIterator(repository.get().getSelectedPacks().stream().map(Pack::open)), id);
            if (fromSelected.isPresent()) {
                return fromSelected;
            }
            final Optional<InputStream> fromAvailable = locateInPacks(
                getDescendingPackIterator(repository.get().getAvailablePacks().stream().map(Pack::open)), id);
            if (fromAvailable.isPresent()) {
                return fromAvailable;
            }
        }

        final Optional<InputStream> fromClasspath = locateFromClasspath(id);
        if (fromClasspath.isPresent()) {
            return fromClasspath;
        }
        return locateFromForgeModList(id);
    }

    private static Optional<InputStream> locateFromResourceManager(final ResourceLocation id) {
        try {
            final Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getResourceManager() == null) {
                return Optional.empty();
            }
            final Object resourceManager = mc.getResourceManager();
            final Method getter = findResourceGetter(resourceManager.getClass());
            if (getter == null) {
                return Optional.empty();
            }
            final Object result = getter.invoke(resourceManager, id);
            final Object resource = unwrapOptional(result);
            if (resource == null) {
                return Optional.empty();
            }
            final Method opener = findResourceOpener(resource.getClass());
            if (opener == null) {
                return Optional.empty();
            }
            final Object opened = opener.invoke(resource);
            if (opened instanceof InputStream) {
                return Optional.of((InputStream) opened);
            }
        } catch (final Exception e) {
            logManagerFailureOnce(id, e);
        }
        return Optional.empty();
    }

    private static Method findResourceGetter(final Class<?> cls) {
        for (final Method m : cls.getMethods()) {
            if (m.getParameterCount() != 1) continue;
            if (!ResourceLocation.class.equals(m.getParameterTypes()[0])) continue;
            if (!Optional.class.isAssignableFrom(m.getReturnType())) continue;
            return m;
        }
        return null;
    }

    private static Method findResourceOpener(final Class<?> cls) {
        for (final Method m : cls.getMethods()) {
            if (m.getParameterCount() != 0) continue;
            if (InputStream.class.isAssignableFrom(m.getReturnType())) {
                return m;
            }
        }
        return null;
    }

    private static InputStream openResource(final PackResources rp, final ResourceLocation id) throws java.io.IOException {
        final IoSupplier<InputStream> result = rp.getResource(PackType.CLIENT_RESOURCES, id);
        return result != null ? result.get() : null;
    }

    private static Optional<InputStream> locateInPacks(
            final Iterator<PackResources> packs, final ResourceLocation id) {
        while (packs.hasNext()) {
            final PackResources rp = packs.next();
            if (rp == null) continue;
            try {
                final InputStream stream = openResource(rp, id);
                if (stream != null) {
                    return Optional.of(stream);
                }
            } catch (final Exception ignored) {}
        }
        return Optional.empty();
    }

    private static Iterator<PackResources> getDescendingPackIterator(final Stream<PackResources> stream) {
        return stream.collect(Collectors.toCollection(LinkedList::new)).descendingIterator();
    }

    private static Optional<PackRepository> getReadyRepository() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return Optional.empty();
        }
        final PackRepository repository = mc.getResourcePackRepository();
        if (repository == null) {
            return Optional.empty();
        }
        if (repository.getAvailablePacks().isEmpty() || repository.getSelectedPacks().isEmpty()) {
            synchronized (repository) {
                repository.reload();
                if (mc.options != null) {
                    mc.options.loadSelectedResourcePacks(repository);
                }
            }
        }
        return Optional.of(repository);
    }

    private static Optional<InputStream> locateFromClasspath(final ResourceLocation id) {
        final String path = toClasspathPath(id);

        final ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            final InputStream is = context.getResourceAsStream(path);
            if (is != null) {
                return Optional.of(is);
            }
        }

        final ClassLoader own = ClientResourceHelper.class.getClassLoader();
        if (own != null) {
            final InputStream is = own.getResourceAsStream(path);
            if (is != null) {
                return Optional.of(is);
            }
        }

        final InputStream is = ClientResourceHelper.class.getResourceAsStream("/" + path);
        return Optional.ofNullable(is);
    }

    private static Optional<InputStream> locateFromForgeModList(final ResourceLocation id) {
        try {
            final Class<?> modListClass = Class.forName("net.minecraftforge.fml.ModList");
            final Object modList = modListClass.getMethod("get").invoke(null);
            final Object modFileInfoOpt = modListClass.getMethod("getModFileById", String.class)
                .invoke(modList, RlUtils.ns(id));
            final Object modFileInfo = unwrapOptional(modFileInfoOpt);
            if (modFileInfo == null) {
                return Optional.empty();
            }

            Object modFile = callNoArg(modFileInfo, "getFile");
            if (modFile == null) {
                final Object owningFile = callNoArg(modFileInfo, "getOwningFile");
                modFile = callNoArg(owningFile, "getFile");
            }
            if (modFile == null) {
                return Optional.empty();
            }

            final String path = toClasspathPath(id);
            Path located = findPath(modFile, path);
            if (located == null) {
                final Object secureJar = callNoArg(modFile, "getSecureJar");
                final Object provider = callNoArg(secureJar, "moduleDataProvider");
                located = findPath(provider, path);
            }
            if (located != null && Files.exists(located)) {
                return Optional.of(Files.newInputStream(located));
            }
        } catch (final ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (final Exception ignored) {}
        return Optional.empty();
    }

    private static Path findPath(final Object owner, final String path) {
        if (owner == null) {
            return null;
        }
        final Path direct = invokePathLookup(owner, "findResource", path);
        if (direct != null) {
            return direct;
        }
        return invokePathLookup(owner, "findFile", path);
    }

    private static Path invokePathLookup(final Object owner, final String methodName, final String path) {
        for (final Method m : owner.getClass().getMethods()) {
            if (!m.getName().equals(methodName)) continue;
            if (m.getParameterCount() != 1) continue;

            try {
                final Class<?> p = m.getParameterTypes()[0];
                final Object result;
                if (String.class.equals(p)) {
                    result = m.invoke(owner, path);
                } else if (p.isArray() && String.class.equals(p.getComponentType())) {
                    result = m.invoke(owner, (Object) new String[]{path});
                } else {
                    continue;
                }
                final Path resolved = unwrapPath(result);
                if (resolved != null) {
                    return resolved;
                }
            } catch (final Exception ignored) {}
        }
        return null;
    }

    private static Object callNoArg(final Object owner, final String methodName) {
        if (owner == null) {
            return null;
        }
        try {
            return owner.getClass().getMethod(methodName).invoke(owner);
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static Object unwrapOptional(final Object value) {
        if (value instanceof Optional) {
            return ((Optional<?>) value).orElse(null);
        }
        return value;
    }

    private static Path unwrapPath(final Object value) {
        final Object unwrapped = unwrapOptional(value);
        if (unwrapped instanceof Path) {
            return (Path) unwrapped;
        }
        if (unwrapped instanceof File) {
            return ((File) unwrapped).toPath();
        }
        if (unwrapped instanceof String) {
            return Paths.get((String) unwrapped);
        }
        return null;
    }

    private static String toClasspathPath(final ResourceLocation id) {
        return "assets/" + RlUtils.ns(id) + "/" + RlUtils.path(id);
    }

    private static boolean isVanilla(final ResourceLocation id) {
        final String ns = RlUtils.ns(id);
        return "minecraft".equals(ns) || "osv".equals(ns);
    }

    private static void logMissingOnce(final ResourceLocation id) {
        if (!MISSING_LOGGED.add(id.toString())) {
            return;
        }
        log.debug("Unable to resolve non-vanilla client resource: {}", id);
    }

    private static void logManagerFailureOnce(final ResourceLocation id, final Exception e) {
        if (isVanilla(id)) {
            return;
        }
        final String key = id + "|" + e.getClass().getName();
        if (MANAGER_FAIL_LOGGED.add(key)) {
            log.debug("Failed to resolve {} through active ResourceManager: {}", id, e.toString());
        }
    }

    private static void closeQuietly(final InputStream is) {
        try {
            is.close();
        } catch (final Exception ignored) {}
    }
}
