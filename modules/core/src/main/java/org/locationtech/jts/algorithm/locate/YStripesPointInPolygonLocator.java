package org.locationtech.jts.algorithm.locate;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

/**
 * Fast point-in-polygon locator using Y-stripes.
 * <p>
 * The YStripes structure partitions the bounding box of a polygon into
 * horizontal "stripes". Each stripe stores a list of polygon segment indices
 * that intersect it. This allows for O(1) lookup of potentially intersecting
 * segments for any given Y-coordinate, drastically reducing the number of
 * segments that need to be checked for a point-in-polygon test.
 * <p>
 * For more details, see the <a href=
 * "https://github.com/tidwall/tg/blob/main/docs/POLYGON_INDEXING.md#ystripes">
 * YStripes: Polygon Indexing in 'tg' Library</a>.
 * 
 * @author Michael Carleton
 */
final class YStripesPointInPolygonLocator implements PointOnGeometryLocator {
	private final RingIndex shell;
	private final RingIndex[] holes;

	YStripesPointInPolygonLocator(Polygon polygon) {
		if (polygon == null) {
			throw new IllegalArgumentException("polygon is null");
		}
		shell = RingIndex.build(polygon.getExteriorRing());
		holes = new RingIndex[polygon.getNumInteriorRing()];
		for (int i = 0; i < holes.length; i++) {
			holes[i] = RingIndex.build(polygon.getInteriorRingN(i));
		}
	}

	/**
	 * Determines the {@link Location} of a point in a {@link Polygon}.
	 * 
	 * @param p the point to test
	 * @return the location of the point in the geometry
	 */
	@Override
	public int locate(Coordinate p) {
		final double x = p.x, y = p.y;
		if (!shell.coversPointFast(x, y)) {
			return Location.EXTERIOR;
		}
		int loc = shell.locateYStripes(x, y);
		if (loc != Location.INTERIOR) {
			return loc; // BOUNDARY or EXTERIOR
		}

		for (RingIndex hole : holes) {
			if (!hole.coversPointFast(x, y)) {
				continue;
			}
			int hLoc = hole.locateYStripes(x, y);
			if (hLoc == Location.BOUNDARY) {
				return Location.BOUNDARY;
			}
			if (hLoc == Location.INTERIOR) {
				return Location.EXTERIOR; // inside a hole
			}
		}
		return Location.INTERIOR;
	}

	/**
	 * Per-ring Y-stripe index with flat storage.
	 */
	private static final class RingIndex {
		final double[] xs, ys;
		final double[] segXMin, segXMax, segYMin, segYMax;

		final double minX, minY, maxX, maxY, height, invH;

		final int nStripes;
		final int[] stripeOffsets; // length = nStripes
		final int[] stripeCounts; // length = nStripes
		final int[] segIndex; // length = total mapped entries

		private RingIndex(double[] xs, double[] ys, double[] segXMin, double[] segXMax, double[] segYMin, double[] segYMax, double minX, double minY,
				double maxX, double maxY, int nStripes, int[] stripeOffsets, int[] stripeCounts, int[] segIndex) {
			this.xs = xs;
			this.ys = ys;
			this.segXMin = segXMin;
			this.segXMax = segXMax;
			this.segYMin = segYMin;
			this.segYMax = segYMax;

			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
			this.height = maxY - minY;
			this.invH = height == 0 ? 0.0 : 1.0 / height;

			this.nStripes = nStripes;
			this.stripeOffsets = stripeOffsets;
			this.stripeCounts = stripeCounts;
			this.segIndex = segIndex;
		}

		static RingIndex build(LinearRing ring) {
			CoordinateSequence seq = ring.getCoordinateSequence();
			int n = seq.size();
			if (n < 2) {
				throw new IllegalArgumentException("Ring has < 2 points");
			}

			double[] xs = new double[n];
			double[] ys = new double[n];

			double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < n; i++) {
				double x = seq.getX(i), y = seq.getY(i);
				xs[i] = x;
				ys[i] = y;
				if (x < minX) {
					minX = x;
				}
				if (x > maxX) {
					maxX = x;
				}
				if (y < minY) {
					minY = y;
				}
				if (y > maxY) {
					maxY = y;
				}
			}

