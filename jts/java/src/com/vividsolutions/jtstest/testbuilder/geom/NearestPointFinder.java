package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.*;

public class NearestPointFinder 
{
  public static Coordinate findNearestPoint(Geometry geom, Coordinate pt, double tolerance)
  {
    NearestPointFinder finder = new NearestPointFinder(geom);
    return finder.getNearestPoint(pt, tolerance);
  }

  private Geometry geom;
  
  public NearestPointFinder(Geometry geom) {
    this.geom = geom;
  }
  
  public Coordinate getNearestPoint(Coordinate pt, double tolerance)
  {
    NearestPointFilter filter = new NearestPointFilter(pt, tolerance);
    geom.apply(filter);
    return filter.getNearestPoint();
  }
  
  static class NearestPointFilter implements CoordinateSequenceFilter
  {
    private double tolerance = 0.0;
    private Coordinate basePt;
    private Coordinate nearestPt = null;
    private double dist = Double.MAX_VALUE;
    
    public NearestPointFilter(Coordinate basePt, double tolerance)
    {
      this.basePt = basePt;
      this.tolerance = tolerance;
    }

    public void filter(CoordinateSequence seq, int i)
    {
      Coordinate p = seq.getCoordinate(i);
      double dist = p.distance(basePt);
      if (dist > tolerance) return;
      
      if (nearestPt == null || basePt.distance(p) < dist) {
        nearestPt = p;
        dist = basePt.distance(nearestPt);
        return;
      }
    }
    
    public Coordinate getNearestPoint() 
    {
      return nearestPt;
    }
    public boolean isDone() { return false; }

    public boolean isGeometryChanged() { return false; }
  }

}
