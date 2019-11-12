/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.Deque;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

/**
 * An overlay node is a vertex at which one or more edges terminate.
 * It is a "virtual" concept, 
 * which is represented by a single {@link OverlayEdge}
 * originating at the node coordinate. 
 * 
 * @author Martin Davis
 *
 */
class OverlayNode {


  /**
   * Scans around a node CCW, propagating the side labels
   * for a given area geometry to all edges (and their sym)
   * with unknown locations for that geometry.
   * @param e2 
   * 
   * @param geomIndex the geometry to propagate locations for
   */
  public static void propagateAreaLocations(OverlayEdge nodeEdge, int geomIndex) {
    /**
     * This handles dangling edges created by overlap limiting
     */
    if (nodeEdge.degree() == 1) return;
    
    OverlayEdge eStart = findPropagationStartEdge(nodeEdge, geomIndex);
    // no labelled edge found, so nothing to propagate
    if ( eStart == null )
      return;
    
    // initialize currLoc to location of L side
    int currLoc = eStart.getLocation(geomIndex, Position.LEFT);
    OverlayEdge e = eStart.oNextOE();

    //Debug.println("\npropagateSideLabels geomIndex = " + geomIndex + " : " + eStart);
    //Debug.print("BEFORE: " + toString(eStart));
    
    do {
      OverlayLabel label = e.getLabel();
      if ( ! label.isBoundary(geomIndex) ) {
      /**
       * If this is not a Boundary edge for this input area, 
       * its location is now known relative to this input area
       */
        label.setLocationLine(geomIndex, currLoc);
      }
      else {
        Assert.isTrue(label.hasSides(geomIndex));
        /**
         *  This is a boundary edge for the input area geom.
         *  Update the current location from its labels.
         *  Also check for topological consistency.
         */
        int locRight = e.getLocation(geomIndex, Position.RIGHT);
        if (locRight != currLoc) {
          /*
          Debug.println("side location conflict: index= " + geomIndex + " R loc " 
        + Location.toLocationSymbol(locRight) + " <>  curr loc " + Location.toLocationSymbol(currLoc) 
        + " for " + e);
        //*/
          throw new TopologyException("side location conflict: arg " + geomIndex, e.getCoordinate());
        }
        int locLeft = e.getLocation(geomIndex, Position.LEFT);
        if (locLeft == Location.NONE) {
          Assert.shouldNeverReachHere("found single null side at " + e);
        }
        currLoc = locLeft;
      }
      e = e.oNextOE();
    } while (e != eStart);
    //Debug.print("AFTER: " + toString(eStart));
  }

  /**
   * Finds a boundary edge for this geom, if one exists
   * 
   * @param nodeEdge an edge for this node
   * @param geomIndex the parent geometry index
   * @return a boundary edge, or null if no boundary edge exists
   */
  private static OverlayEdge findPropagationStartEdge(OverlayEdge nodeEdge, int geomIndex) {
    OverlayEdge eStart = nodeEdge;
    do {
      OverlayLabel label = eStart.getLabel();
      if (label.isBoundary(geomIndex)) {
        Assert.isTrue(label.hasSides(geomIndex));
        return eStart;
      }
      eStart = (OverlayEdge) eStart.oNext();
    } while (eStart != nodeEdge);
    return null;
  }
  
  static void propagateLineLocation(OverlayEdge eStart, int index, 
      Deque<OverlayEdge> edgeStack, InputGeometry inputGeometry) {
    OverlayEdge e = eStart.oNextOE();
    int lineLoc = eStart.getLabel().getLineLocation(index);
    
    /**
     * If the parent geom is an L (dim 1) 
     * then only propagate EXTERIOR locations.
     */
    if (! inputGeometry.isArea(index) 
        && lineLoc != Location.EXTERIOR) return;
    
    do {
      OverlayLabel label = e.getLabel();
      //Debug.println("propagateLineLocationAtNode - checking " + index + ": " + e);
      if ( label.isLineLocationUnknown(index) ) {
        /**
         * If edge is not a boundary edge, 
         * its location is now known for this area
         */
        label.setLocationLine(index, lineLoc);
        //Debug.println("propagateLineLocationAtNode - setting "+ index + ": " + e);

        /**
         * Add sym edge to stack for graph traversal
         * (Don't add e itself, since e origin node has now been scanned)
         */
        edgeStack.addFirst( e.symOE() );
      }
      e = e.oNextOE();
    } while (e != eStart);
  }
  
  public static String toString(OverlayEdge nodeEdge) {
    Coordinate orig = nodeEdge.orig();
    StringBuilder sb = new StringBuilder();
    sb.append("Node( "+ WKTWriter.format(orig) + " )" + "\n");
    OverlayEdge e = nodeEdge;
    do {
      sb.append("  -> " + e);
      if (e.isResultLinked()) {
        sb.append(" Link: ");
        sb.append(e.nextResult());
      }
      sb.append("\n");
      e = e.oNextOE();
    } while (e != nodeEdge);
    return sb.toString(); 
  }

}
