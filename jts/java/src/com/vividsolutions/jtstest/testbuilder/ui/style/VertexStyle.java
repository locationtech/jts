package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.*;

public class VertexStyle  implements Style
{
  private static final int size = 4;
  private double sizeOver2 = size / 2d;;
  
  protected RectangularShape shape;
  private Color color;
  
  public VertexStyle(Color color) {
    this.color = color;
    shape = new Rectangle2D.Double();
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    g.setPaint(color);
    Coordinate[] coordinates = geom.getCoordinates();
    
    for (int i = 0; i < coordinates.length; i++) {
        if (! viewport.containsInModel(coordinates[i])) {
            //Otherwise get "sun.dc.pr.PRException: endPath: bad path" exception 
            continue;
        }            
        Point2D p = viewport.toView(
                new Point2D.Double(coordinates[i].x, coordinates[i].y));
        setShape(p);
        g.fill(shape);
    }
  }
  
  private void setShape(Point2D p) {
      shape.setFrame(
          p.getX() - sizeOver2,
          p.getY() - sizeOver2, 
          size, size);
  }

}
