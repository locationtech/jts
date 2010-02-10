package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import com.vividsolutions.jtstest.testbuilder.*;

public class GeometryPainter 
{
  public static void paint(Graphics2D g, Viewport viewport, Geometry geometry, Style style)
  throws Exception
  {
    if (geometry == null)
      return;

    // cull non-visible geometries
    if (! viewport.intersectsInModel(geometry.getEnvelopeInternal())) 
      return;

    if (geometry instanceof GeometryCollection) {
      paintGeometryCollection(g, viewport, (GeometryCollection) geometry, style);
      return;
    }
    
    style.paint(geometry, viewport, g);
  }

  private static void paintGeometryCollection(Graphics2D g, Viewport viewport, 
      GeometryCollection gc,
      Style style
      ) 
  throws Exception
  {
    /**
     * Render each element separately.
     * Otherwise it is not possible to render both filled and non-filled
     * (1D) elements correctly
     */
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      paint(g, viewport, gc.getGeometryN(i), style);
    }
  }


}
