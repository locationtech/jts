/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.hprtree;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.shape.fractal.HilbertCode;

public class HilbertEncoder {
  private int level;
  private double minx;
  private double miny;
  private double strideX;
  private double strideY;

  public HilbertEncoder(int level, Envelope extent) {
    this.level = level;
    int hside = (int) Math.pow(2, level) - 1;
    
    minx = extent.getMinX();
    double extentX = extent.getWidth();
    strideX = extentX / hside;
    
    miny = extent.getMinX();
    double extentY = extent.getHeight();
    strideY = extentY / hside;
  }

  public int encode(Envelope env) {
    double midx = env.getWidth()/2 + env.getMinX();
    int x = (int) ((midx - minx) / strideX);

    double midy = env.getHeight()/2 + env.getMinY();
    int y = (int) ((midy - miny) / strideY);
      
    return HilbertCode.encode(level, x, y);
  }

}
