package io.github.joseetoon.osv.io;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.apache.commons.io.FileUtils;
import io.github.joseetoon.genlib.io.FileIO;
import personthecat.fresult.Result;
import personthecat.fresult.Void;
import io.github.joseetoon.osv.util.Reference;

import javax.annotation.CheckReturnValue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

@Log4j2
@UtilityClass
public class ResourceHelper {

    private static final PackLocationInfo PACK_INFO = new PackLocationInfo(
        ModFolders.RESOURCE_DIR.getName(),
        Component.literal(ModFolders.RESOURCE_DIR.getName()),
        PackSource.BUILT_IN,
        Optional.empty()
    );

    public static final PackResources RESOURCES =
        new PathPackResources(PACK_INFO, ModFolders.RESOURCE_DIR.toPath());
    public static final PackSelectionConfig PACK_SELECTION = new PackSelectionConfig(
        true,
        Pack.Position.TOP,
        false
    );
    private static final String PACK_MCMETA_PATH = "assets/" + Reference.MOD_ID + "/" + PackResources.PACK_META;
    private static final String FALLBACK_PACK_MCMETA = """
        {
          "pack": {
            "pack_format": 15,
            "description": "Generated assets for OSV."
          }
        }
        """;

    static {
        // In case the resource pack gets accessed earlier than expected.
        final File target = file(PackResources.PACK_META);
        try {
            JarFiles.copyIfAbsent(PACK_MCMETA_PATH, target);
        } catch (final RuntimeException e) {
            // Fallback when runtime classpath misses the embedded asset copy.
            Result.suppress(() -> {
                FileIO.mkdirsOrThrow(target.getParentFile());
                Files.writeString(target.toPath(), FALLBACK_PACK_MCMETA, StandardCharsets.UTF_8);
            });
            log.warn("Could not copy {} from classpath; wrote fallback {}", PACK_MCMETA_PATH, target.getPath());
        }
    }

    /**
     * Writes a string of data at the relative location in the /resources directory.
     *
     * @param path The relative path where the data will be kept.
     * @param data The string contents of this file.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final String path, final String data) {
        return writeResource(new FileSpec(() -> new ByteArrayInputStream(data.getBytes()), path));
    }

    /**
     * Writes <b>any</b> stream of data at the relative location in the /resources directory.
     *
     * @param path The relative path where the data will be kept.
     * @param data The raw contents of this file.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final String path, final InputStream data) {
        return writeResource(new FileSpec(() -> data, path));
    }

    /**
     * Writes a single file to the /resources directory.
     *
     * @param spec A model containing the input stream and relative path.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final FileSpec spec) {
        final File file = new File(ModFolders.RESOURCE_DIR, spec.path);
        if (!(ModFolders.RESOURCE_DIR.exists() || ModFolders.RESOURCE_DIR.mkdirs())) {
            return Result.err(new IOException("Could not make directory: " + ModFolders.RESOURCE_DIR));
        }
        return Result.of(() -> FileUtils.copyInputStreamToFile(spec.is.get(), file)).ifErr(Result::WARN);
    }

    /**
     * Writes a series of input streams to the disk using their respective paths.
     *
     * <p>Note that if any file fails to serialize, the following files will be ignored.
     *
     * @param files Models containing data streams and resource locations.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResources(final Collection<FileSpec> files) {
        return Result.of(() -> {
            for (final FileSpec spec : files) {
                writeResource(spec).throwIfErr();
            }
        }).ifErr(Result::WARN);
    }

    /**
     * Variant of {@link #writeResources(Collection)} which operates from an array.
     *
     * @param files Models containing data streams and resource locations.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResources(final FileSpec... files) {
        return Result.of(() -> {
            for (final FileSpec spec : files) {
                writeResource(spec).throwIfErr();
            }
        }).ifErr(Result::WARN);
    }

    /**
     * Returns whether the /resources directory contains the given path <b>or</b>
     * the resource is loaded as an asset in memory.
     *
     * @param path The <b>relative</b> path to the resource.
     * @return Whether the resource exists.
     */
    @CheckReturnValue
    public static boolean hasResource(final String path) {
        return file(path).exists() || FileIO.resourceExists(path);
    }

    /**
     * Returns a resource at the given path. If the resource does not exist in the
     * /resources directory, it will be returned from the jar file.
     *
     * @param path The relative path to the expected resource.
     * @return The resource, or else {@link Optional#empty}.
     */
    @CheckReturnValue
    public static Optional<InputStream> getResource(final String path) {
        final File file = file(path);
        if (file.exists()) {
            return Result.<InputStream>suppress(() -> new FileInputStream(file)).get();
        }
        return FileIO.getResource(path);
    }

    /**
     * Returns a file in the /resources directory.
     *
     * @param path The relative path in the /resources folder.
     * @return An absolute file at this location.
     */
    @CheckReturnValue
    public static File file(final String path) {
        return new File(ModFolders.RESOURCE_DIR, path);
    }

    public static PackResources createPackResources(final PackLocationInfo info) {
        return new PathPackResources(info, ModFolders.RESOURCE_DIR.toPath());
    }
}
