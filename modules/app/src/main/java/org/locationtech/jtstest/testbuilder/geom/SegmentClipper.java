package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Clips a segment to a rectangle.
 * Modification is done in-place in the input Coordinates,
 * so do not pass in Coordinates from source Geometries. 
 * 
 * @author mbdavis
 *
 */
public class SegmentClipper {
  
  public static void clip(Coordinate p0, Coordinate p1, Envelope env) {
    clipEndpoint(p0, p1, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
    clipEndpoint(p1, p0, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
  }

  private static void clipEndpoint(Coordinate p0, Coordinate p1, 
        double xmin, double ymin, double xmax, double ymax) {
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double x = p0.getX();
    double y = p0.getY();
    if (dx != 0) {
      if (x < xmin) {
        y = y + (xmin - x) * dy / dx;
        x = xmin;
      }
      else if (x > xmax) {
        y = y + (xmax - x) * dy / dx;
        x = xmax;
      }
    }
    if (dy != 0) {
      if (y < ymin) {
        x = x + (ymin - y) * dx / dy;
        y = ymin;
      }
      else if (y > ymax) {
        x = x + (ymax - y) * dx / dy;
        y = ymax;
      }
    }
    p0.setX(x);
    p0.setY(y);
  }
}
