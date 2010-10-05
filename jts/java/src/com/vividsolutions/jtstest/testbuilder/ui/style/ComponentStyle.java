package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;

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
