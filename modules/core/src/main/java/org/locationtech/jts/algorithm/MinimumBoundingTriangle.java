/*
 * Copyright (c) 2025 Michael Carleton
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import static java.lang.Math.floorMod;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.MathUtil;

/**
 * Computes the Minimum Bounding Triangle (MBT) for the points in a Geometry.
 * The MBT is the smallest triangle which covers all the input points (this is
 * also known as the Smallest Enclosing Triangle).
 * <p>
 * The algorithm for finding minimum area enclosing triangles is based on an
 * elegant geometric characterisation initially introduced in Klee & Laskowski.
 * The algorithm iterates over each edge of the convex polygon setting side C of
 * the enclosing triangle to be flush with this edge. A side <i>S</i> is said to
 * be flush with edge <i>E</i> if <i>S⊇E</i>. The authors of O’Rourke et al.
 * prove that for each fixed flush side C a local minimum enclosing triangle
 * exists. Moreover, the authors have shown that:
 * <ul>
 * <li>The midpoints of the enclosing triangle’s sides must touch the
 * polygon.</li>
 * <li>There exists a local minimum enclosing triangle with at least two sides
 * flush with edges of the polygon. The third side of the triangle can be either
 * flush with an edge or tangent to the polygon.</li>
 * </ul>
 * Thus, for each flush side C the algorithm will find the second flush side and
 * set the third side either flush/tangent to the polygon.
 * <p>
 * O'Rourke provides a θ(n) algorithm for finding the minimal enclosing triangle
 * of a 2D <b>convex</b> polygon with n vertices. However, the overall
 * complexity for the concave computation is O(nlog(n)) because a convex hull
 * must first be computed for the input geometry.
 * 
 * @author Python <a href=
 *         "https://web.archive.org/web/20211006220154/https://github.com/crm416/point-location/blob/master/min_triangle.py">implementation</a>
 *         by Charlie Marsh
 * @author Java port and enhancements by Michael Carleton
 *
 */
public class MinimumBoundingTriangle {

	private final Geometry hull;
	private final int n;
	private final Coordinate[] points;
	private final GeometryFactory gf;
	private final double tol;

	/**
	 * Constructs a solver for computing a minimum-area enclosing triangle for the
	 * input geometry.
	 * <p>
	 * The input geometry is reduced to its convex hull; only the hull vertices are
	 * used in the computation. The coordinate sequence of the hull is treated as an
	 * open (unclosed) ring by dropping a trailing duplicate coordinate if present.
	 * </p>
	 * <p>
	 * The input must contain at least three non-collinear points. If the convex
	 * hull of the input has dimension less than 2 (i.e., it is a point or a line),
	 * this constructor throws an exception.
	 * </p>
	 *
	 * @param shape any JTS geometry; its convex hull drives the computation
	 * @throws IllegalArgumentException if the convex hull of {@code shape} has
	 *                                  dimension &lt; 2 (fewer than three
	 *                                  non-collinear points)
	 */
	public MinimumBoundingTriangle(Geometry shape) {
		gf = shape.getFactory();
		hull = shape.convexHull();
		int dim = hull.getDimension();
		if (dim < 2) {
			throw new IllegalArgumentException("MinimumBoundingTriangle requires at least 3 non-collinear points.");
		}
		// Treat coordinates as unclosed by default
		points = Arrays.copyOfRange(hull.getCoordinates(), 0, hull.getCoordinates().length - 1);
		n = points.length;

		// adaptive tolerance (so we can handle huge geometries gracefully)
		double coordMag = 0.0;
		for (Coordinate c : points) {
			coordMag = Math.max(coordMag, Math.max(Math.abs(c.x), Math.abs(c.y)));
		}
		double eps = Math.ulp(1.0);
		tol = 10.0 * eps * Math.max(coordMag, 1.0);
	}

	/**
	 * Computes a minimum-area triangle that encloses the convex hull of the input
	 * geometry.
	 * <p>
	 * This implements a rotating-calipers-style search over the convex polygon. If
	 * a valid enclosing triangle cannot be identified due to degeneracy (e.g., hull
	 * has fewer than three distinct points, or near-parallel/collinear
	 * configurations beyond tolerance), this method returns {@code null}.
	 *
	 * @return a triangle polygon of minimum area enclosing the hull, or
	 *         {@code null} if none
	 */
	public Geometry getTriangle() {
		if (hull.getNumPoints() <= 4) {
			// hull is triangle (or less)
			return hull;
		}
		int a = 1;
		int b = 2;

		double minArea = Double.MAX_VALUE;
		Polygon optimalTriangle = null;

		for (int i = 0; i < n; i++) {
			TriangleForIndex tForIndex = new TriangleForIndex(i, a, b);
			Polygon triangle = tForIndex.triangle;
			a = tForIndex.aOut;
			b = tForIndex.bOut;
			if (triangle != null) {
				double area = triangle.getArea();
				if (optimalTriangle == null || area < minArea) {
					optimalTriangle = triangle;
					minArea = area;
				}
			}
		}

		return optimalTriangle;
	}

