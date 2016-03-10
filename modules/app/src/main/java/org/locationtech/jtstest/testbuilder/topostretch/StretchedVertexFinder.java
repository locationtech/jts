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

package org.locationtech.jtstest.testbuilder.topostretch;

import java.util.*;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

class StretchedVertexFinder 
{
	public static List findNear(Collection linestrings, double tolerance, Envelope mask)
	{
		StretchedVertexFinder finder = new StretchedVertexFinder(linestrings, tolerance, mask);
		return finder.getNearVertices();
	}
	
	private Collection linestrings;
	private double tolerance = 0.0;
	private Envelope limitEnv = null;
	private List nearVerts = new ArrayList();
	
	public StretchedVertexFinder(Collection linestrings, double tolerance)
	{
		this.linestrings = linestrings;
		this.tolerance = tolerance;
	}
	
	public StretchedVertexFinder(Collection linestrings, double tolerance, Envelope limitEnv)
	{
		this(linestrings, tolerance);
		this.limitEnv = limitEnv;
	}
	
	public List getNearVertices()
	{
		findNearVertices();
		return nearVerts;
	}
	
	private void findNearVertices()
	{
		for (Iterator i = linestrings.iterator(); i.hasNext(); ) {
			LineString line = (LineString) i.next();
			findNearVertices(line);
		}
		
	}
	
  private static int geomPointsLen(Coordinate[] pts)
  {
    int n = pts.length;
    // don't process the last point of a ring twice
    if (CoordinateArrays.isRing(pts))
      n = pts.length - 1;
    return n;
  }
  
	private void findNearVertices(LineString targetLine)
	{
		Coordinate[] pts = targetLine.getCoordinates();
    // don't process the last point of a ring twice
    int n = geomPointsLen(pts);
		for (int i = 0; i < n; i++) {
      if (limitEnv.intersects(pts[i]))
        findNearVertex(pts, i);
		}
	}
	
	private void findNearVertex(Coordinate[] linePts, int index)
	{
		for (Iterator i = linestrings.iterator(); i.hasNext(); ) {
			LineString testLine = (LineString) i.next();
			findNearVertex(linePts, index, testLine);
		}
	}

  /**
   * Finds a single near vertex.
   * This is simply the first one found, not necessarily 
   * the nearest.  
   * This choice may sub-optimal, resulting 
   * in odd result geometry.
   * It's not clear that this can be done better, however.
   * If there are several near points, the stretched
   * geometry is likely to be distorted anyway.
   * 
   * @param targetPts
   * @param index
   * @param testLine
   */
	private void findNearVertex(Coordinate[] targetPts, int index, LineString testLine)
	{
    Coordinate targetPt = targetPts[index];
		Coordinate[] testPts = testLine.getCoordinates();
    // don't process the last point of a ring twice
    int n = geomPointsLen(testPts);
		for (int i = 0; i < n; i++) {
			Coordinate testPt = testPts[i];
      
			StretchedVertex stretchVert = null;
	
			// is near to vertex?
			double dist = testPt.distance(targetPt);
			if (dist <= tolerance && dist != 0.0) {
				stretchVert = new StretchedVertex(targetPt, targetPts, index, testPt, testPts, i);
			}
      // is near segment?
			else if (i < testPts.length - 1) {
				Coordinate segEndPt = testPts[i + 1];
				
				/**
				 * Check whether pt is near or equal to other segment endpoint.
				 * If near, it will be handled by the near vertex case code.
				 * If equal, don't record it at all
				 */
				double distToOther = segEndPt.distance(targetPt);
				if (distToOther <= tolerance)
					// will be handled as a point-vertex case
					continue;
				
				// Here we know point is not near the segment endpoints.
				// Check if it is near the segment at all.
				if (isPointNearButNotOnSeg(targetPt, testPt, segEndPt, tolerance)) {
					stretchVert = new StretchedVertex(targetPt, targetPts, i, new LineSegment(testPt, testPts[i + 1]));
				}
			}
			if (stretchVert != null)
				nearVerts.add(stretchVert);
		}
	}
	
  private static boolean contains(Envelope env, Coordinate p0, Coordinate p1)
  {
    if (! env.contains(p0)) return false;
    if (! env.contains(p1)) return false;
    return true;
  }
  
	private static boolean isPointNearButNotOnSeg(Coordinate p, Coordinate p0, Coordinate p1, double distTol)
	{
		// don't rely on segment distance algorithm to correctly compute zero distance
		// on segment
		if (CGAlgorithms.computeOrientation(p0, p1, p) == CGAlgorithms.COLLINEAR)
			return false;

		// compute actual distance
		distSeg.p0 = p0;
		distSeg.p1 = p1;
		double segDist = distSeg.distance(p);
		if (segDist > distTol)
			return false;
		return true;
	}

	private static LineSegment distSeg = new LineSegment();
	
	private static double distanceToSeg(Coordinate p, Coordinate p0, Coordinate p1)
	{
		distSeg.p0 = p0;
		distSeg.p1 = p1;
		double segDist = distSeg.distance(p);
		
		// robust calculation of zero distance
		if (CGAlgorithms.computeOrientation(p0, p1, p) == CGAlgorithms.COLLINEAR)
			segDist = 0.0;
		
		return segDist;
	}
}
