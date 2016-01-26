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

package com.vividsolutions.jts.index.kdtree;

/**
 * A visitor for {@link KdNode}s in a {@link KdTree} index.
 *
 * @version 1.7
 */

public interface KdNodeVisitor
{
  /**
   * Visits a node.
   * 
   * @param node the node to visit
   */
  void visit(KdNode node);
}
