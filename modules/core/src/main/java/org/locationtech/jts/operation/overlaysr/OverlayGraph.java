package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.util.Assert;

public class OverlayGraph {

  public static OverlayGraph buildGraph(Collection<SegmentString> edges) {
    OverlayGraph graph = new OverlayGraph();
    for (SegmentString ss : edges) {
      graph.addEdge(ss);
    }
    return graph;
  }

  private List<OverlayEdge> edges = new ArrayList<OverlayEdge>();
  private Map<Coordinate, OverlayEdge> vertexMap = new HashMap<Coordinate, OverlayEdge>();
  
  public OverlayGraph() {
  }

  /**
   * Creates a single HalfEdge.
   * Override to use a different HalfEdge subclass.
   * @param lbl 
   * 
   * @param orig the origin location
   * @param direction the direction along the segment string - true is forward
   * @return a new HalfEdge with the given origin
   */
  protected OverlayEdge createEdge(SegmentString ss, OverlayLabel lbl, boolean direction)
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
    return new OverlayEdge(origin, dirPt, direction, lbl, ss);
  }

  private OverlayEdge create(SegmentString ss)
  {
    // Note that the same label is used for each half-edge
    OverlayLabel lbl = new OverlayLabel((OverlayLabel) ss.getData());
    OverlayEdge e0 = createEdge(ss, lbl, true);
    
    OverlayLabel lblOpp = lbl.createFlipped();
    OverlayEdge e1 = createEdge(ss, lblOpp, false);
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
    edges.add(e);
    OverlayEdge eAdj = (OverlayEdge) vertexMap.get(e.orig());
    if (eAdj != null) {
      eAdj.insert(e);
    }
    else {
      // add new halfedges to to map
      vertexMap.put(e.orig(), e);
    }
  }

  public List<OverlayEdge> getEdges() 
  {
    return edges;
  }
  
  public Collection<OverlayEdge> getVertexEdges()
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

  /**
   * Computes a full topological labelling for all edges and nodes in the graph.
   */
  public void computeLabelling() {
    // compute labelling using a Left-Right sweepline, to keep things deterministic
    List<OverlayEdge> nodes = sortedNodes();
    for (OverlayEdge node : nodes) {
      node.computeLabelling();
    }
    mergeSymLabels(nodes);
  }

  private void mergeSymLabels(List<OverlayEdge> nodes) {
    for (OverlayEdge node : nodes) {
      node.nodeMergeSymLabels();
    }
  }

  private List<OverlayEdge> sortedNodes() {
    List<OverlayEdge> edges = new ArrayList<OverlayEdge>(getVertexEdges());
    edges.sort(OverlayEdge.nodeComparator());
    return edges;
  }

  public void markResultAreaEdges(int overlayOpCode) {
    for (OverlayEdge edge : getEdges()) {
      edge.markInResultArea(overlayOpCode);
    }
  }

  public void cancelDuplicateResultAreaEdges() {
    for (OverlayEdge edge : getEdges()) {
      if ( edge.isInResult()  && edge.symOE().isInResult() ) {
        edge.removeFromResult();
        edge.symOE().removeFromResult();      
      }
    }
  }

  public List<OverlayEdge> getResultAreaEdges() {
    List<OverlayEdge> resultEdges = new ArrayList<OverlayEdge>();
    for (OverlayEdge edge : getEdges()) {
      if (edge.isInResult()) {
        resultEdges.add(edge);
      }
    } 
    return resultEdges;
  }

  public void linkResultAreaEdges(List<OverlayEdge> resultEdges) {
    for (OverlayEdge edge : resultEdges ) {
      //Assert.isTrue(edge.isInResult());
      // TODO: find some way to skip nodes which are already linked
      edge.linkOriginResultEdges();
    }    
  }

}
