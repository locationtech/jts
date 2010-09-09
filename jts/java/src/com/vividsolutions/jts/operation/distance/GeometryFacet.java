package com.vividsolutions.jts.operation.distance;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

/**
 * Represents a line segment or point of a {@link Geometry},
 * and records its distance to another facet.
 * 
 * @author Martin Davis
 *
 */
public class GeometryFacet
  implements Comparable
{
  private Coordinate p0;
  private Coordinate p1 = null;
  double distance;
  
  public GeometryFacet(Coordinate p0, Coordinate p1, double distance)
  {
    this.p0 = p0;
    this.p1 = p1;
    this.distance = distance;
  }
  
  public double getDistance() { return distance; }
  
  public boolean isSegment()
  {
    return p1 != null;
  }
  
  public Envelope getEnvelope()
  {
    Envelope env = new Envelope(p0);
    if (p1 != null)
      env.expandToInclude(p1);
    return env;
  }
  
  public double distance(GeometryFacet facet)
  {
    if (isSegment()) {
      if (facet.isSegment()) {
        return CGAlgorithms.distanceLineLine(
          p0, p1, facet.p0, facet.p1 );
      }
      else {
        return CGAlgorithms.distancePointLine(facet.p0, p0, p1);
      }
    }
    // this is a point
    if (facet.isSegment()) {
      return CGAlgorithms.distancePointLine(
        p0, facet.p0, facet.p1 );
    }
    else {
      return p0.distance(facet.p0);
    }
    
    
  }
  /**
   *  Compares this object with the specified object for order.
   *  Uses the standard lexicographic ordering for the points in the LineSegment.
   *
   *@param  o  the <code>LineSegment</code> with which this <code>LineSegment</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineSegment</code>
   *      is less than, equal to, or greater than the specified <code>LineSegment</code>
   */
  public int compareTo(Object o) {
    GeometryFacet sd = (GeometryFacet) o;
    return Double.compare(distance, sd.distance);
  }
}