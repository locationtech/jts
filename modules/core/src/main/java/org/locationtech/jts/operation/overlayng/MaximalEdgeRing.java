/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;


class MaximalEdgeRing {

  private static final int STATE_FIND_INCOMING = 1;
  private static final int STATE_LINK_OUTGOING = 2;

  /**
   * Traverses the star of edges originating at a node
   * and links consecutive result edges together
   * into <b>maximal</b> edge rings.
   * To link two edges the <code>resultNextMax</code> pointer 
   * for an <b>incoming</b> result edge
   * is set to the next <b>outgoing</b> result edge.
   * <p>
   * Edges are linked when:
   * <ul>
   * <li>they belong to an area (i.e. they have sides)
   * <li>they are marked as being in the result
   * </ul>
   * <p>
   * Edges are linked in CCW order 
   * (which is the order they are linked in the underlying graph).
   * This means that rings have their face on the Right
   * (in other words,
   * the topological location of the face is given by the RHS label of the DirectedEdge).
   * This produces rings with CW orientation.
   * <p>
   * PRECONDITIONS: 
   * - This edge is in the result
   * - This edge is not yet linked
   * - The edge and its sym are NOT both marked as being in the result
   */
  public static void linkResultAreaMaxRingAtNode(OverlayEdge nodeEdge)
  {
    Assert.isTrue(nodeEdge.isInResultArea(), "Attempt to link non-result edge");
    // assertion is only valid if building a polygonal geometry (ie not a coverage)
    //Assert.isTrue(! nodeEdge.symOE().isInResultArea(), "Found both half-edges in result");

    /**
     * Since the node edge is an out-edge, 
     * make it the last edge to be linked
     * by starting at the next edge.
     * The node edge cannot be an in-edge as well, 
     * but the next one may be the first in-edge.
     */
    OverlayEdge endOut = nodeEdge.oNextOE();
    OverlayEdge currOut = endOut;
//Debug.println("\n------  Linking node MAX edges");
//Debug.println("BEFORE: " + toString(nodeEdge));
    int state = STATE_FIND_INCOMING;
    OverlayEdge currResultIn = null;
    do {
      /**
       * If an edge is linked this node has already been processed
       * so can skip further processing
       */
      if (currResultIn != null && currResultIn.isResultMaxLinked())
        return;
      
      switch (state) {
      case STATE_FIND_INCOMING:
        OverlayEdge currIn = currOut.symOE();
        if (! currIn.isInResultArea()) break;
        currResultIn = currIn;
        state = STATE_LINK_OUTGOING;
        //Debug.println("Found result in-edge:  " + currResultIn);
        break;
      case STATE_LINK_OUTGOING:
        if (! currOut.isInResultArea()) break;
        // link the in edge to the out edge
        currResultIn.setNextResultMax(currOut);
        state = STATE_FIND_INCOMING;
        //Debug.println("Linked Max edge:  " + currResultIn + " -> " + currOut);
        break;
      }
      currOut = currOut.oNextOE();
    } while (currOut != endOut);
    //Debug.println("AFTER: " + toString(nodeEdge));
    if (state == STATE_LINK_OUTGOING) {
//Debug.print(firstOut == null, this);
      throw new TopologyException("no outgoing edge found", nodeEdge.getCoordinate());
    }    
  }

  private OverlayEdge startEdge;

  public MaximalEdgeRing(OverlayEdge e) {
    this.startEdge = e;
    attachEdges(e);
  }

  private void attachEdges(OverlayEdge startEdge) {
    OverlayEdge edge = startEdge;
    do {
      if (edge == null)
        throw new TopologyException("Ring edge is null");
      if (edge.getEdgeRingMax() == this)
        throw new TopologyException("Ring edge visited twice at " + edge.getCoordinate(), edge.getCoordinate());
      if (edge.nextResultMax() == null) {
        throw new TopologyException("Ring edge missing at", edge.dest());
      }
      edge.setEdgeRingMax(this);
      edge = edge.nextResultMax();
    } while (edge != startEdge);  
  }
  
  public List<OverlayEdgeRing> buildMinimalRings(GeometryFactory geometryFactory)
  {
    linkMinimalRings();
    
    List<OverlayEdgeRing> minEdgeRings = new ArrayList<OverlayEdgeRing>();
    OverlayEdge e = startEdge;
    do {
      if (e.getEdgeRing() == null) {
        OverlayEdgeRing minEr = new OverlayEdgeRing(e, geometryFactory);
        minEdgeRings.add(minEr);
      }
      e = e.nextResultMax();
    } while (e != startEdge);
    return minEdgeRings;
  }
  
