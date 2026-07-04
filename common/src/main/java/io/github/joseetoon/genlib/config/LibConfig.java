package io.github.joseetoon.genlib.config;

import io.github.joseetoon.genlib.event.error.Severity;
import personthecat.overwritevalidator.annotations.OverwriteTarget;

@OverwriteTarget
public class LibConfig {

    public static boolean enableGlobalLibCommands() {
        return true;
    }

    public static Severity errorLevel() {
        return Severity.ERROR;
    }

    public static boolean wrapText() {
        return true;
    }

    public static int displayLength() {
        return 35;
    }

    public static boolean debugStartup() {
        return false;
    }
}
