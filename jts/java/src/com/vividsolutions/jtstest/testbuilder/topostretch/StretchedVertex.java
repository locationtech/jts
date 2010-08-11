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
			stretchedPt = displaceFromVertex(nearPt, dist);
//			stretchedPt = displaceFromPoint(nearPt, dist);
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
  	// in the meantime, just do something simple
    if (nearIndex == 0 || nearIndex >= nearPts.length -1)
      return displaceFromPoint(nearPt, dist);
    
    
    // analyze corner to see how to displace the vertex
    // find corner points
    Coordinate p1 = nearPts[nearIndex - 1];
    Coordinate p2 = nearPts[nearIndex + 1];
    
    // if vertexPt is identical to an arm of the corner, just displace the point
    if (p1.equals2D(vertexPt) || p2.equals2D(vertexPt))
      return displaceFromPoint(nearPt, dist);
    
    // if corner is nearly flat, just displace point
    // TODO: displace from vertex on appropriate side of flat line, with suitable angle
    if (isFlat(nearPt, p1, p2))
      return displaceFromFlatCorner(nearPt, p1, p2, dist);

    Coordinate[] corner = orientCorner(nearPt, p1, p2);
    
    // find quadrant of corner that vertex pt lies in
    int quadrant = quadrant(vertexPt, nearPt, corner);
    
    Coordinate normOffset = normalizedOffset(nearPt, p1, p2);
    Coordinate baseOffset = VectorMath.multiply(dist, normOffset);
    Coordinate rotatedOffset = rotateToQuadrant(baseOffset, quadrant);
    
    return VectorMath.sum(vertexPt, rotatedOffset);
    //return null;
    
  }
  
  private static final double POINT_LINE_FLATNESS_RATIO = 0.01;
  
  private static boolean isFlat(Coordinate p, Coordinate p1, Coordinate p2)
  {
  	double dist = CGAlgorithms.distancePointLine(p, p1, p2);
  	double len = p1.distance(p2);
  	if (dist/len < POINT_LINE_FLATNESS_RATIO)
  		return true;
  	return false;
  }
  
  /**
   * 
   * @param pt
   * @param cornerBase the two vertices defining the 
   * @param corner the two vertices defining the arms of the corner, oriented CW
   * @return the quadrant the pt lies in
   */
  private static int quadrant(Coordinate pt, Coordinate cornerBase, Coordinate[] corner)
  {
  	if (CGAlgorithms.orientationIndex(cornerBase, corner[0], pt) == CGAlgorithms.CLOCKWISE) {
  		if (CGAlgorithms.orientationIndex(cornerBase, corner[1], pt) == CGAlgorithms.COUNTERCLOCKWISE) {
  			return 0;
  		}
  		else
  			return 3;
  	}
  	else {
  		if (CGAlgorithms.orientationIndex(cornerBase, corner[1], pt) == CGAlgorithms.COUNTERCLOCKWISE) {
  			return 1;
  		}
  		else
  			return 2; 		
  	}
  }
  
  private static Coordinate rotateToQuadrant(Coordinate v, int quadrant)
  {
  	switch (quadrant) {
  	case 0: return v;
  	case 1: return new Coordinate(-v.y, v.x);
  	case 2: return new Coordinate(-v.x, -v.y);
  	case 3: return new Coordinate(v.y, -v.x);
  	}
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
  private static Coordinate[] orientCorner(Coordinate p0, Coordinate p1, Coordinate p2)
  {
  	Coordinate[] orient;
  	// TODO: not sure if determining orientation is necessary?
    if (CGAlgorithms.CLOCKWISE == CGAlgorithms.orientationIndex(p0, p1, p2)) {
    	orient = new Coordinate[] {p1, p2 };
    }
    else {
    	orient = new Coordinate[] { p2, p1 };
    }
    
    return orient;
  }
  
  /**
   * Returns an array of pts such that p0 - p[0] - [p1] is CW.
   * 
   * @param p0
   * @param p1
   * @param p2
   * @return
   */
  private static Coordinate normalizedOffset(Coordinate p0, Coordinate p1, Coordinate p2)
  {
  	Coordinate u1 = VectorMath.normalize(VectorMath.difference(p1, p0));
  	Coordinate u2 = VectorMath.normalize(VectorMath.difference(p2, p0));
    Coordinate offset = VectorMath.normalize(VectorMath.average(u1, u2));
    return offset;
  }
  
  private Coordinate displaceFromFlatCorner(Coordinate nearPt, Coordinate p1, Coordinate p2, double dist)
  {
  	// compute perpendicular bisector of p1-p2
  	Coordinate bisecVec = VectorMath.rotateByQuarterCircle(VectorMath.difference(p1, p2), 1);
  	Coordinate offset = VectorMath.multiply(dist, VectorMath.normalize(bisecVec));
    return VectorMath.sum(vertexPt, offset);
  }
}
