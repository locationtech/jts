/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
}
