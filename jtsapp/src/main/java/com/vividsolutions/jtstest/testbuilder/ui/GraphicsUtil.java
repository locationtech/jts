package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.Graphics2D;

public class GraphicsUtil {
  
  public static void drawStringAlignCenter(Graphics2D g2d, String s, int x, int y) {
    int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
    g2d.drawString(s, x - stringLen /2, y); 

  }
}
