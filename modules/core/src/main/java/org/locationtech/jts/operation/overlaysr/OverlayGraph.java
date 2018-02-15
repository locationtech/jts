package org.locationtech.jts.operation.overlaysr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentString;

public class OverlayGraph {

  public static OverlayGraph buildGraph(Collection edges) {
    OverlayGraph graph = new OverlayGraph();
    for (Iterator it = edges.iterator(); it.hasNext(); ) {
      SegmentString ss = (SegmentString) it.next();
      graph.addEdge(ss);
    }
    return graph;
  }

  private Map vertexMap = new HashMap();
  
  public OverlayGraph() {
  }

  /**
   * Creates a single HalfEdge.
   * Override to use a different HalfEdge subclass.
   * 
   * @param orig the origin location
   * @param direction the direction along the segment string - true is forward
   * @return a new HalfEdge with the given origin
   */
  protected OverlayEdge createEdge(SegmentString ss, boolean direction)
  {
    Coordinate origin;
    Coordinate dirPt;
    if (direction) {
      origin = ss.getCoordinate(0);
      dirPt = ss.getCoordinate(1);
    }
    else {
      int ilast = ss.size() - 1;
      origin = ss.getCoordinate(ilast);
      dirPt = ss.getCoordinate(ilast-1);
    }
    return new OverlayEdge(origin, dirPt, direction, ss);
  }

  private OverlayEdge create(SegmentString ss)
  {
    OverlayEdge e0 = createEdge(ss, true);
    OverlayEdge e1 = createEdge(ss, false);
    e0.init(e1);
    return e0;
  }
  
  /**
   * Adds an edge between the coordinates orig and dest
   * to this graph.
   * Only valid edges can be added (in particular, zero-length segments cannot be added)
   * 
   * @param orig the edge origin location
   * @param dest the edge destination location.
   * @return the created edge
   * @return null if the edge was invalid and not added
   * 
   * @see #isValidEdge(Coordinate, Coordinate)
   */
  public OverlayEdge addEdge(SegmentString ss) {
    //if (! isValidEdge(orig, dest)) return null;
    OverlayEdge e = insert(ss);
    return e;
  }

  /**
   * Tests if the given coordinates form a valid edge (with non-zero length).
   * 
   * @param orig the start coordinate
   * @param dest the end coordinate
   * @return true if the edge formed is valid
   */
  public static boolean isValidEdge(Coordinate orig, Coordinate dest) {
    int cmp = dest.compareTo(orig);
    return cmp != 0;
  }

  /**
   * Inserts an edge not already present into the graph.
   * 
   * @param orig the edge origin location
   * @param dest the edge destination location
   * @param eAdj an existing edge with same orig (if any)
   * @return the created edge
   */
  private OverlayEdge insert(SegmentString ss) {
    // edge does not exist, so create it and insert in graph
    OverlayEdge e = create(ss);
    insert(e);
    insert((OverlayEdge) e.sym());

    return e;
  }

  private void insert(OverlayEdge e) {
    OverlayEdge eAdj = (OverlayEdge) vertexMap.get(e.orig());
    if (eAdj != null) {
      eAdj.insert(e);
    }
    else {
      // add new halfedges to to map
      vertexMap.put(e.orig(), e);
    }
  }

  public Collection getVertexEdges()
  {
    return vertexMap.values();
  }

  /**
   * Finds an edge in this graph with the given origin
   * and destination, if one exists.
   * 
   * @param orig the origin location
   * @param dest the destination location.
   * @return an edge with the given orig and dest, or null if none exists
   */
  public HalfEdge findEdge(Coordinate orig, Coordinate dest) {
    HalfEdge e = (HalfEdge) vertexMap.get(orig);
    if (e == null) return null;
    return e.find(dest);
  }

  public void computeLabelling() {
    // TODO Auto-generated method stub
    
  }
}
