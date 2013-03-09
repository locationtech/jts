package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.*;

public class VertexStyle  implements Style
{
  private double sizeOver2 = AppConstants.VERTEX_SIZE / 2d;
  
  protected Rectangle shape;
  private Color color;
  
  // reuse point objects to avoid creation overhead
  private Point2D pM = new Point2D.Double();
  private Point2D pV = new Point2D.Double();

  public VertexStyle(Color color) {
    this.color = color;
    // create basic rectangle shape
    shape = new Rectangle(0,
        0, 
        AppConstants.VERTEX_SIZE, 
        AppConstants.VERTEX_SIZE);
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
        pM.setLocation(coordinates[i].x, coordinates[i].y);
        viewport.toView(pM, pV);
      	shape.setLocation((int) (pV.getX() - sizeOver2), (int) (pV.getY() - sizeOver2));
        g.fill(shape);
    }
  }
  
}
