package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geomgraph.DirectedEdge;
import org.locationtech.jts.operation.overlay.MaximalEdgeRing;

public class PolygonBuilder {

  private OverlayGraph graph;
  private GeometryFactory geomFact;

  public PolygonBuilder(OverlayGraph graph, GeometryFactory geomFact) {
    this.graph = graph;
    this.geomFact = geomFact;
  }

  public List<Polygon> getPolygons() {
    
  }

  /**
   * for all DirectedEdges in result, form them into MaximalEdgeRings
   */
  private List buildMaximalEdgeRings(Collection dirEdges)
  {
    List maxEdgeRings     = new ArrayList();
    for (Iterator it = dirEdges.iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      if (de.isInResult() && de.getLabel().isArea() ) {
        // if this edge has not yet been processed
        if (de.getEdgeRing() == null) {
          MaximalEdgeRing er = new MaximalEdgeRing(de, geometryFactory);
          maxEdgeRings.add(er);
          er.setInResult();
//System.out.println("max node degree = " + er.getMaxDegree());
        }
      }
    }
    return maxEdgeRings;
  }

}
