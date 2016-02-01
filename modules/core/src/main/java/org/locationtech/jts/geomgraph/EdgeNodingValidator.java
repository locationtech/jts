
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
package org.locationtech.jts.geomgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.FastNodingValidator;

/**
 * Validates that a collection of {@link Edge}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class EdgeNodingValidator 
{  
	/**
   * Checks whether the supplied {@link Edge}s
   * are correctly noded.  
   * Throws a  {@link TopologyException} if they are not.
   * 
   * @param edges a collection of Edges.
   * @throws TopologyException if the SegmentStrings are not correctly noded
   *
   */
	public static void checkValid(Collection edges)
	{
		EdgeNodingValidator validator = new EdgeNodingValidator(edges);
		validator.checkValid();
	}
	
  public static Collection toSegmentStrings(Collection edges)
  {
    // convert Edges to SegmentStrings
    Collection segStrings = new ArrayList();
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      Edge e = (Edge) i.next();
      segStrings.add(new BasicSegmentString(e.getCoordinates(), e));
    }
    return segStrings;
  }

  private FastNodingValidator nv;

  /**
   * Creates a new validator for the given collection of {@link Edge}s.
   * 
   * @param edges a collection of Edges.
   */
  public EdgeNodingValidator(Collection edges)
  {
    nv = new FastNodingValidator(toSegmentStrings(edges));
  }

  /**
   * Checks whether the supplied edges
   * are correctly noded.  Throws an exception if they are not.
   * 
   * @throws TopologyException if the SegmentStrings are not correctly noded
   *
   */
  public void checkValid()
  {
    nv.checkValid();
  }

}
