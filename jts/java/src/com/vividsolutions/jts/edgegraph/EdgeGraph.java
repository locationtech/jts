package com.vividsolutions.jts.edgegraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A graph comprised of {@link HalfEdge}s.
 * It supports tracking the vertices in the graph
 * via edges incident on them, 
 * to allow efficient lookup of edges and vertices.
 * <p>
 * This class may be subclassed to use a 
 * different subclass of HalfEdge,
 * by overriding {@link #createEdge(Coordinate)}.
 * If additional logic is required to initialize
 * edges then {@link EdgeGraph#addEdge(Coordinate, Coordinate)}
 * can be overridden as well.
 * 
 * @author Martin Davis
 *
 */
public class EdgeGraph 
{
  private Map vertexMap = new HashMap();
  
  public EdgeGraph() {
  }

  /**
   * Creates a single HalfEdge.
   * Override to use a different HalfEdge subclass.
   * 
   * @param orig the origin location
   * @return a new HalfEdge with the given origin
   */
  protected HalfEdge createEdge(Coordinate orig)
  {
    return new HalfEdge(orig);
  }

  private HalfEdge create(Coordinate p0, Coordinate p1)
  {
    HalfEdge e0 = createEdge(p0);
    HalfEdge e1 = createEdge(p1);
    HalfEdge.init(e0, e1);
    return e0;
  }
  
  /**
   * Adds an edge between the coordinates orig and dest
   * to this graph.
   * 
   * @param orig the origin location
   * @param dest the destination location.
   * @return the created edge
   */
  public HalfEdge addEdge(Coordinate orig, Coordinate dest) {
    int cmp = dest.compareTo(orig);
    // ignore zero-length edges
    if (cmp == 0) return null;
    
    HalfEdge eAdj = (HalfEdge) vertexMap.get(orig);
    HalfEdge eSame = null;
    if (eAdj != null) {
      eSame = eAdj.find(dest);
    }
    if (eSame != null) {
      return eSame;
    }
    
    // edge does not exist, so create it and insert in graph
    HalfEdge e = create(orig, dest);
    if (eAdj != null) {
      eAdj.insert(e);
    }
    else {
      // add halfedges to to map
      vertexMap.put(orig, e);
    }
    
    HalfEdge eAdjDest = (HalfEdge) vertexMap.get(dest);
    if (eAdjDest != null) {
      eAdjDest.insert(e.sym());
    }
    else {
      vertexMap.put(dest, e.sym());
    }
    
    return e;
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
}
