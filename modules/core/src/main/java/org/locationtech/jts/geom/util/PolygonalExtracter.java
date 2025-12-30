/*
 * Copyright (c) 2023 Martin Davis.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a
 * {@link Geometry}.
 * <p>
 * This class implements {@link GeometryFilter} so it can be passed to
 * {@link Geometry#apply(GeometryFilter)}.
 * 
 * @version 1.7
 * @see org.locationtech.jts.geom.util.GeometryExtracter GeometryExtracter
 */
public final class PolygonalExtracter implements GeometryFilter {

	/**
	 * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a
	 * {@link Geometry} and adds them to the provided collection.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @param out  the collection to add the extracted elements to
	 * @return the {@code out} collection
	 */
	public static Collection<? super Geometry> getPolygonals(Geometry geom, Collection<? super Geometry> out) {
		if (geom == null || geom.isEmpty())
			return out;

		geom.apply(new PolygonalExtracter(out));
		return out;
	}

	/**
	 * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a
	 * {@link Geometry} and returns them in a list.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @return a new modifiable list containing polygonal elements
	 */
	public static List<Geometry> getPolygonals(Geometry geom) {
		List<Geometry> result = new ArrayList<Geometry>();
		getPolygonals(geom, result);
		return result;
	}

	private final Collection<? super Geometry> comps;

	/**
	 * Constructs a filter with a collection in which to store polygonal elements
	 * found.
	 *
	 * @param comps the collection in which to store polygonal elements found
	 */
	public PolygonalExtracter(Collection<? super Geometry> comps) {
		this.comps = comps;
	}

	@Override
	public void filter(Geometry geom) {
		if (geom instanceof Polygon || geom instanceof MultiPolygon) {
			comps.add(geom);
		}
	}
}