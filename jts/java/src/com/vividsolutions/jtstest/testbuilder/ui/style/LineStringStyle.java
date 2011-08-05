package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;

public abstract class LineStringStyle
  implements Style
{
	public static final int LINE = 1;
	public static final int POLY_SHELL = 2;
	public static final int POLY_HOLE = 3;
	
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
      paintLineString(lineString, LINE, viewport, g);
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
      paint(polygon.getExteriorRing(), POLY_SHELL, viewport, g);
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
          paint(polygon.getInteriorRingN(i), POLY_HOLE, viewport, g);
      }
      return;
    }
  }

  public void paint(LineString line, int lineType, Viewport viewport, Graphics2D g) 
  throws Exception
  {
    // cull non-visible geometries
    if (! viewport.intersectsInModel(line.getEnvelopeInternal())) 
      return;
    
    paintLineString(line, lineType, viewport, g);
  }
  
  protected abstract void paintLineString(LineString lineString,
  		int lineType,
      Viewport viewport, Graphics2D graphics)
  throws Exception;


}