  private void linkMinimalRings() {
    OverlayEdge e = startEdge;
    do {
      linkMinRingEdgesAtNode(e, this);
      e = e.nextResultMax();
    } while (e != startEdge);
  }
  
  /**
   * Links the edges of a {@link MaximalEdgeRing} around this node
   * into minimal edge rings ({@link OverlayEdgeRing}s).
   * Minimal ring edges are linked in the opposite orientation (CW)
   * to the maximal ring.
   * This changes self-touching rings into a two or more separate rings,
   * as per the OGC SFS polygon topology semantics.
   * This relinking must be done to each max ring separately,
   * rather than all the node result edges, since there may be 
   * more than one max ring incident at the node.
   * 
   * @param nodeEdge an edge originating at this node
   * @param maxRing the maximal ring to link
   */
  private static void linkMinRingEdgesAtNode(OverlayEdge nodeEdge, MaximalEdgeRing maxRing)
  {
    //Assert.isTrue(nodeEdge.isInResult(), "Attempt to link non-result edge");

    /**
     * The node edge is an out-edge, 
     * so it is the first edge linked
     * with the next CCW in-edge
     */
    OverlayEdge endOut = nodeEdge;
    OverlayEdge currMaxRingOut = endOut;
    OverlayEdge currOut = endOut.oNextOE();
//Debug.println("\n------  Linking node MIN ring edges");
//Debug.println("BEFORE: " + toString(nodeEdge));
    do {
      if (isAlreadyLinked(currOut.symOE(), maxRing)) 
        return;

      if (currMaxRingOut == null) {
        currMaxRingOut = selectMaxOutEdge(currOut, maxRing);
      }
      else {
        currMaxRingOut = linkMaxInEdge(currOut, currMaxRingOut, maxRing);
      }
      currOut = currOut.oNextOE();
    } while (currOut != endOut);
    //Debug.println("AFTER: " + toString(nodeEdge));
    if ( currMaxRingOut != null ) {
      throw new TopologyException("Unmatched edge found during min-ring linking", nodeEdge.getCoordinate());
    }    
  }

  /**
   * Tests if an edge of the maximal edge ring is already linked into
   * a minimal {@link OverlayEdgeRing}.  If so, this node has already been processed
   * earlier in the maximal edgering linking scan.
   * 
   * @param edge an edge of a maximal edgering
   * @param maxRing the maximal edgering
   * @return true if the edge has already been linked into a minimal edgering.
   */
  private static boolean isAlreadyLinked(OverlayEdge edge, MaximalEdgeRing maxRing) {
    boolean isLinked = edge.getEdgeRingMax() == maxRing
        && edge.isResultLinked();
    return isLinked;
  }

  private static OverlayEdge selectMaxOutEdge(OverlayEdge currOut, MaximalEdgeRing maxEdgeRing) {
    // select if currOut edge is part of this max ring
    if (currOut.getEdgeRingMax() ==  maxEdgeRing)
      return currOut;
    // otherwise skip this edge
    return null;
  }

  private static OverlayEdge linkMaxInEdge(OverlayEdge currOut, 
      OverlayEdge currMaxRingOut, 
      MaximalEdgeRing maxEdgeRing) 
  {
    OverlayEdge currIn = currOut.symOE();
    // currIn is not in this max-edgering, so keep looking
    if (currIn.getEdgeRingMax() !=  maxEdgeRing) 
      return currMaxRingOut;
     
    //Debug.println("Found result in-edge:  " + currIn);
    
    currIn.setNextResult(currMaxRingOut);
    //Debug.println("Linked Min Edge:  " + currIn + " -> " + currMaxRingOut);
    // return null to indicate to scan for the next max-ring out-edge
    return null;
  }
  
  public String toString() {
    Coordinate[] pts = getCoordinates();
    return WKTWriter.toLineString(pts);
  }

  private Coordinate[] getCoordinates() {
    CoordinateList coords = new CoordinateList();
    OverlayEdge edge = startEdge;
    do {
      coords.add(edge.orig());
      if (edge.nextResultMax() == null) {
        break;
      }
      edge = edge.nextResultMax();
    } while (edge != startEdge); 
    // add last coordinate
    coords.add(edge.dest());
    return coords.toCoordinateArray();
  }
}
