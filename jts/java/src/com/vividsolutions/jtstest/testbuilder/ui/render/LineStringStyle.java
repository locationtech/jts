package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;

public abstract class LineStringStyle
  implements Style
{

  public LineStringStyle() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g) 
    throws Exception
  {
    // cull non-visible geometries
    if (! viewport.intersectsInModel(geom.getEnvelopeInternal())) 
      return;

    if (geom instanceof LineString) {
      LineString lineString = (LineString) geom;
      if (lineString.getNumPoints() < 2) {
        return;
      }
      paintLineString(lineString, viewport, g);
    }
    
    if (geom instanceof Point)
      return;
    if (geom instanceof MultiPoint)
      return;

    if (geom instanceof GeometryCollection) {
      GeometryCollection gc = (GeometryCollection) geom;
      for (int i = 0; i < gc.getNumGeometries(); i++) {
        paint(gc.getGeometryN(i), viewport, g);
      }
      return;
    }
    if (geom instanceof Polygon) {
      Polygon polygon = (Polygon) geom;
      paint(polygon.getExteriorRing(), viewport, g);
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
          paint(polygon.getInteriorRingN(i), viewport, g);
      }
      return;
    }
  }

  protected abstract void paintLineString(LineString lineString,
      Viewport viewport, Graphics2D graphics)
  throws Exception;


}
