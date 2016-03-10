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

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.MathUtil;
import org.locationtech.jts.math.Vector2D;

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
	
  private boolean isNearRing()
  {
    return CoordinateArrays.isRing(nearPts);
  }
  
  private Coordinate getNearRingPoint(int i)
  {
    int index = i;
    if (i < 0) 
      index = i + nearPts.length -1; 
    else if (i >= nearPts.length - 1) 
      index = i - (nearPts.length - 1); 
    return nearPts[index];
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
    // handle linestring endpoints - do simple displacement
    if (! isNearRing() 
        && nearIndex == 0 || nearIndex >= nearPts.length -1) {
      return displaceFromPoint(nearPt, dist);
    }
      
    // analyze corner to see how to displace the vertex
    // find corner points
    Coordinate p1 = getNearRingPoint(nearIndex - 1);
    Coordinate p2 = getNearRingPoint(nearIndex + 1);
    
    // if vertexPt is identical to an arm of the corner, just displace the point
    if (p1.equals2D(vertexPt) || p2.equals2D(vertexPt))
      return displaceFromPoint(nearPt, dist);
    
    return displaceFromCornerAwayFromArms(nearPt, p1, p2, dist);
  }
  
  private Coordinate displaceFromCornerOriginal(Coordinate nearPt, Coordinate p1, Coordinate p2, double dist)
  {
    // if corner is nearly flat, just displace point
    // TODO: displace from vertex on appropriate side of flat line, with suitable angle
    if (isFlat(nearPt, p1, p2))
      return displaceFromFlatCorner(nearPt, p1, p2, dist);

    Coordinate[] corner = orientCorner(nearPt, p1, p2);
    
    // find quadrant of corner that vertex pt lies in
    int quadrant = quadrant(vertexPt, nearPt, corner);
    
    Vector2D normOffset = normalizedOffset(nearPt, p1, p2);
    Vector2D baseOffset = normOffset.multiply(dist);
    Vector2D rotatedOffset = baseOffset.rotateByQuarterCircle(quadrant);
    
    return rotatedOffset.translate(vertexPt);
    //return null;
  }

  private Coordinate displaceFromCorner(Coordinate nearPt, Coordinate p1, Coordinate p2, double dist)
  {
    Coordinate[] corner = orientCorner(nearPt, p1, p2);
      // compute perpendicular bisector of p1-p2
    Vector2D u1 = Vector2D.create(nearPt, corner[0]).normalize();
    Vector2D u2 = Vector2D.create(nearPt, corner[1]).normalize();
    double ang = u1.angle(u2);
    Vector2D innerBisec = u2.rotate(ang / 2);
        Vector2D offset = innerBisec.multiply(dist);
    if (! isInsideCorner(vertexPt, nearPt, corner[0], corner[1])) {
        offset = offset.multiply(-1);
    }
    return offset.translate(vertexPt);
  }

  private static final double MAX_ARM_NEARNESS_ANG = 20.0 / 180.0 * Math.PI;
  
  private static double maxAngleToBisector(double ang)
  {
    double relAng = ang / 2 - MAX_ARM_NEARNESS_ANG;
    if (relAng < 0) return 0;
    return relAng;
  }
  
  /**
   * Displaces a vertex from a corner,
   * with angle limiting
   * used to ensure that the displacement is not close to the arms of the corner.
   * 
   * @param nearPt
   * @param p1
   * @param p2
   * @param dist
   * @return
   */
  private Coordinate displaceFromCornerAwayFromArms(Coordinate nearPt, Coordinate p1, Coordinate p2, double dist)
  {
    Coordinate[] corner = orientCorner(nearPt, p1, p2);
    boolean isInsideCorner = isInsideCorner(vertexPt, nearPt, corner[0], corner[1]);
    
    Vector2D u1 = Vector2D.create(nearPt, corner[0]).normalize();
    Vector2D u2 = Vector2D.create(nearPt, corner[1]).normalize();
    double cornerAng = u1.angle(u2);
    
    double maxAngToBisec = maxAngleToBisector(cornerAng);
    
    Vector2D bisec = u2.rotate(cornerAng / 2);
    if (! isInsideCorner) {
      bisec = bisec.multiply(-1);
      double outerAng = 2 * Math.PI - cornerAng;
      maxAngToBisec = maxAngleToBisector(outerAng);
    }
    
    Vector2D pointwiseDisplacement = Vector2D.create(nearPt, vertexPt).normalize();
    double stretchAng = pointwiseDisplacement.angleTo(bisec);
    double stretchAngClamp = MathUtil.clamp(stretchAng, -maxAngToBisec, maxAngToBisec);
    Vector2D cornerDisplacement = bisec.rotate(-stretchAngClamp).multiply(dist);
 
    return cornerDisplacement.translate(vertexPt);
  }

  private boolean isInsideCorner(Coordinate queryPt, Coordinate base, Coordinate p1, Coordinate p2)
  {
      return CGAlgorithms.orientationIndex(base, p1, queryPt) == CGAlgorithms.CLOCKWISE
              && CGAlgorithms.orientationIndex(base, p2, queryPt) == CGAlgorithms.COUNTERCLOCKWISE;
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
  private static Vector2D normalizedOffset(Coordinate p0, Coordinate p1, Coordinate p2)
  {
  	Vector2D u1 = Vector2D.create(p0, p1).normalize();
  	Vector2D u2 = Vector2D.create(p0, p2).normalize();
  	Vector2D offset = u1.add(u2).normalize();
    return offset;
  }
  
  private Coordinate displaceFromFlatCorner(Coordinate nearPt, Coordinate p1, Coordinate p2, double dist)
  {
  	// compute perpendicular bisector of p1-p2
  	Vector2D bisecVec = Vector2D.create(p2, p1).rotateByQuarterCircle(1);
  	Vector2D offset = bisecVec.normalize().multiply(dist);
    return offset.translate(vertexPt);
  }
}
