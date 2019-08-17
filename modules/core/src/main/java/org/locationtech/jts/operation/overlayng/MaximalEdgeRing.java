package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;


public class MaximalEdgeRing {

  private OverlayEdge startEdge;
  private boolean isValid = false;

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
        return;
        // throw new TopologyException("Found null edge in ring", edge.dest());
      }
      edge.setEdgeRingMax(this);
      edge = edge.nextResultMax();
    } while (edge != startEdge);  
    isValid = true;
  }
  
  public boolean isValid() {
    return isValid;
  }
  
  private void linkMinimalRings() {
    OverlayEdge e = startEdge;
    do {
      OverlayNode.linkResultAreaEdges(e, this);
      e = e.nextResultMax();
    } while (e != startEdge);
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
}
