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
import org.locationtech.jts.geom.Polygon;

/**
 * Extracts {@link Polygon} components from a {@link Geometry}.
 * <p>
 * This class implements {@link GeometryFilter} so it can be passed to
 * {@link Geometry#apply(GeometryFilter)}.
 * 
 * @version 1.7
 * @see org.locationtech.jts.geom.util.GeometryExtracter GeometryExtracter
 */
public final class PolygonExtracter implements GeometryFilter {

	/**
	 * Extracts the {@link Polygon} elements from a single {@link Geometry} and adds
	 * them to the provided {@link Collection}.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @param out  an optional collection to add the extracted polygons to (may be
	 *             {@code null})
	 * @return a new modifiable {@link List} containing the extracted polygons
	 */
	public static List<Polygon> getPolygons(Geometry geom, Collection<? super Polygon> out) {
		return GeometryExtracter.extractByClass(geom, Polygon.class, out);
	}

	/**
	 * Extracts the {@link Polygon} elements from a single {@link Geometry} and
	 * returns them in a {@link List}.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @return a new modifiable {@link List} containing the extracted polygons
	 */
	public static List<Polygon> getPolygons(Geometry geom) {
		return GeometryExtracter.extractByClass(geom, Polygon.class);
	}

	private final Collection<? super Polygon> comps;

	/**
	 * Constructs a filter with a collection in which to store {@link Polygon}s
	 * found.
	 *
	 * @param comps the collection in which to store polygons found
	 */
	public PolygonExtracter(Collection<? super Polygon> comps) {
		this.comps = comps;
	}

	@Override
	public void filter(Geometry geom) {
		if (geom instanceof Polygon)
			comps.add((Polygon) geom);
	}
}