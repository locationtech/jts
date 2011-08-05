package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.ui.ColorUtil;
import com.vividsolutions.jtstest.testbuilder.ui.render.GeometryPainter;

/**
 * Shows polygon structure (shells versus holes)
 * by symbolizing the rings differently.
 * 
 * @author Martin Davis
 *
 */
public class PolygonStructureStyle 
extends LineStringStyle
{
  private Color color = Color.BLACK;

  public PolygonStructureStyle(Color color) {
    this.color = color;
  }

  protected void paintLineString(LineString lineString,
      int lineType,
      Viewport viewport, Graphics2D gr)
  throws Exception
  {
    Color dashClr = color.darker().darker(); //new Color(0, 0, 0);
    Graphics2D gr2 = (Graphics2D) gr.create();
    gr2.setColor(dashClr);
    
    Stroke dashStroke = new BasicStroke((float) 1.2,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {3, 6}, // Dash pattern
        0);                   // Dash phase 
    gr2.setStroke(dashStroke);

    if (lineType == POLY_HOLE) {
      Shape ringShape = GeometryPainter.getConverter(viewport).toShape(lineString);
      gr2.draw(ringShape);
    }
      //Color shellClr = ColorUtil.saturate(color, 0.9);
      //gr2.setColor(shellClr);
      //paintRing(polygon.getExteriorRing(), true, viewport, gr2);
  }


}
