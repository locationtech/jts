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
package org.locationtech.jts.geom.prep;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

/**
 * An interface for classes which prepare {@link Geometry}s 
 * in order to optimize the performance 
 * of repeated calls to specific geometric operations.
 * <p>
 * A given implementation may provide optimized implementations
 * for only some of the specified methods, 
 * and delegate the remaining methods to the original {@link Geometry} operations.
 * An implementation may also only optimize certain situations,
 * and delegate others. 
 * See the implementing classes for documentation about which methods and situations
 * they optimize.
 * <p>
 * Subclasses are intended to be thread-safe, to allow <code>PreparedGeometry</code>
 * to be used in a multi-threaded context 
 * (which allows extracting maximum benefit from the prepared state).
 * 
 * @author Martin Davis
 *
 */
public interface PreparedGeometry 
{
	
	/**
	 * Gets the original {@link Geometry} which has been prepared.
	 * 
	 * @return the base geometry
	 */
	Geometry getGeometry();

	/**
	 * Tests whether the base {@link Geometry} contains a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry contains the given Geometry
	 * 
	 * @see Geometry#contains(Geometry)
	 */
	boolean contains(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} properly contains a given geometry.
	 * <p>
	 * The <code>containsProperly</code> predicate has the following equivalent definitions:
	 * <ul>
	 * <li>Every point of the other geometry is a point of this geometry's interior.
	 * <li>The DE-9IM Intersection Matrix for the two geometries matches 
	 * <code>[T**FF*FF*]</code>
	 * </ul>
	 * In other words, if the test geometry has any interaction with the boundary of the target
	 * geometry the result of <tt>containsProperly</tt> is <tt>false</tt>. 
	 * This is different semantics to the {@link Geometry#contains} predicate,
	 * in which test geometries can intersect the target's boundary and still be contained.
	 * <p>
	 * The advantage of using this predicate is that it can be computed
	 * efficiently, since it avoids the need to compute the full topological relationship
	 * of the input boundaries in cases where they intersect.
	 * <p>
	 * An example use case is computing the intersections
	 * of a set of geometries with a large polygonal geometry.  
	 * Since <tt>intersection</tt> is a fairly slow operation, it can be more efficient
	 * to use <tt>containsProperly</tt> to filter out test geometries which lie
	 * wholly inside the area.  In these cases the intersection is
	 * known <i>a priori</i> to be exactly the original test geometry. 
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry properly contains the given Geometry
	 * 
	 * @see Geometry#contains
	 * 
	 */
	boolean containsProperly(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} is covered by a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry is covered by the given Geometry
	 * 
	 * @see Geometry#coveredBy(Geometry)
	 */
	boolean coveredBy(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} covers a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry covers the given Geometry
	 * 
	 * @see Geometry#covers(Geometry)
	 */
	boolean covers(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} crosses a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry crosses the given Geometry
	 * 
	 * @see Geometry#crosses(Geometry)
	 */
	boolean crosses(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} is disjoint from a given geometry.
	 * This method supports {@link GeometryCollection}s as input
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry is disjoint from the given Geometry
	 * 
	 * @see Geometry#disjoint(Geometry)
	 */
	boolean disjoint(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} intersects a given geometry.
	 * This method supports {@link GeometryCollection}s as input
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry intersects the given Geometry
	 * 
	 * @see Geometry#intersects(Geometry)
	 */
	boolean intersects(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} overlaps a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry overlaps the given Geometry
	 * 
	 * @see Geometry#overlaps(Geometry)
	 */
	boolean overlaps(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} touches a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry touches the given Geometry
	 * 
	 * @see Geometry#touches(Geometry)
	 */
	boolean touches(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} is within a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry is within the given Geometry
	 * 
	 * @see Geometry#within(Geometry)
	 */
	boolean within(Geometry geom);

}
