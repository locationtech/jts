/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.distance3d;

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Plane3D;
import org.locationtech.jts.math.Vector3D;

/**
 * Models a polygon lying in a plane in 3-dimensional Cartesian space.
 * The polygon representation is supplied
 * by a {@link Polygon},
 * containing coordinates with XYZ ordinates.
 * 3D polygons are assumed to lie in a single plane.
 * The plane best fitting the polygon coordinates is
 * computed and is represented by a {@link Plane3D}.
 * 
 * @author mdavis
 *
 */
public class PlanarPolygon3D {

	private Plane3D plane;
	private Polygon poly;
	private int facingPlane = -1;

	public PlanarPolygon3D(Polygon poly) {
		this.poly = poly;
		plane = findBestFitPlane(poly);
		facingPlane = plane.closestAxisPlane();
	}

	/**
	 * Finds a best-fit plane for the polygon, 
	 * by sampling a few points from the exterior ring.
	 * <p>
	 * The algorithm used is Newell's algorithm:
	 * - a base point for the plane is determined from the average of all vertices
	 * - the normal vector is determined by
	 *   computing the area of the projections on each of the axis planes
	 * 
	 * @param poly the polygon to determine the plane for
	 * @return the best-fit plane
	 */
	private Plane3D findBestFitPlane(Polygon poly) 
	{
		CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
		Coordinate basePt = averagePoint(seq);
		Vector3D normal = averageNormal(seq);
		return new Plane3D(normal, basePt);
	}

	/**
	 * Computes an average normal vector from a list of polygon coordinates.
	 * Uses Newell's method, which is based
	 * on the fact that the vector with components
	 * equal to the areas of the projection of the polygon onto 
	 * the Cartesian axis planes is normal.
	 * 
	 * @param seq the sequence of coordinates for the polygon
	 * @return a normal vector
	 */
	private Vector3D averageNormal(CoordinateSequence seq) 
	{
		int n = seq.size();
		Coordinate sum = new Coordinate(0,0,0);
		Coordinate p1 = new Coordinate(0,0,0);
		Coordinate p2 = new Coordinate(0,0,0);
		for (int i = 0; i < n - 1; i++) {
			seq.getCoordinate(i, p1);
			seq.getCoordinate(i+1, p2);
			sum.x += (p1.y - p2.y)*(p1.z + p2.z);
			sum.y += (p1.z - p2.z)*(p1.x + p2.x);
			sum.z += (p1.x - p2.x)*(p1.y + p2.y);
		}
		sum.x /= n;
		sum.y /= n;
		sum.z /= n;
		Vector3D norm = Vector3D.create(sum).normalize();
		return norm;
	}

	/**
	 * Computes a point which is the average of all coordinates
	 * in a sequence.
	 * If the sequence lies in a single plane,
	 * the computed point also lies in the plane.
	 * 
	 * @param seq a coordinate sequence
	 * @return a Coordinate with averaged ordinates
	 */
	private Coordinate averagePoint(CoordinateSequence seq) {
		Coordinate a = new Coordinate(0,0,0);
		int n = seq.size();
		for (int i = 0; i < n; i++) {
			a.x += seq.getOrdinate(i, CoordinateSequence.X);
			a.y += seq.getOrdinate(i, CoordinateSequence.Y);
			a.z += seq.getOrdinate(i, CoordinateSequence.Z);
		}
		a.x /= n;
		a.y /= n;
		a.z /= n;
		return a;
	}

	public Plane3D getPlane() {
		return plane;
	}

	public Polygon getPolygon() {
		return poly;
	}

	public boolean intersects(Coordinate intPt) {
		if (Location.EXTERIOR == locate(intPt, poly.getExteriorRing()))
			return false;
		
		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			if (Location.INTERIOR == locate(intPt, poly.getInteriorRingN(i)))
				return false;
		}
		return true;
	}

	private int locate(Coordinate pt, LineString ring) {
		CoordinateSequence seq = ring.getCoordinateSequence();
		CoordinateSequence seqProj = project(seq, facingPlane);
		Coordinate ptProj = project(pt, facingPlane);
		return RayCrossingCounter.locatePointInRing(ptProj, seqProj);
	}
	
	public boolean intersects(Coordinate pt, LineString ring) {
		CoordinateSequence seq = ring.getCoordinateSequence();
		CoordinateSequence seqProj = project(seq, facingPlane);
		Coordinate ptProj = project(pt, facingPlane);
		return Location.EXTERIOR != RayCrossingCounter.locatePointInRing(ptProj, seqProj);
	}
	
	private static CoordinateSequence project(CoordinateSequence seq, int facingPlane)
	{
		switch (facingPlane) {
		case Plane3D.XY_PLANE: return AxisPlaneCoordinateSequence.projectToXY(seq);
		case Plane3D.XZ_PLANE: return AxisPlaneCoordinateSequence.projectToXZ(seq);
		default: return AxisPlaneCoordinateSequence.projectToYZ(seq);
		}
	}
	
	private static Coordinate project(Coordinate p, int facingPlane)
	{
		switch (facingPlane) {
		case Plane3D.XY_PLANE: return new Coordinate(p.x, p.y);
		case Plane3D.XZ_PLANE: return new Coordinate(p.x, p.z);
		// Plane3D.YZ
		default: return new Coordinate(p.y, p.z);
		}
	}
	

}
