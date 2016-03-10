/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.intervalrtree;

import java.util.Comparator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.io.WKTWriter;


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
