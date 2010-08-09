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
  private Coordinate[] nearPts = null;
  private int nearIndex = -1;
	private LineSegment nearSeg = null;
	private Coordinate stretchedPt = null; 
	
	/**
	 * Creates a vertex which lies near a vertex
	 * @param vertexPt
	 * @param parentLine
	 * @param index
	 * @param nearPt
	 */
	public StretchedVertex(Coordinate vertexPt, Coordinate[] parentLine, int parentIndex, 
      Coordinate nearPt, Coordinate[] nearPts, int nearIndex)
	{
		this.vertexPt = vertexPt;
		this.parentLine = parentLine;
		this.parentIndex = parentIndex;
		this.nearPt = nearPt;
    this.nearPts = nearPts;
    this.nearIndex = nearIndex;
	}
	
	/**
	 * Creates a vertex for a point which lies near a line segment
	 * @param vertexPt
	 * @param parentLine
	 * @param parentIndex
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
			stretchedPt = displaceFromPoint(nearPt, dist);
			// displace in direction of segment this pt lies on
		}
		else {
			stretchedPt = displaceFromSeg(nearSeg, dist);
		}
		return stretchedPt;
	}
	
	private Coordinate displaceFromPoint(Coordinate nearPt, double dist)
	{
		LineSegment seg = new LineSegment(nearPt, vertexPt);
		
		// compute an adjustment which displaces in the direction of the nearPt-vertexPt vector
		// TODO: make this robust!
		double len = seg.getLength();
		double frac = (dist + len) / len;
		Coordinate strPt = seg.pointAlong(frac);
		return strPt;
	}
	
	private Coordinate displaceFromSeg(LineSegment nearSeg, double dist)
	{
		double frac = nearSeg.projectionFactor(vertexPt);
		
		// displace away from the segment on the same side as the original point
		int side = nearSeg.orientationIndex(vertexPt);
		if (side == CGAlgorithms.RIGHT)
			dist = -dist;
		
		return nearSeg.pointAlongOffset(frac, dist);
	}
  
  private Coordinate displaceFromVertex(Coordinate nearPt, double dist)
  {
    // TODO: handle case of rings
    if (nearIndex == 0 || nearIndex >= nearPts.length -1)
      return displaceFromPoint(nearPt, dist);
    
    // analyze corner to see how to displace the vertex
    // find corner points
    Coordinate p1 = nearPts[nearIndex - 1];
    Coordinate p2 = nearPts[nearIndex + 1];
    Coordinate[] cornerPt = normalizeCorner(nearPt, p1, p2);
    return null;
    
  }
  
  /**
   * Returns an array of pts such that p0 - p[0] - [p1] is CW.
   * 
   * @param p0
   * @param p1
   * @param p2
   * @return
   */
  private static Coordinate[] normalizeCorner(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    if (CGAlgorithms.CLOCKWISE == CGAlgorithms.computeOrientation(p0, p1, p2)) {
      return new Coordinate[] { p1, p2 };
    }
    return new Coordinate[] { p2, p1 };
  }
  

}
