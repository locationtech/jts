/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.ui.Viewport;

public abstract class ComponentStyle
  implements Style
{
  public void paint(Geometry geom, Viewport viewport, Graphics2D g) 
    throws Exception
  {
    // cull non-visible geometries
    if (! viewport.intersectsInModel(geom.getEnvelopeInternal())) 
      return;

    if (geom instanceof GeometryCollection) {
      GeometryCollection gc = (GeometryCollection) geom;
      for (int i = 0; i < gc.getNumGeometries(); i++) {
        paint(gc.getGeometryN(i), viewport, g);
      }
      return;
    }
    paintComponent(geom, viewport, g);
  }

  protected abstract void paintComponent(Geometry geom,
      Viewport viewport, Graphics2D graphics)
  throws Exception;


}
