/*
 * Copyright (c) 2017 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.util;

import java.awt.Color;

import org.locationtech.jts.math.MathUtil;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;

public class HSBPalette {
  
  public static final float HUE_WIDTH = 0.08333f;
  private static final float HUE_WRAP_MAX = 1 - HUE_WIDTH;

  public static HSBPalette createSpectrum(int numHue, float s, float b) {
    return new HSBPalette(numHue, 0, HUE_WRAP_MAX,
        1, s, s,
        1, b, b
        ); 
  }

  public static HSBPalette createSpectrumWrap(int numHue, float s, float b) {
    return new HSBPalette(numHue, 0, 1,
        1, s, s,
        1, b, b
        ); 
  }

  public static HSBPalette createSpectrumIncremental(float hInc, float s, float b) {
    HSBPalette pal = new HSBPalette(50, 0, 1,
        1, s, s,
        1, b, b
        ); 
    pal.setHueInc(hInc);
    return pal;
  }

  public static HSBPalette createSpectrumRange(int numHue, float hueRange, float h, float s, float b) {
    return new HSBPalette(numHue, h, hueRange,
        1, s, s,
        1, b, b
        ); 
  }
  
  private void setHueInc(float hInc) {
      this.hInc = hInc;
   }

  int numH = 5;
  int numS = 3;
  int numB = 3;
  
  float hInc = 0.03f;
  float sInc = 0.1f;
  float bInc = 0.1f;
  
  private int numEntries;
  private float sLo;
  private float bLo;
  private float hBase;
  private float hRange;
  
  private float h1;
  
  public HSBPalette(
    int numH, float hBase, float hRange,
    int numS, float sLo, float sHi,
    int numB, float bLo, float bHi) 
  {
    this.numH = numH;
    this.numS = numS;
    this.numB = numB;
    this.hBase = hBase;
    this.hRange = hRange;
    this.h1 = hBase;
    this.sLo = sLo;
    this.bLo = bLo;
    // TODO: make this mod 1
    this.hInc = (numH < 2) ? 0 : (hRange) / (numH - 1);
    this.sInc = (numS < 2) ? 0 : (sHi - sLo) / (numS - 1);
    this.bInc = (numB < 2) ? 0 : (bHi - bLo) / (numB - 1);
    numEntries = numH * numS * numB;
  }
  public int numEntries() { return numEntries; }
  
  public Color color(int index, int alpha) {
    int i = index % numEntries;
    int iH = i / (numS * numB);
    int iSB = i - iH * (numS * numB);
    int iS = iSB / numB;
    int iB = iSB - iS * numB;
    float h = (h1 + iH * hInc) % 1.0f;
    float s = (float) MathUtil.clamp(sLo + iS * sInc, 0, 1);
    float b = (float) MathUtil.clamp(bLo + iB * bInc, 0, 1);
    Color chsb = Color.getHSBColor(h, s, b);
    return ColorUtil.setAlpha(chsb, alpha);
  }
  
}
