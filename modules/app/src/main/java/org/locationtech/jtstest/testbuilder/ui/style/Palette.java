/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Color;

import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.util.HSBPalette;

public class Palette {

  public static final int TYPE_BASIC = 1;
  public static final int TYPE_VARY = 2;
  public static final int TYPE_RAINBOW = 3;
  public static final int TYPE_RAINBOW_RANDOM = 4;
  
  private static final float BRIGHT_RANGE = 0.1f;
  private static final float SAT_RANGE = 0.2f;
  
  public static HSBPalette customPalette(int paletteType, Color clrBase, int numHues) {
    HSBPalette pal = null;
    float sat = ColorUtil.getSaturation(clrBase);
    float bright = ColorUtil.getBrightness(clrBase);
    if (TYPE_VARY == paletteType) {
      float hue = ColorUtil.getHue(clrBase);
      pal = new HSBPalette(5, hue, HSBPalette.HUE_WIDTH / 2,
          3, sat - SAT_RANGE/2, sat + SAT_RANGE/2,
          3, bright - BRIGHT_RANGE/2, bright + BRIGHT_RANGE/2
          );
    }
    else if (TYPE_RAINBOW == paletteType) {
      return HSBPalette.createRainbow(numHues, sat, bright);
    }
    else if (TYPE_RAINBOW_RANDOM == paletteType) {
      return HSBPalette.createRainbowIncremental(0.23f, sat, bright);
    }
    return pal;
  }

  public static Color paletteColor(int i, HSBPalette pal, Color clrBase) {
    int alpha = clrBase.getAlpha();
    Color clr = pal.color(i, alpha);
    return clr;
  }

}
