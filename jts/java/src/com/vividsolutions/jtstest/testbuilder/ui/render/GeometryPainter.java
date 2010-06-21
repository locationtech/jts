package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.ui.style.Style;

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

  /**
   * Paints a geometry onto a graphics context,
   * using a given Viewport.
   * 
   * @param geometry shape to paint
   * @param viewport
   * @param g the graphics context
   * @param lineColor line color (null if none)
   * @param fillColor fill color (null if none)
   */
  public static void paint(Geometry geometry, Viewport viewport, 
      Graphics2D g,
      Color lineColor, Color fillColor) 
  {
    ShapeWriter converter = new ShapeWriter(viewport);
    paint(geometry, viewport, converter, g, lineColor, fillColor);
  }
  
  public static void paint(Geometry geometry, Viewport viewport, ShapeWriter converter, Graphics2D g,
      Color lineColor, Color fillColor) 
  {
    if (geometry == null)
			return;

    if (geometry instanceof GeometryCollection) {
      paintGeometryCollection((GeometryCollection) geometry, viewport, converter, g,
          lineColor, fillColor);
      return;
    }

		Shape shape = converter.toShape(geometry);
    
    // Test for a polygonal shape and fill it if required
		if (!(shape instanceof GeneralPath) && fillColor != null) {
			g.setPaint(fillColor);
			g.fill(shape);
		}
		if (lineColor != null) {
		  g.setColor(lineColor);
		  try {
		    g.draw(shape);
		  } 
		  catch (Throwable ex) {
		    System.out.println(ex);
		    // eat it!
		  }
		}
	}

  private static void paintGeometryCollection(GeometryCollection gc,
      Viewport viewport, ShapeWriter converter,
      Graphics2D g, Color lineColor, Color fillColor) 
  {
    /**
     * Render each element separately.
     * Otherwise it is not possible to render both filled and non-filled
     * (1D) elements correctly
     */
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      paint(gc.getGeometryN(i), viewport, converter, g, lineColor, fillColor);
    }
  }


}
