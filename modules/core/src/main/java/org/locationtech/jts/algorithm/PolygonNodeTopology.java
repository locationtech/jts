/*
 * Copyright (c) 2021 Martin Davis.
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Quadrant;

/**
 * Functions to compute topological information 
 * about nodes (ring intersections) in polygonal geometry.
 * 
 * @author mdavis
 *
 */
public class PolygonNodeTopology 
{
  /**
   * Check if four segments at a node cross.
   * Typically the segments lie in two different rings, or different sections of one ring.
   * The node is topologically valid if the rings do not cross.
   * If any segments are collinear, the test returns false.
   *  
   * @param nodePt the node location
   * @param a0 the previous segment endpoint in a ring
   * @param a1 the next segment endpoint in a ring
   * @param b0 the previous segment endpoint in the other ring
   * @param b1 the next segment endpoint in the other ring
   * @return true if the rings cross at the node
   */
  public static boolean isCrossing(Coordinate nodePt, Coordinate a0, Coordinate a1, Coordinate b0, Coordinate b1) {
    Coordinate aLo = a0;
    Coordinate aHi = a1;
    if (isAngleGreater(nodePt, aLo, aHi)) {
      aLo = a1;
      aHi = a0;
    }
    /*
    boolean isBetween0 = isBetween(nodePt, b0, aLo, aHi);
    boolean isBetween1 = isBetween(nodePt, b1, aLo, aHi);
    
    return isBetween0 != isBetween1;
    */
    
    /**
     * Find positions of b0 and b1.  
     * The edges cross if the positions are different.
     * If any edge is collinear they are reported as not crossing
     */
    int compBetween0 = compareBetween(nodePt, b0, aLo, aHi);
    if (compBetween0 == 0) return false;
    int compBetween1 = compareBetween(nodePt, b1, aLo, aHi);
    if (compBetween1 == 0) return false;
    
    return compBetween0 != compBetween1;
  }

  /**
   * Tests whether an segment node-b lies in the interior or exterior
   * of a corner of a ring formed by the two segments a0-node-a1.
   * The ring interior is assumed to be on the right of the corner 
   * (i.e. a CW shell or CCW hole).
   * The test segment must not be collinear with the corner segments.
   * 
   * @param nodePt the node location
   * @param a0 the first vertex of the corner
   * @param a1 the second vertex of the corner
   * @param b the other vertex of the test segment
   * @return true if the segment is interior to the ring corner
   */
  public static boolean isInteriorSegment(Coordinate nodePt, Coordinate a0, Coordinate a1, Coordinate b) {
    Coordinate aLo = a0;
    Coordinate aHi = a1;
    boolean isInteriorBetween = true;
    if (isAngleGreater(nodePt, aLo, aHi)) {
      aLo = a1;
      aHi = a0;
      isInteriorBetween = false;
    }
    boolean isBetween = isBetween(nodePt, b, aLo, aHi);
    boolean isInterior = (isBetween && isInteriorBetween)
        || (! isBetween && ! isInteriorBetween);
    return isInterior;
  }
  
  /**
   * Tests if an edge p is between edges e0 and e1,
   * where the edges all originate at a common origin.
   * The "inside" of e0 and e1 is the arc which does not include the origin.
   * The edges are assumed to be distinct (non-collinear).
   * 
   * @param origin the origin
   * @param p the destination point of edge p
   * @param e0 the destination point of edge e0
   * @param e1 the destination point of edge e1
   * @return true if p is between e0 and e1
   */
  private static boolean isBetween(Coordinate origin, Coordinate p, Coordinate e0, Coordinate e1) {
    boolean isGreater0 = isAngleGreater(origin, p, e0);
    if (! isGreater0) return false;
    boolean isGreater1 = isAngleGreater(origin, p, e1);
    return ! isGreater1;
  }

  /**
   * Compares whether an edge p is between or outside the edges e0 and e1,
   * where the edges all originate at a common origin.
   * The "inside" of e0 and e1 is the arc which does not include 
   * the positive X-axis at the origin.
   * If p is collinear with an edge 0 is returned.
   * 
   * @param origin the origin
   * @param p the destination point of edge p
   * @param e0 the destination point of edge e0
   * @param e1 the destination point of edge e1
   * @return a negative integer, zero or positive integer as the vector P lies outside, collinear with, or inside the vectors E0 and E1
   */
  private static int compareBetween(Coordinate origin, Coordinate p, Coordinate e0, Coordinate e1) {
    int comp0 = compareAngle(origin, p, e0);
    if (comp0 == 0) return 0;
    int comp1 = compareAngle(origin, p, e1);
    if (comp1 == 0) return 0;
    if (comp0 > 0 && comp1 < 0) return 1;
    return -1;
  }
  
  /**
   * Tests if the angle with the origin of a vector P is greater than that of the
   * vector Q.
   * 
   * @param origin the origin of the vectors
   * @param p the endpoint of the vector P
   * @param q the endpoint of the vector Q
   * @return true if vector P has angle greater than Q
   */
  private static boolean isAngleGreater(Coordinate origin, Coordinate p, Coordinate q) {      
    int quadrantP = quadrant(origin, p);
    int quadrantQ = quadrant(origin, q);

    /**
     * If the vectors are in different quadrants, 
     * that determines the ordering
     */
    if (quadrantP > quadrantQ) return true;
    if (quadrantP < quadrantQ) return false;
    
    //--- vectors are in the same quadrant
    // Check relative orientation of vectors
    // P > Q if it is CCW of Q
    int orient = Orientation.index(origin, q, p);
    return orient == Orientation.COUNTERCLOCKWISE;
  }

  /**
   * Compares the angles of two vectors 
   * relative to the positive X-axis at their origin.
   * 
   * @param origin the origin of the vectors
   * @param p the endpoint of the vector P
   * @param q the endpoint of the vector Q
   * @return a negative integer, zero, or a positive integer as this vector P has angle less than, equal to, or greater than vector Q
   */
  public static int compareAngle(Coordinate origin, Coordinate p, Coordinate q) {      
    int quadrantP = quadrant(origin, p);
    int quadrantQ = quadrant(origin, q);

    /**
     * If the vectors are in different quadrants, 
     * that determines the ordering
     */
    if (quadrantP > quadrantQ) return 1;
    if (quadrantP < quadrantQ) return -1;
    
    //--- vectors are in the same quadrant
    // Check relative orientation of vectors
    // P > Q if it is CCW of Q
    int orient = Orientation.index(origin, q, p);
    switch (orient) {
    case Orientation.COUNTERCLOCKWISE: return 1;
    case Orientation.CLOCKWISE: return -1;
    default: return 0;
    }
  }
  
  private static int quadrant(Coordinate origin, Coordinate p) {
    double dx = p.getX() - origin.getX();
    double dy = p.getY() - origin.getY();
    return Quadrant.quadrant(dx,  dy);
  }

}
