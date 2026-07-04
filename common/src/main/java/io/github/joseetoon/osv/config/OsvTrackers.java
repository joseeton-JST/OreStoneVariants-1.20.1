package io.github.joseetoon.osv.config;

import io.github.joseetoon.genlib.versioning.ConfigTracker;
import io.github.joseetoon.osv.util.Reference;

public class OsvTrackers {
    private static volatile ConfigTracker<ModelCache> modelCache;

    private OsvTrackers() {}

    public static ConfigTracker<ModelCache> modelCache() {
        ConfigTracker<ModelCache> tracker = modelCache;
        if (tracker == null) {
            synchronized (OsvTrackers.class) {
                tracker = modelCache;
                if (tracker == null) {
                    tracker = ConfigTracker.forMod(Reference.MOD).withCategory("model_cache")
                        .scheduleSave(ConfigTracker.PersistOption.GAME_READY).track(ModelCache.capture());
                    modelCache = tracker;
                }
            }
        }
        return tracker;
    }
}
