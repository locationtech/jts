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
package org.locationtech.jtslab.snapround;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

public class PolygonCleaner {

	public static Geometry clean(Geometry geom) {
		PolygonCleanerTransformer trans = new PolygonCleanerTransformer();
		return trans.transform(geom);
	}

	static class PolygonCleanerTransformer extends GeometryTransformer {

		PolygonCleanerTransformer() {
		}

		protected Geometry transformPolygon(Polygon geom, Geometry parent) {
			// if parent is a MultiPolygon, let it do the cleaning
			if (parent instanceof MultiPolygon) {
				return geom;
			}
			return createValidArea(geom);
		}

		protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
			Geometry roughGeom = super.transformMultiPolygon(geom, parent);
			return createValidArea(roughGeom);
		}

		/**
		 * Creates a valid area geometry from one that possibly has bad topology (i.e.
		 * self-intersections). 
		 * 
		 * @param area
		 *            an area geometry possibly containing self-intersections
		 * @return a valid area geometry
		 */
		private Geometry createValidArea(Geometry area) {
			if (area.isValid()) return area;
			// TODO: this is slow and has potential errors (due to buffer robustness failure)
			// TODO: replace with a proper polygon cleaner
			/**
			 * Creates a valid area geometry from one that possibly has bad topology (i.e.
			 * self-intersections). Since buffer can handle invalid topology, but always
			 * returns valid geometry, constructing a 0-width buffer "corrects" the
			 * topology. Note this only works for area geometries, since buffer always
			 * returns areas. This also may return empty geometries, if the input has no
			 * actual area.
			 */
			return area.buffer(0.0);
		}
	}

}
