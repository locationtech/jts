package com.vividsolutions.jtstest.testbuilder.topostretch;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

/**
 * Models a vertex of a Geometry which will be stretched
 * due to being too near other segments and vertices.
 * <p>
 * Currently for simplicity a vertex is assumed to 
 * be near only one segment or other vertex.
 * This is sufficient for most cases.
 * 
 * @author Martin Davis
 *
 */
public class StretchedVertex 
{
	// TODO: also provide information about the segments around the facet the vertex is near to, to allow smarter adjustment 
	
	private Coordinate vertexPt;
	private Coordinate[] parentLine;
	private int parentIndex;
	private Coordinate nearPt = null;
	private LineSegment nearSeg = null;
	private Coordinate stretchedPt = null; 
	
	/**
	 * Creates a NearVertex for a point which lies near a vertex
	 * @param vertexPt
	 * @param parentLine
	 * @param index
	 * @param nearPt
	 */
	public StretchedVertex(Coordinate vertexPt, Coordinate[] parentLine, int parentIndex, Coordinate nearPt)
	{
		this.vertexPt = vertexPt;
		this.parentLine = parentLine;
		this.parentIndex = parentIndex;
		this.nearPt = nearPt;
	}
	
	/**
	 * Creates a NearVertex for a point which lies near a line segment
	 * @param vertexPt
	 * @param parentLine
	 * @param index
	 * @param nearSeg
	 */
	public StretchedVertex(Coordinate vertexPt, Coordinate[] parentLine, int parentIndex, 
			LineSegment nearSeg)
	{
		this.vertexPt = vertexPt;
		this.parentLine = parentLine;
		this.parentIndex = parentIndex;
		this.nearSeg = nearSeg;
	}
	
	public Coordinate getVertexCoordinate()
	{
		return vertexPt;
	}
	
	/**
	 * Gets the point which this near vertex will be stretched to
	 * (by a given distance)
	 * 
	 * @param dist the distance to adjust the point by 
	 * @return the stretched coordinate
	 */
	public Coordinate getStretchedVertex(double dist)
	{
		if (stretchedPt != null) 
			return stretchedPt;
		
		if (nearPt != null) {
			stretchedPt = getDisplacedFromPoint(dist);
			// displace in direction of segment this pt lies on
		}
		else {
			stretchedPt = getDisplacedFromSeg(dist);
		}
		return stretchedPt;
	}
	
	private Coordinate getDisplacedFromPoint(double dist)
	{
		LineSegment seg = new LineSegment(nearPt, vertexPt);
		
		// compute an adjustment which displaces in the direction of the nearPt-vertexPt vector
		// TODO: make this robust!
		double len = seg.getLength();
		double frac = (dist + len) / len;
		Coordinate strPt = seg.pointAlong(frac);
		return strPt;
	}
	
	private Coordinate getDisplacedFromSeg(double dist)
	{
		double frac = nearSeg.projectionFactor(vertexPt);
		
		// displace away from the segment on the same side as the original point
		int side = nearSeg.orientationIndex(vertexPt);
		if (side == CGAlgorithms.RIGHT)
			dist = -dist;
		
		return nearSeg.pointAlongOffset(frac, dist);
	}
}
