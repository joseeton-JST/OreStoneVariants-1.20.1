package io.github.joseetoon.osv.compat;

import lombok.extern.log4j.Log4j2;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.exception.CompatOutOfDateException;
import io.github.joseetoon.osv.util.Reference;

@Log4j2
class CompatLoader {
    static void runChecked(final String mod, final Runnable f) {
        if (McUtils.isModLoaded(mod)) {
            try {
                f.run();
            } catch (final LinkageError e) {
                log.error("Compat module for mod '{}' is out of date", mod, e);
                LibErrorContext.error(Reference.MOD, new CompatOutOfDateException(mod, e));
            }
        }
    }
}