	private class TriangleForIndex {

		final int aOut, bOut;
		final Polygon triangle;

		private final Side sideC;
		private Side sideA, sideB;

		TriangleForIndex(int c, int a, int b) {
			a = floorMod(Math.max(a, c + 1), n);
			b = floorMod(Math.max(b, c + 2), n);
			sideC = side(c);

			// Move b onto the right chain
			while (onLeftChain(b)) {
				b = floorMod(b + 1, n);
			}
			// Advance a/b until a and b are high/critical
			while (dist(b, sideC) > dist(a, sideC) + tol) {
				int[] ab = incrementLowHigh(a, b);
				a = ab[0];
				b = ab[1];
			}

			// Advance b until tangency
			while (tangency(a, b)) {
				b = floorMod(b + 1, n);
			}

			// Compute gamma for B
			Coordinate gammaB = gamma(points[b], side(a), sideC);
			if (gammaB == null) {
				// Cannot form a valid candidate for this index due to degeneracy
				this.triangle = null;
				this.aOut = a;
				this.bOut = b;
				return;
			}

			// Decide construction based on low/high and relative distances
			if (low(b, gammaB) || dist(b, sideC) < dist(floorMod(a - 1, n), sideC) - tol) {
				Side tempSideB = side(b);
				Side tempSideA = side(a);

				Coordinate iCB = sideC.intersection(tempSideB);
				Coordinate iAB = tempSideA.intersection(tempSideB);
				if (iCB == null || iAB == null) {
					// Degenerate/parallel configuration
					this.triangle = null;
					this.aOut = a;
					this.bOut = b;
					return;
				}
				sideB = new Side(iCB, iAB);
				sideA = tempSideA;

				if (dist(sideB.midpoint(), sideC) < dist(floorMod(a - 1, n), sideC) - tol) {
					Coordinate gammaA = gamma(points[floorMod(a - 1, n)], sideB, sideC);
					if (gammaA == null) {
						this.triangle = null;
						this.aOut = a;
						this.bOut = b;
						return;
					}
					sideA = new Side(gammaA, points[floorMod(a - 1, n)]);
				}
			} else {
				sideB = new Side(gammaB, points[b]);
				sideA = new Side(gammaB, points[floorMod(a - 1, n)]);
			}

			// Final intersections
			final Coordinate vertexA = sideC.intersection(sideB);
			final Coordinate vertexB = sideC.intersection(sideA);
			final Coordinate vertexC = sideA.intersection(sideB);

			if (!isValidTriangle(vertexA, vertexB, vertexC, a, b, c)) {
				this.triangle = null;
			} else {
				this.triangle = gf.createPolygon(new Coordinate[] { vertexA, vertexB, vertexC, vertexA });
			}

			this.aOut = a;
			this.bOut = b;
		}

		private double dist(int point, Side side) {
			return side.distance(points[floorMod(point, points.length)]);
		}

		private double dist(Coordinate point, Side side) {
			return side.distance(point);
		}

		private Coordinate gamma(Coordinate point, Side on, Side base) {
			Coordinate I = on.intersection(base);
			if (I == null) {
				return null;
			}

			double dxOn = on.p2.x - on.p1.x;
			double dyOn = on.p2.y - on.p1.y;

			double bx = base.p2.x - base.p1.x;
			double by = base.p2.y - base.p1.y;
			double nx = -by, ny = bx; // normal to base
			double nLen = MathUtil.hypot(nx, ny);
			if (nLen == 0) {
				return null;
			}

			// Signed distance of point from base
			double signedP = ((point.x - base.p1.x) * nx + (point.y - base.p1.y) * ny) / nLen;

			// Change in signed distance per unit t along 'on'
			double denom = (dxOn * nx + dyOn * ny) / nLen;

			// Use analytic solution if well-conditioned
			if (Math.abs(denom) > tol) {
				double t = (2.0 * signedP) / denom;
				return new Coordinate(I.x + t * dxOn, I.y + t * dyOn);
			}

			// Fallback: finite-difference step
			double target = 2.0 * Math.abs(signedP);

			if (on.vertical) {
				// move 1 unit along 'on' (vertical)
				double dd = base.distance(new Coordinate(I.x, I.y + 1));
				if (dd <= tol) {
					return null;
				}
				double s = target / dd;
				Coordinate guess = new Coordinate(I.x, I.y + s);
				if (ccw(base.p1, base.p2, guess) != ccw(base.p1, base.p2, point)) {
					guess = new Coordinate(I.x, I.y - s);
				}
				return guess;
			} else {
				// move 1 unit in +x along 'on'
				Coordinate p = on.atX(I.x + 1);
				double dd = base.distance(p);
				if (dd <= tol) {
					return null;
				}
				double s = target / dd;
				Coordinate guess = on.atX(I.x + s);
				if (ccw(base.p1, base.p2, guess) != ccw(base.p1, base.p2, point)) {
					guess = on.atX(I.x - s);
				}
				return guess;
			}
		}

