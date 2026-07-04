package io.github.joseetoon.osv.config;

import lombok.EqualsAndHashCode;
import io.github.joseetoon.osv.preset.data.ModelSettings;

import java.io.Serializable;

@EqualsAndHashCode
public class ModelCache implements Serializable {
    public final ModelSettings.Type modelType;
    public final double overlayScale;
    public final boolean shadeModifier;
    public final boolean denseOres;

    public ModelCache(
        final ModelSettings.Type modelType,
        final double overlayScale,
        final boolean shadeModifier,
        final boolean denseOres
    ) {
        this.modelType = modelType;
        this.overlayScale = overlayScale;
        this.shadeModifier = shadeModifier;
        this.denseOres = denseOres;
    }

    public static ModelCache capture() {
        return new ModelCache(Cfg.modelType(), Cfg.overlayScale(), Cfg.shadeModifier(), Cfg.denseOres());
    }
}
