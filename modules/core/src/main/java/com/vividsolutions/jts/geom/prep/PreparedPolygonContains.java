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
package com.vividsolutions.jts.geom.prep;


import com.vividsolutions.jts.geom.*;

/**
 * Computes the <tt>contains</tt> spatial relationship predicate
 * for a {@link PreparedPolygon} relative to all other {@link Geometry} classes.
 * Uses short-circuit tests and indexing to improve performance. 
 * <p>
 * It is not possible to short-circuit in all cases, in particular
 * in the case where the test geometry touches the polygon linework.
 * In this case full topology must be computed.
 * 
 * @author Martin Davis
 *
 */
class PreparedPolygonContains 
	extends AbstractPreparedPolygonContains
{
	/**
	 * Computes the </tt>contains</tt> predicate between a {@link PreparedPolygon}
	 * and a {@link Geometry}.
	 * 
	 * @param prep the prepared polygon
	 * @param geom a test geometry
	 * @return true if the polygon contains the geometry
	 */
	public static boolean contains(PreparedPolygon prep, Geometry geom)
	{
    PreparedPolygonContains polyInt = new PreparedPolygonContains(prep);
    return polyInt.contains(geom);
	}

  /**
   * Creates an instance of this operation.
   * 
   * @param prepPoly the PreparedPolygon to evaluate
   */
	public PreparedPolygonContains(PreparedPolygon prepPoly)
	{
		super(prepPoly);
	}
		
	/**
	 * Tests whether this PreparedPolygon <tt>contains</tt> a given geometry.
	 * 
	 * @param geom the test geometry
	 * @return true if the test geometry is contained
	 */
	public boolean contains(Geometry geom)
	{
		return eval(geom);
	}
	
	/**
	 * Computes the full topological <tt>contains</tt> predicate.
	 * Used when short-circuit tests are not conclusive.
	 * 
	 * @param geom the test geometry
	 * @return true if this prepared polygon contains the test geometry
	 */
	protected boolean fullTopologicalPredicate(Geometry geom)
	{
		boolean isContained = prepPoly.getGeometry().contains(geom);
		return isContained;
	}
	
}
