/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.util;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.Point;

/**
 * Extracts all the 0-dimensional ({@link Point}) components from a
 * {@link Geometry}.
 * 
 * @version 1.7
 * @see org.locationtech.jts.geom.util.GeometryExtracter GeometryExtracter
 */
public final class PointExtracter implements GeometryFilter {
	/**
	 * Extracts the {@link Point} elements from a single {@link Geometry} and adds
	 * them to the provided {@link Collection}.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @param out  an optional collection to add the extracted points to (may be
	 *             {@code null})
	 * @return a new modifiable {@link List} containing the extracted points
	 */
	public static List<Point> getPoints(Geometry geom, Collection<? super Point> out) {
		return GeometryExtracter.extractByClass(geom, Point.class, out);
	}

	/**
	 * Extracts the {@link Point} elements from a single {@link Geometry} and
	 * returns them in a {@link List}.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @return a new modifiable {@link List} containing the extracted points
	 */
	public static List<Point> getPoints(Geometry geom) {
		return GeometryExtracter.extractByClass(geom, Point.class);
	}

	private final Collection<? super Point> comps;

	/**
	 * Constructs a filter with a collection in which to store {@link Point}s found.
	 *
	 * @param comps the collection in which to store points found
	 */
	public PointExtracter(Collection<? super Point> comps) {
		this.comps = comps;
	}

	@Override
	public void filter(Geometry geom) {
		if (geom instanceof Point)
			comps.add((Point) geom);
	}
}