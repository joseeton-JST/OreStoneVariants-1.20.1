package io.github.joseetoon.osv.client.texture;

import java.awt.*;

public interface OverlayModifier {
    Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay);
}
