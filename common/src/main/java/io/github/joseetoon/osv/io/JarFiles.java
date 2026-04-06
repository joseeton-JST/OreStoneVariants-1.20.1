package io.github.joseetoon.osv.io;

import lombok.extern.log4j.Log4j2;
import io.github.joseetoon.genlib.exception.ResourceException;
import io.github.joseetoon.genlib.io.FileIO;
import personthecat.fresult.Result;
import io.github.joseetoon.osv.config.DefaultOres;
import io.github.joseetoon.osv.config.DefaultStones;
import io.github.joseetoon.osv.util.Group;
import io.github.joseetoon.osv.util.Reference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.github.joseetoon.genlib.util.PathUtils.extension;
import static io.github.joseetoon.genlib.util.PathUtils.filename;
import static io.github.joseetoon.genlib.util.PathUtils.noExtension;
import static io.github.joseetoon.genlib.util.Shorthand.f;
import static io.github.joseetoon.osv.io.ModFolders.ORE_DIR;
import static io.github.joseetoon.osv.io.ModFolders.STONE_DIR;

@Log4j2
public class JarFiles {

    public static final String TUTORIAL = "TUTORIAL.xjs";
    public static final String REFERENCE = "REFERENCE.xjs";

    public static void copyPresets() {
        FileIO.mkdirsOrThrow(ORE_DIR, STONE_DIR);

        final List<String> ores = new ArrayList<>();
        for (final Group g : DefaultOres.LISTED) ores.addAll(g.filenames());
        for (final Group g : DefaultOres.UNLISTED) ores.addAll(g.filenames());
        ores.add(TUTORIAL);
        ores.add(REFERENCE);

        final List<String> stones = new ArrayList<>();
        for (final Group g : DefaultStones.LISTED) stones.addAll(g.filenames());
        for (final Group g : DefaultStones.UNLISTED) stones.addAll(g.filenames());
        stones.remove("minecraft/stone.xjs");
        stones.add(TUTORIAL);

        copyPresets(ORE_DIR, ores);
        copyPresets(STONE_DIR, stones);
    }

    public static void copyIfAbsent(final String from, final File to) {
        if (!FileIO.fileExists(to)) {
            copyFile(from, to, false);
        }
    }

    public static boolean isSpecialFile(final String name) {
        return TUTORIAL.equalsIgnoreCase(name) || REFERENCE.equalsIgnoreCase(name);
    }

    private static void copyPresets(final File dir, final Collection<String> filenames) {
        final List<String> existing = getAllPresets(dir);

        for (final String name : filenames) {
            if (isSpecialFile(name) || !existing.contains(noExtension(filename(name)))) {
                final String from = f("data/{}/{}/{}", Reference.MOD_ID, dir.getName(), name);
                final String to = f("{}/{}", dir.getPath(), name);

                log.info("copying from [{}] to [{}]", from, to);
                copyFile(from, new File(to), true);
            }
        }
    }

    private static List<String> getAllPresets(File dir) {
        final List<File> files = FileIO.listFilesRecursive(dir);
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> names = new ArrayList<>();
        for (final File f : files) {
            final String ext = extension(f).toLowerCase(Locale.ROOT);
            if ("xjs".equals(ext) || "hjson".equals(ext) || "json".equals(ext)) {
                names.add(noExtension(f));
            }
        }
        return names;
    }

    private static void copyFile(final String from, final File to, final boolean canBeGenerated) {
        FileIO.mkdirsOrThrow(to.getParentFile());
        final InputStream toCopy;
        try {
            toCopy = FileIO.getRequiredResource(from);
        } catch (final ResourceException e) {
            if (canBeGenerated) {
                log.info("Resource not found in jar will be generated: {}", from);
                return;
            }
            throw e;
        }
        Result.with(() -> new FileOutputStream(to), fos -> {
            FileIO.copyStream(toCopy, fos).throwIfErr();
        }).expect("Error copying {}", from);
    }
}