			int nSegs = n - 1;
			double[] segXMin = new double[nSegs];
			double[] segXMax = new double[nSegs];
			double[] segYMin = new double[nSegs];
			double[] segYMax = new double[nSegs];

			// compute seg bboxes + perim + area in one pass
			double perim = 0.0;
			double area2 = 0.0;
			for (int i = 0; i < nSegs; i++) {
				double ax = xs[i], ay = ys[i];
				double bx = xs[i + 1], by = ys[i + 1];
				double xmin = (ax < bx) ? ax : bx;
				double xmax = (ax > bx) ? ax : bx;
				double ymin = (ay < by) ? ay : by;
				double ymax = (ay > by) ? ay : by;
				segXMin[i] = xmin;
				segXMax[i] = xmax;
				segYMin[i] = ymin;
				segYMax[i] = ymax;
				// perim
				double dx = bx - ax, dy = by - ay;
				perim += Math.sqrt(dx * dx + dy * dy);
				// area (shoelace)
				area2 += ax * by - bx * ay;
			}
			double area = Math.abs(0.5 * area2);

			// Stripe heuristic: cheap and stable
			int base = Math.max(64, Math.min(nSegs, 65_536));
			double score = (perim > 0) ? (area * Math.PI * 4.0) / (perim * perim) : 1.0; // Polsby-Popper
			double boost = Math.max(0.35, Math.min(1.0, score * 1.5));
			int nStripes = (maxY == minY) ? 1 : Math.max(1, (int) Math.round(base * boost));

			if (nStripes == 1) {
				int[] stripeOffsets = new int[] { 0 };
				int[] stripeCounts = new int[] { nSegs };
				int[] segIndex = new int[nSegs];
				for (int i = 0; i < nSegs; i++) {
					segIndex[i] = i;
				}
				return new RingIndex(xs, ys, segXMin, segXMax, segYMin, segYMax, minX, minY, maxX, maxY, 1, stripeOffsets, stripeCounts, segIndex);
			}

			final double scale = nStripes / (maxY - minY);
			int[] counts = new int[nStripes]; // zero-initialized
			int nMap = 0;

			for (int i = 0; i < nSegs; i++) {
				int smin = (int) ((segYMin[i] - minY) * scale);
				int smax = (int) ((segYMax[i] - minY) * scale);
				if (smax >= nStripes) {
					smax = nStripes - 1;
				}
				if (smin < 0) {
					smin = 0;
				}
				if (smin > smax) {
					smin = smax;
				}
				for (int s = smin; s <= smax; s++) {
					counts[s]++;
					nMap++;
				}
			}

			int[] stripeOffsets = new int[nStripes];
			int[] stripeCounts = new int[nStripes];
			int run = 0;
			for (int s = 0; s < nStripes; s++) {
				stripeOffsets[s] = run;
				run += counts[s];
			}
			int[] segIndex = new int[nMap];

			for (int i = 0; i < nSegs; i++) {
				int smin = (int) ((segYMin[i] - minY) * scale);
				int smax = (int) ((segYMax[i] - minY) * scale);
				if (smax >= nStripes) {
					smax = nStripes - 1;
				}
				if (smin < 0) {
					smin = 0;
				}
				if (smin > smax) {
					smin = smax;
				}
				for (int s = smin; s <= smax; s++) {
					int pos = stripeOffsets[s] + stripeCounts[s]++;
					segIndex[pos] = i;
				}
			}

			return new RingIndex(xs, ys, segXMin, segXMax, segYMin, segYMax, minX, minY, maxX, maxY, nStripes, stripeOffsets, stripeCounts, segIndex);
		}

