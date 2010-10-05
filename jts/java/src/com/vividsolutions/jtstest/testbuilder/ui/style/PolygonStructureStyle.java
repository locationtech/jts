package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.ui.render.GeometryPainter;

public class PolygonStructureStyle 
  extends ComponentStyle
{
  private Color color = Color.BLACK;

  public PolygonStructureStyle(Color color) {
    this.color = new Color(255, 255, 255, 160);
    //this.color = color;
  }

  protected void paintComponent(Geometry geom, Viewport viewport, Graphics2D gr)
  throws Exception
  {
  	Graphics2D gr2 = (Graphics2D) gr.create();
  	gr2.setColor(color);
  	
    Stroke dashStroke = new BasicStroke(2,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {2, 10}, // Dash pattern
        0);                   // Dash phase 
    gr2.setStroke(dashStroke);

    if (geom instanceof Polygon) {
      Polygon polygon = (Polygon) geom;
      //paintRing(polygon.getExteriorRing(), true, viewport, gr2);
      
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      	paintRing(polygon.getInteriorRingN(i), false, viewport, gr2);
      }
      return;
    }
  }

  private void paintRing(LineString ring, boolean isShell, Viewport viewport, Graphics2D gr)
  {
  	//if (! isShell) return;
  	Shape ringShape = GeometryPainter.getConverter(viewport).toShape(ring);
  	gr.draw(ringShape);
  }

}
