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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;


/**
 * Computes an approximate intersection of two line segments
 * by taking the most central of the endpoints of the segments.
 * This is effective in cases where the segments are nearly parallel
 * and should intersect at an endpoint.
 * It is also a reasonable strategy for cases where the 
 * endpoint of one segment lies on or almost on the interior of another one.
 * Taking the most central endpoint ensures that the computed intersection
 * point lies in the envelope of the segments.
 * Also, by always returning one of the input points, this should result 
 * in reducing segment fragmentation.
 * Intended to be used as a last resort for 
 * computing ill-conditioned intersection situations which 
 * cause other methods to fail.
 * <p>
 * WARNING: in some cases this algorithm makes a poor choice of endpoint.
 * It has been replaced by a better heuristic in {@link RobustLineIntersector}.
 *  
 * @author Martin Davis
 * @version 1.8
 * @deprecated
 */
public class CentralEndpointIntersector 
{
	public static Coordinate getIntersection(Coordinate p00, Coordinate p01,
			Coordinate p10, Coordinate p11)
	{
		CentralEndpointIntersector intor = new CentralEndpointIntersector(p00, p01, p10, p11);
		return intor.getIntersection();
	}
	
	private Coordinate[] pts;
	private Coordinate intPt = null;

	public CentralEndpointIntersector(Coordinate p00, Coordinate p01,
			Coordinate p10, Coordinate p11) 
	{
		pts = new Coordinate[] { p00, p01, p10, p11 };
		compute();
	}

	private void Ocompute() 
	{
		Coordinate centroid = average(pts);
		intPt = new Coordinate(findNearestPoint(centroid, pts));
	}

	public Coordinate getIntersection() {
		return intPt;
	}

	private static Coordinate average(Coordinate[] pts)
	{
		Coordinate avg = new Coordinate();
		int n = pts.length;
		for (int i = 0; i < pts.length; i++) {
			avg.x += pts[i].x;
			avg.y += pts[i].y;
		}
		if (n > 0) {
			avg.x /= n;
			avg.y /= n;
		}
		return avg;
	}
	
  /**
   * Determines a point closest to the given point.
   * 
   * @param p the point to compare against
   * @param p1 a potential result point
   * @param p2 a potential result point
   * @param q1 a potential result point
   * @param q2 a potential result point
   * @return the point closest to the input point p
   */
  private Coordinate findNearestPoint(Coordinate p, Coordinate[] pts)
  {
  	double minDist = Double.MAX_VALUE;
  	Coordinate result = null;
  	for (int i = 0; i < pts.length; i++) {
  		double dist = p.distance(pts[i]);
  		// always initialize the result
  		if (i == 0 || dist < minDist) {
  			minDist = dist;
  			result = pts[i];
  		}
  	}
  	return result;
  }
  
  	private double minDist = Double.MAX_VALUE;
  	
  	/**
  	 * Finds point with smallest distance to other segment
  	 */
	private void compute() 
	{
		tryDist(pts[0], pts[2], pts[3]);
		tryDist(pts[1], pts[2], pts[3]);
		tryDist(pts[2], pts[0], pts[1]);
		tryDist(pts[3], pts[0], pts[1]);
	}

	private void tryDist(Coordinate p, Coordinate p0, Coordinate p1) 
	{
		double dist = CGAlgorithms.distancePointLine(p, p0, p1);
		if (dist < minDist) {
			minDist = dist;
			intPt = p;
		}
	}



}
