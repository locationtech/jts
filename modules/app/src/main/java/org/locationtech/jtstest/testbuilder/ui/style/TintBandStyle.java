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

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.render.GeometryPainter;


/**
 * WIP
 * 
 * Idea: draw inside buffer instead of band - avoids need for different op.
 * 
 * @author mdavis
 *
 */
public class TintBandStyle implements Style
{
  private static final Color TINT_BAND_SHADE = new Color(255,255,255, 100);

  public TintBandStyle() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g2d)
  {
    if (! (geom instanceof Polygon)) 
      return;
    
    Geometry band = computeBand((Polygon) geom, 10);
    if (band == null)
      return;
    GeometryPainter.paint(band, viewport, g2d, null, TINT_BAND_SHADE);
  }
  
  private Geometry computeBand(Polygon poly, double dist) {
    try {
      Geometry insideBuffer = poly.buffer(-dist);
      return OverlayNGRobust.overlay(poly, insideBuffer, OverlayNG.DIFFERENCE);
    }
    catch (TopologyException ex) {
      return null;
    }
  }

}
