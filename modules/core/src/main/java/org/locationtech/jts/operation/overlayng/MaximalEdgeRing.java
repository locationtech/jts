package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;


public class MaximalEdgeRing {

  private OverlayEdge startEdge;

  public MaximalEdgeRing(OverlayEdge e) {
    this.startEdge = e;
    assignEdgeRing(e);
  }

  private void assignEdgeRing(OverlayEdge startEdge) {
    OverlayEdge edge = startEdge;
    do {
      if (edge == null)
        throw new TopologyException("Found null edge in ring");
      if (edge.getEdgeRingMax() == this)
        throw new TopologyException("Edge visited twice during ring-building at " + edge.getCoordinate(), edge.getCoordinate());
      if (edge.nextResultMax() == null)
        throw new TopologyException("Found null edge in ring", edge.dest());

      edge.setEdgeRingMax(this);
      edge = edge.nextResultMax();
    } while (edge != startEdge);  
  }
  
  private void linkMinimalRings() {
    OverlayEdge e = startEdge;
    do {
      OverlayNode.linkResultAreaEdges(e, this);
      e = e.nextResultMax();
    } while (e != startEdge);
  }
  
  public List<EdgeRing> buildMinimalRings(GeometryFactory geometryFactory)
  {
    linkMinimalRings();
    
    List<EdgeRing> minEdgeRings = new ArrayList<EdgeRing>();
    OverlayEdge e = startEdge;
    do {
      if (e.getEdgeRing() == null) {
        EdgeRing minEr = new EdgeRing(e, geometryFactory);
        minEdgeRings.add(minEr);
      }
      e = e.nextResultMax();
    } while (e != startEdge);
    return minEdgeRings;
  }
}