		/**
		 * Fast envelope check against the ring's bounding box.
		 */
		boolean coversPointFast(double x, double y) {
			return !(y < minY || y > maxY || x < minX || x > maxX);
		}

		/**
		 * Locates a point relative to the ring using Y-stripe lookup and ray crossing.
		 */
		int locateYStripes(double px, double py) {
			boolean in = false;
			int onIdx = -1;

			int s;
			if (nStripes == 1 || height == 0) {
				s = 0;
			} else {
				s = (int) (((py - minY) * invH) * nStripes);
				if (s < 0) {
					s = 0;
				} else if (s >= nStripes) {
					s = nStripes - 1;
				}
			}

			int base = stripeOffsets[s];
			int cnt = stripeCounts[s];
			int end = base + cnt;

			for (int p = base; p < end; p++) {
				int i = segIndex[p];

				double ymin = segYMin[i], ymax = segYMax[i];
				if (py < ymin || py > ymax) {
					continue;
				}

				double ax = xs[i], ay = ys[i];
				double bx = xs[i + 1], by = ys[i + 1];
				double xmin = segXMin[i], xmax = segXMax[i];

				// Horizontal edges: boundary only; never counted for crossings
				if (ay == by) {
					if (py == ay && px >= xmin && px <= xmax) {
						onIdx = i;
						break;
					}
					continue;
				}

				// Entire segment strictly to the right of the point: guaranteed crossing if it
				// straddles py (half-open rule)
				if (px < xmin) {
					if ((ay > py) != (by > py)) {
						in = !in;
					}
					continue;
				}

				// Entire segment strictly to the left of the point: cannot affect parity
				if (px > xmax) {
					continue;
				}

				int rc = raycast(ax, ay, bx, by, px, py, xmin, xmax, ymin, ymax);
				if (rc == RC.ON) {
					onIdx = i;
					break;
				} else if (rc == RC.IN) {
					in = !in;
				}
			}

			if (onIdx != -1) {
				return Location.BOUNDARY;
			}
			return in ? Location.INTERIOR : Location.EXTERIOR;
		}

		/**
		 * Ray-crossing against a single segment. Handles horizontal/vertical/degenerate
		 * segments and uses a robust collinearity test when needed.
		 *
		 * @param ax   seg start X
		 * @param ay   seg start Y
		 * @param bx   seg end X
		 * @param by   seg end Y
		 * @param px   point X
		 * @param py   point Y
		 * @param minx segment bbox min X
		 * @param maxx segment bbox max X
		 * @param miny segment bbox min Y
		 * @param maxy segment bbox max Y
		 * @return one of {@link RC#OUT}, {@link RC#IN}, {@link RC#ON}
		 */
		private static int raycast(double ax, double ay, double bx, double by, double px, double py, double minx, double maxx, double miny, double maxy) {
			if ((px == ax && py == ay) || (px == bx && py == by)) {
				return RC.ON;
			}
			if (ay == by) {
				if (py == ay && px >= minx && px <= maxx) {
					return RC.ON;
				}
				return RC.OUT;
			}
			// Half-open straddle check first (avoids orientation for non-straddling edges)
			if (!((ay > py) != (by > py))) {
				return RC.OUT;
			}
			// need DD to pass, otherwise RobustDeterminant good enough?
			int orient = CGAlgorithmsDD.orientationIndex(ax, ay, bx, by, px, py);
			if (orient == 0) {
				if (px >= minx && px <= maxx && py >= miny && py <= maxy) {
					return RC.ON;
				}
				return RC.OUT;
			}
			if (by < ay) {
				orient = -orient;
			}
			return (orient > 0) ? RC.IN : RC.OUT;
		}

		/**
		 * Return codes for segment ray-casting.
		 */
		private static final class RC {
			/** Ray misses or segment rejected. */
			static final int OUT = 0;
			/** Ray crosses segment (affects inside parity). */
			static final int IN = 1;
			/** Point is on segment (boundary). */
			static final int ON = 2;

			private RC() {
			}
		}
	}
}
