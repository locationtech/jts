package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.WKTWriter;


class MaximalEdgeRing {

  private OverlayEdge startEdge;

  public MaximalEdgeRing(OverlayEdge e) {
    this.startEdge = e;
    attachRingtoEdges(e);
  }

  private void attachRingtoEdges(OverlayEdge startEdge) {
    OverlayEdge edge = startEdge;
    do {
      if (edge == null)
        throw new TopologyException("Found null edge in ring");
      if (edge.getEdgeRingMax() == this)
        throw new TopologyException("Edge visited twice during ring-building at " + edge.getCoordinate(), edge.getCoordinate());
      if (edge.nextResultMax() == null) {
        throw new TopologyException("Found null edge in ring", edge.dest());
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
      linkMinRingEdges(e, this);
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
  private static void linkMinRingEdges(OverlayEdge nodeEdge, MaximalEdgeRing maxRing)
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
    
    currIn.setResultNext(currMaxRingOut);
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
      if (edge == null)
        break;
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
