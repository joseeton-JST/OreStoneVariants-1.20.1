package io.github.joseetoon.osv.client.texture;

import java.awt.*;

import static io.github.joseetoon.osv.client.texture.ImageUtils.getAverage;
import static io.github.joseetoon.osv.client.texture.ImageUtils.getAverageDistance;
import static io.github.joseetoon.osv.client.texture.ImageUtils.getMaxDistance;
import static io.github.joseetoon.osv.client.texture.ImageUtils.getMaxRelativeDistance;

public class OverlayData {
    final Color bgAvg;
    final double bgDist;
    final double maxDist;
    final double maxRel;
    final Color[][] bg;
    final Color[][] fg;

    OverlayData(final Color[][] bg, final Color[][] fg) {
        this.bgAvg = getAverage(bg);
        this.bgDist = getAverageDistance(bg);
        this.maxDist = getMaxDistance(bg, fg);
        this.maxRel = getMaxRelativeDistance(bg, fg);
        this.bg = bg;
        this.fg = fg;
    }
}
