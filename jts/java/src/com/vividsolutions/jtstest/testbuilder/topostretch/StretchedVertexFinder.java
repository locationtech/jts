package com.vividsolutions.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

class StretchedVertexFinder 
{
	public static List findNear(Collection linestrings, double tolerance)
	{
		StretchedVertexFinder finder = new StretchedVertexFinder(linestrings, tolerance);
		return finder.getNearVertices();
	}
	
	private Collection linestrings;
	private double tolerance = 0.0;
	private Envelope limitEnv;
	private List nearVerts = new ArrayList();
	
	public StretchedVertexFinder(Collection linestrings, double tolerance)
	{
		this.linestrings = linestrings;
		this.tolerance = tolerance;
	}
	
	public StretchedVertexFinder(Collection linestrings, double tolerance, Envelope limitEnv)
	{
		this.linestrings = linestrings;
		this.tolerance = tolerance;
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
	
	private void findNearVertices(LineString line)
	{
		Coordinate[] pts = line.getCoordinates();
		for (int i = 0; i < pts.length; i++) {
			findNearVertex(pts, i, pts[i]);
//				nearVerts.add(new NearVertex(pts, i));
		}
	}
	
	private void findNearVertex(Coordinate[] linePts, int index, Coordinate p)
	{
		for (Iterator i = linestrings.iterator(); i.hasNext(); ) {
			LineString line = (LineString) i.next();
			findNearVertex(linePts, index, p, line);
		}
		
	}

	private void findNearVertex(Coordinate[] linePts, int index, Coordinate p, LineString testLine)
	{
		Coordinate[] pts = testLine.getCoordinates();
		for (int i = 0; i < pts.length; i++) {

			Coordinate testPt = pts[i];
			
			StretchedVertex nearVert = null;
	
			// is near to vertex?

			double dist = testPt.distance(p);
			if (dist <= tolerance && dist != 0.0) {
				nearVert = new StretchedVertex(p, linePts, i, testPt);
			}
			else if (i < pts.length - 1) {
				// is near segment?
				
				Coordinate segEndPt = pts[i + 1];
				
				/**
				 * Check whether pt is near or equal to other segment endpoint.
				 * If near, it will be handled by the near vertex case code.
				 * If equal, don't record it at all
				 */
				double distToOther = segEndPt.distance(p);
				if (distToOther <= tolerance)
					// will be handled as a point-vertex case
					continue;
				
				// here we know point is not near the segment endpoints
				// check if it is near the segment at all
				double segDist = distanceToSeg(p, testPt, segEndPt);
				if (segDist <= tolerance && segDist != 0.0) {
					nearVert = new StretchedVertex(p, linePts, i, new LineSegment(testPt, pts[i + 1]));
				}
			}
			if (nearVert != null)
				nearVerts.add(nearVert);
		}
	}

	/*
	private boolean isNear(Coordinate pt, Coordinate testPt)
	{
		double dist = testPt.distance(p);
		if (dist <= tolerance && dist != 0.0) {
			nearVert = new NearVertex(p, linePts, i, testPt);
		}
	}
	*/
	
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
