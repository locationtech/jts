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

import org.locationtech.jts.index.ItemVisitor;

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
