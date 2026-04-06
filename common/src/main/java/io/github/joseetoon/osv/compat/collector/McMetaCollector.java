package io.github.joseetoon.osv.compat.collector;

import io.github.joseetoon.osv.preset.OrePreset;
import org.hjson.JsonObject;

public interface McMetaCollector {
    void collect(final OrePreset cfg, final JsonObject output, final JsonObject original);
}
