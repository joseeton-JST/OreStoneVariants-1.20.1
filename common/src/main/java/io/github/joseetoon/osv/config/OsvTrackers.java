package io.github.joseetoon.osv.config;

import io.github.joseetoon.genlib.versioning.ConfigTracker;
import io.github.joseetoon.osv.util.Reference;

public class OsvTrackers {
    public static final ConfigTracker<ModelCache> MODEL_CACHE =
        ConfigTracker.forMod(Reference.MOD).withCategory("model_cache")
            .scheduleSave(ConfigTracker.PersistOption.GAME_READY).track(new ModelCache());
}
