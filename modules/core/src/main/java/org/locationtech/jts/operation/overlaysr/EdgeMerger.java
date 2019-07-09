package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

public class EdgeMerger {

  public static List<Edge> merge(List<Edge> edges) {
    EdgeMerger merger = new EdgeMerger(edges);
    return merger.merge();
  }

  private Collection<Edge> edges;
  private Map<EdgeKey, Edge> edgeMap = new HashMap<EdgeKey, Edge>();
  
  public EdgeMerger(List<Edge> edges) {
    this.edges = edges;
  }
  
  public ArrayList<Edge> merge() {
    for (Edge ss : edges) {
      EdgeKey edgeKey = EdgeKey.create(ss);
      Edge existing = edgeMap.get(edgeKey);
      if (existing == null) {
        edgeMap.put(edgeKey, ss);
      }
      else {
        // TODO: check that edges are identical (up to direction)
        mergeLabel(existing, ss);
      }
    }
    return new ArrayList<Edge>(edgeMap.values());
  }
  
  private void mergeLabel(Edge target, Edge edge) {
    boolean relDir = relativeDirection(target, edge);
    OverlayLabel lblTarget = target.getLabel();
    OverlayLabel lblToMerge = edge.getLabel();
    if (relDir) {
      lblTarget.merge(lblToMerge);
    }
    else {
      lblTarget.mergeFlip(lblToMerge);
    }
  }

  /**
   * Compares endpoints of two matching edges to determine
   * whether they have the same or opposite direction.
   * 
   * @param edge1 an edge
   * @param edge2 an edge
   * @return true if the edges have the same direction, false if not
   */
  private static boolean relativeDirection(Edge edge1, Edge edge2) {
    // assert: the edges match (have the same coordinates up to direction)
    if (! edge1.getCoordinate(0).equals2D(edge2.getCoordinate(0)))
      return false;
    if (! edge1.getCoordinate(1).equals2D(edge2.getCoordinate(1)))
      return false;
    return true;
  }

  private static class EdgeKey implements Comparable<EdgeKey> {
    
    public static EdgeKey create(Edge edge) {
      return new EdgeKey(edge);
    }
    
    public static boolean direction(Edge ss) {
      Coordinate[] pts = ss.getCoordinates();
      if (pts.length < 2) {
        throw new IllegalStateException("Edge must have >= 2 points");
      }
      Coordinate p0 = pts[0];
      Coordinate p1 = pts[1];
      
      Coordinate pn0 = pts[pts.length - 1];
      Coordinate pn1 = pts[pts.length - 2];
      
      int cmp = 0;
      int cmp0 = p0.compareTo(pn0);
      if (cmp0 != 0) cmp = cmp0;
      
      if (cmp == 0) {
        int cmp1 = p1.compareTo(pn1);
        if (cmp1 != 0) cmp = cmp1;
      }
      
      if (cmp == 0) {
        throw new IllegalStateException("Edge direction cannot be determined because endpoints are equal");
      }
      
      return cmp == -1 ? true : false;
    }
    
    private Edge edge;
    private Coordinate p0;
    private Coordinate p1;

    EdgeKey(Edge edge) {
      this.edge = edge;
      initPoints(edge);
    }

    private void initPoints(Edge edge) {
      boolean direction = direction(edge);
      if (direction) {
        p0 = edge.getCoordinate(0);
        p1 = edge.getCoordinate(1);
      }
      else {
        int len = edge.size();
        p0 = edge.getCoordinate(len - 1);
        p1 = edge.getCoordinate(len - 2);
      }
    }

    @Override
    public int compareTo(EdgeKey ek) {
      int cmp0 = p0.compareTo(ek.p0);
      if (cmp0 != 0) return cmp0;
      int cmp1 = p1.compareTo(ek.p1);
      return cmp1;
    }
    
    public boolean equals(Object o) {
      if (! (o instanceof EdgeKey)) {
        return false;
      }
      EdgeKey ek = (EdgeKey) o;
      return p0.equals2D(ek.p0) && p1.equals2D(ek.p1);
    }
    
    /**
     * Gets a hashcode for this object.
     * 
     * @return a hashcode for this object
     */
    public int hashCode() {
      //Algorithm from Effective Java by Joshua Bloch
      int result = 17;
      result = 37 * result + hashCode(p0.x);
      result = 37 * result + hashCode(p0.y);
      result = 37 * result + hashCode(p1.x);
      result = 37 * result + hashCode(p1.y);
      return result;
    }
    
    /**
     * Computes a hash code for a double value, using the algorithm from
     * Joshua Bloch's book <i>Effective Java"</i>
     * 
     * @param x the value to compute for
     * @return a hashcode for x
     */
    public static int hashCode(double x) {
      long f = Double.doubleToLongBits(x);
      return (int)(f^(f>>>32));
    }
    
    public String toString() {
      return "EdgeKey(" + WKTWriter.format(p0) 
        + ", " +  WKTWriter.format(p1) + ")";
    }
  }
}
