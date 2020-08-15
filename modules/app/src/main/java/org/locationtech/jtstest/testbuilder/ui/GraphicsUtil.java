/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui;

import java.awt.Graphics2D;

public class GraphicsUtil {
  
  public static void drawStringAlignCenter(Graphics2D g2d, String s, int x, int y) {
    int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
    g2d.drawString(s, x - stringLen /2, y); 
  }
  
  /**
   * 
   * @param g2d
   * @param s
   * @param x
   * @param y
   * @param anchorx value between 0 and 1 indicating anchor position along X
   * @param anchory value between 0 and 1 indicating anchor position along Y
   */
  public static void drawStringAlign(Graphics2D g2d, String s, int x, int y, float anchorx, float anchory) {
    int width = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
    int height = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getHeight();
    g2d.drawString(s, x - anchorx*width, y + anchory*height); 
  }
  
  /**
   * 
   * @param g2d
   * @param s
   * @param x
   * @param y
   * @param anchorx value between 0 and 1 indicating anchor position along X
   * @param anchory value between 0 and 1 indicating anchor position along Y
   * @param offset offset distance from anchor point
   */
  public static void drawStringAlign(Graphics2D g2d, String s, int x, int y, float anchorx, float anchory, int offset) {
    int width = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
    int height = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getHeight();
    
    int dirx = 0;
    if (anchorx <= 0) dirx = 1;
    if (anchorx >= 1) dirx = -1;
    
    // Y directions are inverted due to graphics orientation
    int diry = 0;
    if (anchory <= 0) diry = -1;
    if (anchory >= 1) diry = 1;

    g2d.drawString(s, x - anchorx*width + dirx*offset, y + anchory*height + diry*offset); 
  }

}
