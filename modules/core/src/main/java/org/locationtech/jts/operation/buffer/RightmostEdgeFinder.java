

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
package org.locationtech.jts.operation.buffer;

/**
 * @version 1.7
 */
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.DirectedEdge;
import org.locationtech.jts.geomgraph.DirectedEdgeStar;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.geomgraph.Node;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.util.Assert;

/**
 * A RightmostEdgeFinder find the DirectedEdge in a list which has the highest coordinate,
 * and which is oriented L to R at that point. (I.e. the right side is on the RHS of the edge.)
 *
 * @version 1.7
 */
class RightmostEdgeFinder {

  //private Coordinate extremeCoord;
  private int minIndex = -1;
  private Coordinate minCoord = null;
  private DirectedEdge minDe = null;
  private DirectedEdge orientedDe = null;
  /**
   * A RightmostEdgeFinder finds the DirectedEdge with the rightmost coordinate.
   * The DirectedEdge returned is guaranteed to have the R of the world on its RHS.
   */
  public RightmostEdgeFinder()
  {
  }
  
  public DirectedEdge getEdge()  {    return orientedDe;  }
  public Coordinate getCoordinate()  {    return minCoord;  }

  public void findEdge(List dirEdgeList)
  {
    /**
     * Check all forward DirectedEdges only.  This is still general,
     * because each edge has a forward DirectedEdge.
     */
    for (Iterator i = dirEdgeList.iterator(); i.hasNext();) {
      DirectedEdge de = (DirectedEdge) i.next();
      if (! de.isForward())
        continue;
      checkForRightmostCoordinate(de);
    }

    /**
     * If the rightmost point is a node, we need to identify which of
     * the incident edges is rightmost.
     */
    Assert.isTrue(minIndex != 0 || minCoord.equals(minDe.getCoordinate()) , "inconsistency in rightmost processing");
    if (minIndex == 0 ) {
      findRightmostEdgeAtNode();
    }
    else {
      findRightmostEdgeAtVertex();
    }
    /**
     * now check that the extreme side is the R side.
     * If not, use the sym instead.
     */
    orientedDe = minDe;
    int rightmostSide = getRightmostSide(minDe, minIndex);
    if (rightmostSide == Position.LEFT) {
      orientedDe = minDe.getSym();
    }
  }
  private void findRightmostEdgeAtNode()
  {
      Node node = minDe.getNode();
      DirectedEdgeStar star = (DirectedEdgeStar) node.getEdges();
      minDe = star.getRightmostEdge();
      // the DirectedEdge returned by the previous call is not
      // necessarily in the forward direction. Use the sym edge if it isn't.
      if (! minDe.isForward()) {
        minDe = minDe.getSym();
        minIndex = minDe.getEdge().getCoordinates().length - 1;
      }
  }
  private void findRightmostEdgeAtVertex()
  {
      /**
       * The rightmost point is an interior vertex, so it has a segment on either side of it.
       * If these segments are both above or below the rightmost point, we need to
       * determine their relative orientation to decide which is rightmost.
       */
      Coordinate[] pts = minDe.getEdge().getCoordinates();
      Assert.isTrue(minIndex > 0 && minIndex < pts.length, "rightmost point expected to be interior vertex of edge");
      Coordinate pPrev = pts[minIndex - 1];
      Coordinate pNext = pts[minIndex + 1];
      int orientation = CGAlgorithms.computeOrientation(minCoord, pNext, pPrev);
      boolean usePrev = false;
        // both segments are below min point
      if (pPrev.y < minCoord.y && pNext.y < minCoord.y
         && orientation == CGAlgorithms.COUNTERCLOCKWISE) {
          usePrev = true;
      }
      else if (pPrev.y > minCoord.y && pNext.y > minCoord.y
                && orientation == CGAlgorithms.CLOCKWISE) {
          usePrev = true;
      }
      // if both segments are on the same side, do nothing - either is safe
      // to select as a rightmost segment
      if (usePrev) {
        minIndex = minIndex - 1;
      }
  }
  private void checkForRightmostCoordinate(DirectedEdge de)
  {
    Coordinate[] coord = de.getEdge().getCoordinates();
    for (int i = 0; i < coord.length - 1; i++) {
      // only check vertices which are the start or end point of a non-horizontal segment
     // <FIX> MD 19 Sep 03 - NO!  we can test all vertices, since the rightmost must have a non-horiz segment adjacent to it
        if (minCoord == null || coord[i].x > minCoord.x ) {
          minDe = de;
          minIndex = i;
          minCoord = coord[i];
        }
      //}
    }
  }

  private int getRightmostSide(DirectedEdge de, int index)
  {
    int side = getRightmostSideOfSegment(de, index);
    if (side < 0)
      side = getRightmostSideOfSegment(de, index - 1);
    if (side < 0) {
      // reaching here can indicate that segment is horizontal
      //Assert.shouldNeverReachHere("problem with finding rightmost side of segment at " + de.getCoordinate());
      // testing only
      minCoord = null;
      checkForRightmostCoordinate(de);
    }
    return side;
  }

  private int getRightmostSideOfSegment(DirectedEdge de, int i)
  {
    Edge e = de.getEdge();
    Coordinate coord[] = e.getCoordinates();

    if (i < 0 || i + 1 >= coord.length) return -1;
    if (coord[i].y == coord[i + 1].y) return -1;    // indicates edge is parallel to x-axis

    int pos = Position.LEFT;
    if (coord[i].y < coord[i + 1].y) pos = Position.RIGHT;
    return pos;
  }
}
