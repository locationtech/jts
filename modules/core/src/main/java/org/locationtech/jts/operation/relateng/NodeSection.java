/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.Comparator;

import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

/**
 * Represents a computed node along with the incident edges on either side of
 * it (if they exist).
 * This captures the information about a node in a geometry component
 * required to determine the component's contribution to the node topology.
 * A node in an area geometry always has edges on both sides of the node.
 * A node in a linear geometry may have one or other incident edge missing, if
 * the node occurs at an endpoint of the line.
 * The edges of an area node are assumed to be provided 
 * with CW-shell orientation (as per JTS norm).
 * This must be enforced by the caller.
 * 
 * @author Martin Davis
 *
 */
class NodeSection implements Comparable<NodeSection>
{
  /**
   * Compares sections by the angle the entering edge makes with the positive X axis.
   */
  public static class EdgeAngleComparator implements Comparator<NodeSection> {

    @Override
    public int compare(NodeSection ns1, NodeSection ns2) {
      return PolygonNodeTopology.compareAngle(ns1.nodePt, ns1.getVertex(0), ns2.getVertex(0));
    }
  }

  public static boolean isAreaArea(NodeSection a, NodeSection b) {
    return a.dimension() == Dimension.A && b.dimension() == Dimension.A;
  }
  
  private boolean isA;
  private int dim;
  private int id;
  private int ringId;
  private boolean isNodeAtVertex;
  private Coordinate nodePt;
  private Coordinate v0;
  private Coordinate v1;
  private Geometry poly;

  public NodeSection(boolean isA, 
      int dimension, int id, int ringId,  
      Geometry poly, boolean isNodeAtVertex, Coordinate v0, Coordinate nodePt, Coordinate v1) {
    this.isA = isA;
    this.dim = dimension;
    this.id = id;
    this.ringId = ringId;
    this.poly = poly;
    this.isNodeAtVertex = isNodeAtVertex;
    this.nodePt = nodePt;
    this.v0 = v0;
    this.v1 = v1;
  }
  
  public Coordinate getVertex(int i) {
    return i == 0 ? v0 : v1;
  }

  public Coordinate nodePt() {
    return nodePt;
  }

  public int dimension() {
    return dim;
  }

  public int id() {
    return id;
  }

  public int ringId() {
    return ringId;
  }
  
  public Geometry getPolygonal() {
    return poly;
  }
  
  public boolean isShell() {
    return ringId == 0;
  }
  
  public boolean isArea() {
    return dim == Dimension.A;
  }

  public boolean isA() {
     return isA;
  }

  public boolean isSameGeometry(NodeSection ns) {
    return isA() == ns.isA();
  }
  
  public boolean isSamePolygon(NodeSection ns) {
    return isA() == ns.isA() && id() == ns.id();
  }
  
  public boolean isNodeAtVertex() {
    return isNodeAtVertex;
  }

  public boolean isProper() {
    return ! isNodeAtVertex;
  }
  
  public static boolean isProper(NodeSection a, NodeSection b) {
    return a.isProper() && b.isProper();
  }
  
  public String toString() {
    String geomName = RelateGeometry.name(isA);
    String atVertexInd = isNodeAtVertex ? "-V-" : "---";
    String polyId = id >= 0 ? "[" + id + ":" + ringId + "]" : "";
    return String.format("%s%d%s: %s %s %s", 
        geomName, dim, polyId, edgeRep(v0, nodePt), atVertexInd, edgeRep(nodePt, v1));
  }

  private String edgeRep(Coordinate p0, Coordinate p1) {
    if (p0 == null || p1 == null)
      return "null";
    return WKTWriter.toLineString(p0, p1);
  }

  /**
   * Compare node sections by parent geometry, dimension, element id and ring id,
   * and edge vertices.
   * Sections are assumed to be at the same node point.
   */
  @Override
  public int compareTo(NodeSection o) {
    // Assert: nodePt.equals2D(o.nodePt())
    
    // sort A before B
    if (isA != o.isA) {
      if (isA) return -1;
      return 1;
    }
    //-- sort on dimensions
    int compDim = Integer.compare(dim,  o.dim);
    if (compDim != 0) return compDim;

    //-- sort on id and ring id
    int compId = Integer.compare(id, o.id);
    if (compId != 0) return compId;
    
    int compRingId = Integer.compare(ringId, o.ringId);
    if (compRingId != 0) return compRingId;
  
    //-- sort on edge coordinates
    int compV0 = compareWithNull(v0, o.v0);
    if (compV0 != 0) return compV0;
    
    return compareWithNull(v1, o.v1);
  }
  
  private static int compareWithNull(Coordinate v0, Coordinate v1) {
    if (v0 == null) {
      if (v1 == null) 
        return 0;
      //-- null is lower than non-null
      return -1;
    }
    // v0 is non-null
    if (v1 == null) 
      return 1;
    return v0.compareTo(v1);
  }


  
}
