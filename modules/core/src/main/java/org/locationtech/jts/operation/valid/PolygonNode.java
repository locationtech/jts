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
package org.locationtech.jts.operation.valid;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Quadrant;

/**
 * Functions to compute topological information 
 * about nodes (ring intersections) in polygonal geometry.
 * 
 * @author mdavis
 *
 */
class PolygonNode 
{
  /**
   * Check if the edges at a node between two rings (or one ring) cross.
   * The node is topologically valid if the ring edges do not cross.
   * This function assumes that the edges are not collinear. 
   *  
   * @param nodePt the node location
   * @param a0 the previous edge endpoint in a ring
   * @param a1 the next edge endpoint in a ring
   * @param b0 the previous edge endpoint in the other ring
   * @param b1 the next edge endpoint in the other ring
   * @return true if the edges cross at the node
   */
  public static boolean isCrossing(Coordinate nodePt, Coordinate a0, Coordinate a1, Coordinate b0, Coordinate b1) {
    Coordinate aLo = a0;
    Coordinate aHi = a1;
    if (isAngleGreater(nodePt, aLo, aHi)) {
      aLo = a1;
      aHi = a0;
    }
    /**
     * Find positions of b0 and b1.  
     * If they are the same they do not cross the other edge
     */
    boolean isBetween0 = isBetween(nodePt, b0, aLo, aHi);
    boolean isBetween1 = isBetween(nodePt, b1, aLo, aHi);
    
    return isBetween0 != isBetween1;
  }

  /**
   * Tests whether an edge node-b lies in the interior or exterior
   * of a corner of a ring given by a0-node-a1.
   * The ring interior is assumed to be on the right of the corner (a CW ring).
   * The edge must not be collinear with the corner segments.
   * 
   * @param nodePt the node location
   * @param a0 the first vertex of the corner
   * @param a1 the second vertex of the corner
   * @param b the destination vertex of the edge
   * @return true if the edge is interior to the ring corner
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

  private static int quadrant(Coordinate origin, Coordinate p) {
    double dx = p.getX() - origin.getX();
    double dy = p.getY() - origin.getY();
    return Quadrant.quadrant(dx,  dy);
  }

}
