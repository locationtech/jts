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
package org.locationtech.jts.index.intervalrtree;

import java.util.Comparator;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.*;
import org.locationtech.jts.io.*;


public abstract class IntervalRTreeNode 
{
	protected double min = Double.POSITIVE_INFINITY;
	protected double max = Double.NEGATIVE_INFINITY;

	public double getMin() { return min; }
	public double getMax() { return max; }
	
	public abstract void query(double queryMin, double queryMax, ItemVisitor visitor);
	
	protected boolean intersects(double queryMin, double queryMax)
	{
		if (min > queryMax 
				|| max < queryMin)
			return false;
		return true;
	}

	public String toString()
	{
		return WKTWriter.toLineString(new Coordinate(min, 0), new Coordinate(max, 0));
	}
  
  public static class NodeComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      IntervalRTreeNode n1 = (IntervalRTreeNode) o1;
      IntervalRTreeNode n2 = (IntervalRTreeNode) o2;
      double mid1 = (n1.min + n1.max) / 2;
      double mid2 = (n2.min + n2.max) / 2;
      if (mid1 < mid2) return -1;
      if (mid1 > mid2) return 1;
      return 0;
    }
  }

}
