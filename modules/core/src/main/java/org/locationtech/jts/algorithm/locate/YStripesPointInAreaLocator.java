package org.locationtech.jts.algorithm.locate;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Point-in-area locator for areal Geometries that uses per-polygon Y-stripe
 * locators to test points efficiently.
 * <p>
 * Each polygonal component of the input Geometry is handled by a
 * YStripesPointInPolygonLocator; when multiple polygonal components are present
 * these per-polygon locators are organized in an STRtree for efficient
 * candidate selection. A single-polygon input uses a direct fast-path, and
 * geometries with no polygonal elements always report EXTERIOR.
 * <p>
 * Instances are immutable and safe for concurrent use, and are intended for
 * repeated point-in-area queries against a fixed Geometry.
 * 
 * @author Michael Carleton
 * @see YStripesPointInPolygonLocator
 */
public final class YStripesPointInAreaLocator implements PointOnGeometryLocator {
	private final Envelope env;
	private final PointOnGeometryLocator single;
	private final STRtree tree; // items are PointOnGeometryLocator
	private Envelope qEnv = new Envelope(0, 0, 0, 0);

	public YStripesPointInAreaLocator(Geometry geom) {
		List<Polygon> polys = new ArrayList<>();
		collectPolygons(geom, polys);

		if (polys.isEmpty()) {
			this.env = geom.getEnvelopeInternal();
			this.single = null;
			this.tree = null;
			return;
		}

		if (polys.size() == 1) {
			this.single = new YStripesPointInPolygonLocator(polys.get(0));
			this.env = null;
			this.tree = null;
			return;
		}

		Envelope e = new Envelope();
		STRtree t = new STRtree();
		for (Polygon p : polys) {
			Envelope pe = p.getEnvelopeInternal();
			e.expandToInclude(pe);
			t.insert(pe, new YStripesPointInPolygonLocator(p));
		}
		t.build();

		this.env = e;
		this.single = null;
		this.tree = t;
	}

	/**
	 * Determines the {@link Location} of a point in an areal {@link Geometry}.
	 * 
	 * @param p the point to test
	 * @return the location of the point in the geometry
	 */
	@Override
	public int locate(Coordinate p) {
		final double x = p.x, y = p.y;

		// no polygonal elements
		if (single == null && tree == null) {
			return Location.EXTERIOR;
		}

		// single polygon fast-path
		if (single != null) {
			return single.locate(p);
		}

		// global envelope reject
		if (!env.contains(x, y)) {
			return Location.EXTERIOR;
		}

		// STRtree candidate lookup
		qEnv.init(p);
		@SuppressWarnings("unchecked")
		List<PointOnGeometryLocator> cands = tree.query(qEnv);

		if (cands.isEmpty()) {
			return Location.EXTERIOR;
		}

		boolean onBoundary = false;
		for (PointOnGeometryLocator loc : cands) {
			int l = loc.locate(p);
			if (l == Location.INTERIOR) {
				return Location.INTERIOR; // interior dominates
			}
			if (l == Location.BOUNDARY) {
				onBoundary = true;
			}
		}
		return onBoundary ? Location.BOUNDARY : Location.EXTERIOR;
	}

	private static void collectPolygons(Geometry g, List<Polygon> out) {
		if (g instanceof Polygon) {
			if (!g.isEmpty()) {
				out.add((Polygon) g);
			}
			return;
		}
		if (g instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection) g;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				collectPolygons(gc.getGeometryN(i), out);
			}
		}
	}
}