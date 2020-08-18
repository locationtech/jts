package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Color;

import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.util.HSBPalette;

public class Palette {

  public static final int TYPE_BASIC = 1;
  public static final int TYPE_VARY = 2;
  public static final int TYPE_RAINBOW = 3;
  public static final int TYPE_RAINBOW_RANDOM = 4;
  
  private static final HSBPalette PAL_RAINBOW_INCREMENTAL = HSBPalette.createRainbowIncremental(0.396f, 0.4f, 1);

  public static HSBPalette customPalette(int paletteType, Color clrBase, int numHues) {
    HSBPalette pal = null;
    if (TYPE_VARY == paletteType) {
      float hue = ColorUtil.getHue(clrBase);
      pal = new HSBPalette(5, hue, 0.1f,
          3, 0.3f, 0.7f,
          3, 0.8f, 0.9f
          );
    }
    else if (TYPE_RAINBOW == paletteType) {
      return HSBPalette.createRainbow(numHues, 0.4f, 1);
    }
    else if (TYPE_RAINBOW_RANDOM == paletteType) {
      return PAL_RAINBOW_INCREMENTAL;
    }
    return pal;
  }

  public static BasicStyle paletteFill(int i, HSBPalette pal, BasicStyle style) {
    Color clrBase = style.getFillColor();
    int alpha = clrBase.getAlpha();
    Color clr = pal.color(i, alpha);
    BasicStyle st = style.copy();
    st.setFillColor(clr);
    return st;
  }


}
