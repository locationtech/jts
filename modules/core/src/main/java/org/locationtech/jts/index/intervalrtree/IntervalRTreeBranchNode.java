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

public class IntervalRTreeBranchNode 
extends IntervalRTreeNode
{
	private IntervalRTreeNode node1;
	private IntervalRTreeNode node2;
	
	public IntervalRTreeBranchNode(IntervalRTreeNode n1, IntervalRTreeNode n2)
	{
		node1 = n1;
		node2 = n2;
		buildExtent(node1, node2);
	}
	
	private void buildExtent(IntervalRTreeNode n1, IntervalRTreeNode n2)
	{
		min = Math.min(n1.min, n2.min);
		max = Math.max(n1.max, n2.max);
	}
	
	public void query(double queryMin, double queryMax, ItemVisitor visitor)
	{
		if (! intersects(queryMin, queryMax)) {
//			System.out.println("Does NOT Overlap branch: " + this);
			return;
		}
//		System.out.println("Overlaps branch: " + this);
		if (node1 != null) node1.query(queryMin, queryMax, visitor);
		if (node2 != null) node2.query(queryMin, queryMax, visitor);
	}
	
}
