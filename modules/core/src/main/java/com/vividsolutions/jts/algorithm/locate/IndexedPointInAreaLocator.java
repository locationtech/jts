/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.algorithm.locate;

import java.util.*;

import com.vividsolutions.jts.algorithm.RayCrossingCounter;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.index.*;
import com.vividsolutions.jts.index.intervalrtree.*;

/**
 * Determines the {@link Location} of {@link Coordinate}s relative to
 * a {@link Polygonal} geometry, using indexing for efficiency.
 * This algorithm is suitable for use in cases where
 * many points will be tested against a given area.
 * 
 * Thread-safe and immutable.
 *
 * @author Martin Davis
 *
 */
public class IndexedPointInAreaLocator 
  implements PointOnGeometryLocator
{
  private final IntervalIndexedGeometry index;
  
  /**
   * Creates a new locator for a given {@link Geometry}
   * @param g the Geometry to locate in
   */
  public IndexedPointInAreaLocator(Geometry g)
  {
    if (! (g instanceof Polygonal))
      throw new IllegalArgumentException("Argument must be Polygonal");
    index = new IntervalIndexedGeometry(g);
  }
    
  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * 
   * @param p the point to test
   * @return the location of the point in the geometry  
   */
  public int locate(Coordinate p)
  {
    RayCrossingCounter rcc = new RayCrossingCounter(p);
    
    SegmentVisitor visitor = new SegmentVisitor(rcc);
    index.query(p.y, p.y, visitor);
  
    /*
     // MD - slightly slower alternative
    List segs = index.query(p.y, p.y);
    countSegs(rcc, segs);
    */
    
    return rcc.getLocation();
  }
  
  private static class SegmentVisitor
    implements ItemVisitor
  {
    private RayCrossingCounter counter;
    
    public SegmentVisitor(RayCrossingCounter counter)
    {
      this.counter = counter;
    }
    
    public void visitItem(Object item)
    {
      LineSegment seg = (LineSegment) item;
      counter.countSegment(seg.getCoordinate(0), seg.getCoordinate(1));
    }
  }
  
  private static class IntervalIndexedGeometry
  {
    private final SortedPackedIntervalRTree index= new SortedPackedIntervalRTree();

    public IntervalIndexedGeometry(Geometry geom)
    {
      init(geom);
    }
    
    private void init(Geometry geom)
    {
      List lines = LinearComponentExtracter.getLines(geom);
      for (Iterator i = lines.iterator(); i.hasNext(); ) {
        LineString line = (LineString) i.next();
        Coordinate[] pts = line.getCoordinates();
        addLine(pts);
      }
    }
    
    private void addLine(Coordinate[] pts)
    {
      for (int i = 1; i < pts.length; i++) {
        LineSegment seg = new LineSegment(pts[i-1], pts[i]);
        double min = Math.min(seg.p0.y, seg.p1.y);
        double max = Math.max(seg.p0.y, seg.p1.y);
        index.insert(min, max, seg);
      }
    }
    
    public List query(double min, double max)
    {
      ArrayListVisitor visitor = new ArrayListVisitor();
      index.query(min, max, visitor);
      return visitor.getItems();
    }
    
    public void query(double min, double max, ItemVisitor visitor)
    {
      index.query(min, max, visitor);
    }
  }

}