		private boolean onLeftChain(int b) {
			double dNext = dist(floorMod(b + 1, n), sideC);
			double dCurr = dist(b, sideC);
			return dNext >= dCurr - tol;
		}

		private int[] incrementLowHigh(int a, int b) {
			Coordinate gammaA = gamma(points[a], side(a), sideC);
			if (high(b, gammaA)) {
				b = floorMod(b + 1, n);
			} else {
				a = floorMod(a + 1, n);
			}
			return new int[] { a, b };
		}

		private boolean tangency(int a, int b) {
			Coordinate gammaB = gamma(points[b], side(a), sideC);
			if (gammaB == null) {
				return false;
			}
			return dist(b, sideC) > dist(floorMod(a - 1, n), sideC) && high(b, gammaB);
		}

		private boolean ccw(Coordinate a, Coordinate b, Coordinate c) {
			return Orientation.index(a, b, c) == Orientation.COUNTERCLOCKWISE;
		}

		private boolean high(int b, Coordinate gammaB) {
			if (gammaB == null) {
				return false;
			}

			int bm1 = floorMod(b - 1, n);
			int bp1 = floorMod(b + 1, n);

			boolean s1 = ccw(gammaB, points[b], points[bm1]);
			boolean s2 = ccw(gammaB, points[b], points[bp1]);
			if (s1 == s2) {
				return false;
			}

			boolean t1 = ccw(points[bm1], points[bp1], gammaB);
			boolean t2 = ccw(points[bm1], points[bp1], points[b]);

			if (t1 == t2) {
				return dist(gammaB, sideC) > dist(b, sideC);
			} else {
				return false;
			}
		}

		private boolean low(int b, Coordinate gammaB) {
			if (gammaB == null) {
				return false;
			}

			int bm1 = floorMod(b - 1, n);
			int bp1 = floorMod(b + 1, n);

			boolean s1 = ccw(gammaB, points[b], points[bm1]);
			boolean s2 = ccw(gammaB, points[b], points[bp1]);
			if (s1 == s2) {
				return false;
			}

			boolean t1 = ccw(points[bm1], points[bp1], gammaB);
			boolean t2 = ccw(points[bm1], points[bp1], points[b]);

			if (t1 == t2) {
				return false;
			} else {
				return dist(gammaB, sideC) > dist(b, sideC);
			}
		}

		private boolean isValidTriangle(Coordinate vertexA, Coordinate vertexB, Coordinate vertexC, int a, int b, int c) {
			if (vertexA == null || vertexB == null || vertexC == null) {
				return false;
			}
			Coordinate midpointA = midpoint(vertexC, vertexB);
			Coordinate midpointB = midpoint(vertexA, vertexC);
			Coordinate midpointC = midpoint(vertexA, vertexB);
			return (validateMidpoint(midpointA, a) && validateMidpoint(midpointB, b) && validateMidpoint(midpointC, c));
		}

		private boolean validateMidpoint(Coordinate midpoint, int index) {
			Side s = side(index);

			double d = Distance.pointToSegment(midpoint, s.p1, s.p2);
			return d <= tol;
		}

		private Side side(final int i) {
			return new Side(points[floorMod(i - 1, n)], points[i]);
		}

		private Coordinate midpoint(Coordinate a, Coordinate b) {
			return new Coordinate((a.x + b.x) / 2, (a.y + b.y) / 2);
		}
	}

	private class Side {

		final Coordinate p1, p2;
		final double slope, intercept;
		final boolean vertical;

		Side(Coordinate p1, Coordinate p2) {
			this.p1 = p1;
			this.p2 = p2;
			slope = (p2.y - p1.y) / (p2.x - p1.x);
			intercept = p1.y - slope * p1.x;
			vertical = p1.x == p2.x;
		}

		private double sqrDistance(Coordinate p) {
			double numerator = (p2.x - p1.x) * (p1.y - p.y) - (p1.x - p.x) * (p2.y - p1.y);
			numerator *= numerator;
			double denominator = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
			return numerator / denominator;
		}

		private Coordinate atX(double x) {
			if (vertical) {
				return p1; // NOTE return p1 (though incorrect) rather than null
			}
			return new Coordinate(x, slope * x + intercept);
		}

		private double distance(Coordinate p) {
			return Math.sqrt(sqrDistance(p));
		}

		private Coordinate intersection(Side that) {
			Coordinate c = Intersection.intersection(this.p1, this.p2, that.p1, that.p2);
			if (c == null || !Double.isFinite(c.x) || !Double.isFinite(c.y)) {
				return null;
			}
			return c;
		}

		private Coordinate midpoint() {
			return new Coordinate((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
		}
	}
}