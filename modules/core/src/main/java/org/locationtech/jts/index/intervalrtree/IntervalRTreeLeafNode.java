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

import org.locationtech.jts.index.*;

public class IntervalRTreeLeafNode 
extends IntervalRTreeNode
{
  private Object item;
	
	public IntervalRTreeLeafNode(double min, double max, Object item)
	{
		this.min = min;
		this.max = max;
		this.item = item;
	}
	
	public void query(double queryMin, double queryMax, ItemVisitor visitor)
	{
		if (! intersects(queryMin, queryMax)) 
      return;
		
		visitor.visitItem(item);
	}

	
}
