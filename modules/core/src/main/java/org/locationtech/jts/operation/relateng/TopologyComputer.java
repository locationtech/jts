/*
 * Copyright (c) 2022 Martin Davis.
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

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.util.Assert;

class TopologyComputer {

  private static final String MSG_GEOMETRY_DIMENSION_UNEXPECTED = "Unexpected combination of geometry dimensions";

  private TopologyPredicate predicate;
  private RelateGeometry geomA;
  private RelateGeometry geomB;
  private Map<Coordinate, NodeSections> nodeMap = new HashMap<Coordinate, NodeSections>();

  public TopologyComputer(TopologyPredicate predicate, RelateGeometry geomA, RelateGeometry geomB) {
    this.predicate = predicate;
    this.geomA = geomA;
    this.geomB = geomB;
    
    initExteriorDims();
  }

  /**
   * Determine a priori partial EXTERIOR topology based on dimensions.
   */
  private void initExteriorDims() {
    int dimRealA = geomA.getDimensionReal();
    int dimRealB = geomB.getDimensionReal();
    
    /**
     * For P/L case, P exterior intersects L interior
     */
    if (dimRealA == Dimension.P && dimRealB == Dimension.L) {
      updateDim(Location.EXTERIOR, Location.INTERIOR, Dimension.L);
    }
    else if (dimRealA == Dimension.L && dimRealB == Dimension.P) {
      updateDim(Location.INTERIOR, Location.EXTERIOR, Dimension.L);
    }
    /**
     * For P/A case, the Area Int and Bdy intersect the Point exterior.
     */
    else if (dimRealA == Dimension.P && dimRealB == Dimension.A) {
      updateDim(Location.EXTERIOR, Location.INTERIOR, Dimension.A);
      updateDim(Location.EXTERIOR, Location.BOUNDARY, Dimension.L);
    }
    else if (dimRealA == Dimension.A && dimRealB == Dimension.P) {
      updateDim(Location.INTERIOR, Location.EXTERIOR, Dimension.A);
      updateDim(Location.BOUNDARY, Location.EXTERIOR, Dimension.L);
    }
    else if (dimRealA == Dimension.L && dimRealB == Dimension.A) {
      updateDim(Location.EXTERIOR, Location.INTERIOR, Dimension.A);
     }
    else if (dimRealA == Dimension.A && dimRealB == Dimension.L) {
      updateDim(Location.INTERIOR, Location.EXTERIOR, Dimension.A);
    }
    //-- cases where one geom is EMPTY
    else if (dimRealA == Dimension.FALSE || dimRealB == Dimension.FALSE) {
      if (dimRealA != Dimension.FALSE) {
        initExteriorEmpty(RelateGeometry.GEOM_A);
      }
      if (dimRealB != Dimension.FALSE) {
        initExteriorEmpty(RelateGeometry.GEOM_B);
      }
    }
  }

  private void initExteriorEmpty(boolean geomNonEmpty) {
    int dimNonEmpty = getDimension(geomNonEmpty);
    switch (dimNonEmpty) {
    case Dimension.P:
      updateDim(geomNonEmpty, Location.INTERIOR, Location.EXTERIOR, Dimension.P);
      break;
    case Dimension.L:
      if (getGeometry(geomNonEmpty).hasBoundary()) {
        updateDim(geomNonEmpty, Location.BOUNDARY, Location.EXTERIOR, Dimension.P);
      }
      updateDim(geomNonEmpty, Location.INTERIOR, Location.EXTERIOR, Dimension.L);
      break;
    case Dimension.A:
      updateDim(geomNonEmpty, Location.BOUNDARY, Location.EXTERIOR, Dimension.L);
      updateDim(geomNonEmpty, Location.INTERIOR, Location.EXTERIOR, Dimension.A);
      break;
    }
  }
  
  private RelateGeometry getGeometry(boolean isA) {
    return isA ? geomA : geomB;
  }

  public int getDimension(boolean isA) {
    return getGeometry(isA).getDimension();
  }
  
  public boolean isAreaArea() {
    return getDimension(RelateGeometry.GEOM_A) == Dimension.A 
        && getDimension(RelateGeometry.GEOM_B) == Dimension.A;
  }
  
  /**
   * Indicates whether the input geometries require self-noding 
   * for correct evaluation of specific spatial predicates. 
   * Self-noding is required for geometries which may 
   * have self-crossing linework,
   * or may have lines lying in the boundary of an area.
   * This causes the coordinates of nodes created by 
   * crossing segments to be computed explicitly.
   * This ensures that node locations match in situations
   * where a self-crossing and mutual crossing occur at the same logical location.
   * The canonical example is a self-crossing line tested against a single segment 
   * identical to one of the crossed segments.
   * 
   * Currently, requiring self-noding prevents noder caching.
   * So it it important to limit the cases which require self-noding.
   * Currently self-noding is required for:
   * <ul>
   * <li>A geoms which require self-noding (lines or GCs, except for single-polygon GCs)
   * <li>B geoms which are mixed A/L GCs
   * </ul>
   * Note that linear B inputs do not require self-noding in all cases.
   * In particular, if A is polygonal then predicates with linear B do not require self-noding.
   * 
   * @return true if self-noding is required
   */
  public boolean isSelfNodingRequired() {
    if (! predicate.requireSelfNoding())
      return false;
    
    if (geomA.isSelfNodingRequired()) 
      return true;
    
    //-- if B is a mixed GC with A and L require full noding
    if (geomB.hasAreaAndLine())
      return true;

    return false;
  }
  
  public boolean isExteriorCheckRequired(boolean isA) {
    return predicate.requireExteriorCheck(isA);
  }
  
  private void updateDim(int locA, int locB, int dimension) {
    //System.out.println(Location.toLocationSymbol(locA) + "/" + Location.toLocationSymbol(locB) + ": " + dimension);
    predicate.updateDimension(locA, locB, dimension);
  }
  
  private void updateDim(boolean isAB, int loc1, int loc2, int dimension) {
    if (isAB) {
      updateDim(loc1, loc2, dimension);
    }
    else {
      // is ordered BA
      updateDim(loc2, loc1, dimension);
    }
  }
  
  public boolean isResultKnown() {
    return predicate.isKnown();
  }
  
  public boolean getResult() {
    return predicate.value();
  }
  
  /**
   * Finalize the evaluation.
   */
  public void finish() {
    predicate.finish();
   }

  private NodeSections getNodeSections(Coordinate nodePt) {
    NodeSections node = nodeMap.get(nodePt);
    if (node == null) {
      node = new NodeSections(nodePt);
      nodeMap.put(nodePt, node);
    }
    return node;
  }
  
  public void addIntersection(NodeSection a, NodeSection b) {
    if (! a.isSameGeometry(b)) {
      updateIntersectionAB(a, b);
    }
    //-- add edges to node to allow full topology evaluation later
    addNodeSections(a, b);
  }
  
  /**
   * Update topology for an intersection between A and B.
   * 
   * @param a the section for geometry A 
   * @param b the section for geometry B
   */
  private void updateIntersectionAB(NodeSection a, NodeSection b) {
    if (NodeSection.isAreaArea(a, b)) {
      updateAreaAreaCross(a, b);
    }
    updateNodeLocation(a, b);
  }

  /**
   * Updates topology for an AB Area-Area crossing node.
   * Sections cross at a node if (a) the intersection is proper 
   * (i.e. in the interior of two segments)
   * or (b) if non-proper then whether the linework crosses
   * is determined by the geometry of the segments on either side of the node.
   * In these situations the area geometry interiors intersect (in dimension 2).
   * 
   * @param a the section for geometry A 
   * @param b the section for geometry B
   */
  private void updateAreaAreaCross(NodeSection a, NodeSection b) {
    boolean isProper = NodeSection.isProper(a, b);
    if (isProper || PolygonNodeTopology.isCrossing(a.nodePt(), 
        a.getVertex(0), a.getVertex(1), 
        b.getVertex(0), b.getVertex(1))) {
      updateDim(Location.INTERIOR, Location.INTERIOR, Dimension.A);
    }
  }
  /**
   * Updates topology for a node at an AB edge intersection.
   * 
   * @param a the section for geometry A 
   * @param b the section for geometry B
   */
  private void updateNodeLocation(NodeSection a, NodeSection b) {
    Coordinate pt = a.nodePt();
    int locA = geomA.locateNode(pt, a.getPolygonal()); 
    int locB = geomB.locateNode(pt, b.getPolygonal()); 
    updateDim(locA, locB, Dimension.P);
  }

  private void addNodeSections(NodeSection ns0, NodeSection ns1) {
    NodeSections sections = getNodeSections(ns0.nodePt());
    sections.addNodeSection(ns0);
    sections.addNodeSection(ns1);
  }
  
  public void addPointOnPointInterior(Coordinate pt) {
    updateDim(Location.INTERIOR, Location.INTERIOR, Dimension.P); 
  }
  
  public void addPointOnPointExterior(boolean isGeomA, Coordinate pt) {
    updateDim(isGeomA, Location.INTERIOR, Location.EXTERIOR, Dimension.P); 
  }
  
  public void addPointOnGeometry(boolean isPointA, int locTarget, int dimTarget, Coordinate pt) {
    //-- update entry for Point interior
    updateDim(isPointA, Location.INTERIOR, locTarget, Dimension.P);
    
    //-- an empty geometry has no points to infer entries from
    if (getGeometry(! isPointA).isEmpty())
      return;
    
    switch (dimTarget) {
    case Dimension.P:
      return;
    case Dimension.L:
      /**
       * Because zero-length lines are handled, 
       * a point lying in the exterior of the line target 
       * may imply either P or L for the Exterior interaction
       */
      //TODO: determine if effective dimension of linear target is L?
      //updateDim(isGeomA, Location.EXTERIOR, locTarget, Dimension.P); 
      return;
    case Dimension.A:
      /**
       * If a point intersects an area target, then the area interior and boundary
       * must extend beyond the point and thus interact with its exterior.
       */
      updateDim(isPointA, Location.EXTERIOR, Location.INTERIOR, Dimension.A);      
      updateDim(isPointA, Location.EXTERIOR, Location.BOUNDARY, Dimension.L);      
      return;
    }
    throw new IllegalStateException("Unknown target dimension: " + dimTarget);
  }
  
  /**
   * Add topology for a line end.
   * The line end point must be "significant";
   * i.e. not contained in an area if the source is a mixed-dimension GC.
   * 
   * @param isLineA the input containing the line end
   * @param locLineEnd the location of the line end (Interior or Boundary)
   * @param locTarget the location on the target geometry
   * @param dimTarget the dimension of the interacting target geometry element,
   *    (if any), or the dimension of the target
   * @param pt the line end coordinate
   */
  public void addLineEndOnGeometry(boolean isLineA, int locLineEnd, int locTarget, int dimTarget, Coordinate pt) {
    //-- record topology at line end point
    updateDim(isLineA, locLineEnd, locTarget, Dimension.P);
    
    //-- an empty geometry has no points to infer entries from
    if (getGeometry(! isLineA).isEmpty())
      return;

    //-- Line and Area targets may have additional topology
    switch (dimTarget) {
    case Dimension.P:
      return;
    case Dimension.L:
      addLineEndOnLine(isLineA, locLineEnd, locTarget, pt);
      return;
    case Dimension.A:
      addLineEndOnArea(isLineA, locLineEnd, locTarget, pt);      
      return;
    }
    throw new IllegalStateException("Unknown target dimension: " + dimTarget);
  }

  private void addLineEndOnLine(boolean isLineA, int locLineEnd, int locLine, Coordinate pt) {
    /**
     * When a line end is in the EXTERIOR of a Line, 
     * some length of the source Line INTERIOR
     * is also in the target Line EXTERIOR. 
     * This works for zero-length lines as well. 
     */
    if (locLine == Location.EXTERIOR) {
      updateDim(isLineA, Location.INTERIOR, Location.EXTERIOR, Dimension.L);      
    }
  }  
  
  private void addLineEndOnArea(boolean isLineA, int locLineEnd, int locArea, Coordinate pt) {
    if (locArea != Location.BOUNDARY) {
      /**
       * When a line end is in an Area INTERIOR or EXTERIOR 
       * some length of the source Line Interior  
       * AND the Exterior of the line
       * is also in that location of the target.
       * NOTE: this assumes the line end is NOT also in an Area of a mixed-dim GC
       */
      //TODO: handle zero-length lines?
      updateDim(isLineA, Location.INTERIOR, locArea, Dimension.L);
      updateDim(isLineA, Location.EXTERIOR, locArea, Dimension.A);     
    }
  }

  /**
   * Adds topology for an area vertex interaction with a target geometry element.
   * Assumes the target geometry element has highest dimension
   * (i.e. if the point lies on two elements of different dimension, 
   * the location on the higher dimension element is provided.
   * This is the semantic provided by {@link RelatePointLocator}.
   * <p>
   * Note that in a GeometryCollection containing overlapping or adjacent polygons,
   * the area vertex location may be INTERIOR instead of BOUNDARY.
   * 
   * @param isAreaA the input that is the area
   * @param locArea the location on the area
   * @param locTarget the location on the target geometry element
   * @param dimTarget the dimension of the target geometry element
   * @param pt the point of interaction
   */
  public void addAreaVertex(boolean isAreaA, int locArea, int locTarget, int dimTarget, Coordinate pt) {
    if (locTarget == Location.EXTERIOR) {
      updateDim(isAreaA, Location.INTERIOR, Location.EXTERIOR, Dimension.A);
      /**
       * If area vertex is on Boundary further topology can be deduced
       * from the neighbourhood around the boundary vertex.
       * This is always the case for polygonal geometries.
       * For GCs, the vertex may be either on boundary or in interior
       * (i.e. of overlapping or adjacent polygons) 
       */
      if (locArea == Location.BOUNDARY) {
        updateDim(isAreaA, Location.BOUNDARY, Location.EXTERIOR, Dimension.L);
        updateDim(isAreaA, Location.EXTERIOR, Location.EXTERIOR, Dimension.A);
      }
      return;
    }
    switch (dimTarget) {
    case Dimension.P:
      addAreaVertexOnPoint(isAreaA, locArea, pt);
      return;
    case Dimension.L:
      addAreaVertexOnLine(isAreaA, locArea, locTarget, pt);
      return;
    case Dimension.A:
      addAreaVertexOnArea(isAreaA, locArea, locTarget, pt);
      return;
    }
    throw new IllegalStateException("Unknown target dimension: " + dimTarget);
  }
  
  /**
   * Updates topology for an area vertex (in Interior or on Boundary)
   * intersecting a point.
   * Note that because the largest dimension of intersecting target is determined,
   * the intersecting point is not part of any other target geometry, 
   * and hence its neighbourhood is in the Exterior of the target.
   * 
   * @param isAreaA whether the area is the A input
   * @param locArea the location of the vertex in the area
   * @param pt the point at which topology is being updated
   */
  private void addAreaVertexOnPoint(boolean isAreaA, int locArea, Coordinate pt) {
    //-- Assert: locArea != EXTERIOR
    //-- Assert: locTarget == INTERIOR
    /**
     * The vertex location intersects the Point.
     */
    updateDim(isAreaA, locArea, Location.INTERIOR, Dimension.P);
    /**
     * The area interior intersects the point's exterior neighbourhood.
     */
    updateDim(isAreaA, Location.INTERIOR, Location.EXTERIOR, Dimension.A);
    /**
     * If the area vertex is on the boundary, 
     * the area boundary and exterior intersect the point's exterior neighbourhood
     */
    if (locArea == Location.BOUNDARY) {
      updateDim(isAreaA, Location.BOUNDARY, Location.EXTERIOR, Dimension.L);
      updateDim(isAreaA, Location.EXTERIOR, Location.EXTERIOR, Dimension.A);
    }
  }

  private void addAreaVertexOnLine(boolean isAreaA, int locArea, int locTarget, Coordinate pt) {
    //-- Assert: locArea != EXTERIOR
    /**
     * If an area vertex intersects a line, all we know is the 
     * intersection at that point.  
     * e.g. the line may or may not be collinear with the area boundary,
     * and the line may or may not intersect the area interior.
     * Full topology is determined later by node analysis
     */
    updateDim(isAreaA, locArea, locTarget, Dimension.P);
    if (locArea == Location.INTERIOR) {
      /**
       * The area interior intersects the line's exterior neighbourhood.
       */
      updateDim(isAreaA, Location.INTERIOR, Location.EXTERIOR, Dimension.A);      
    }
  }

  public void addAreaVertexOnArea(boolean isAreaA, int locArea, int locTarget, Coordinate pt) {
    if (locTarget == Location.BOUNDARY) {
      if (locArea == Location.BOUNDARY) {
        //-- B/B topology is fully computed later by node analysis
        updateDim(isAreaA, Location.BOUNDARY, Location.BOUNDARY, Dimension.P);
      }
      else {
        // locArea == INTERIOR
        updateDim(isAreaA, Location.INTERIOR, Location.INTERIOR, Dimension.A);
        updateDim(isAreaA, Location.INTERIOR, Location.BOUNDARY, Dimension.L);
        updateDim(isAreaA, Location.INTERIOR, Location.EXTERIOR, Dimension.A);
      }
    }
    else {  
      //-- locTarget is INTERIOR or EXTERIOR` 
      updateDim(isAreaA, Location.INTERIOR, locTarget, Dimension.A);
      /**
       * If area vertex is on Boundary further topology can be deduced
       * from the neighbourhood around the boundary vertex.
       * This is always the case for polygonal geometries.
       * For GCs, the vertex may be either on boundary or in interior
       * (i.e. of overlapping or adjacent polygons) 
       */
      if (locArea == Location.BOUNDARY) {
        updateDim(isAreaA, Location.BOUNDARY, locTarget, Dimension.L);
        updateDim(isAreaA, Location.EXTERIOR, locTarget, Dimension.A);
      }
    }
  }
  
  public void evaluateNodes() {
    for (NodeSections nodeSections : nodeMap.values()) {
      if (nodeSections.hasInteractionAB()) {
        evaluateNode(nodeSections);
        if (isResultKnown())
          return;
      }
    }
  }

  private void evaluateNode(NodeSections nodeSections) {
    Coordinate p = nodeSections.getCoordinate();
    RelateNode node = nodeSections.createNode();
    //-- Node must have edges for geom, but may also be in interior of a overlapping GC
    boolean isAreaInteriorA = geomA.isNodeInArea(p, nodeSections.getPolygonal(RelateGeometry.GEOM_A));
    boolean isAreaInteriorB = geomB.isNodeInArea(p, nodeSections.getPolygonal(RelateGeometry.GEOM_B));
    node.finish(isAreaInteriorA, isAreaInteriorB);
    evaluateNodeEdges(node);
  }

  private void evaluateNodeEdges(RelateNode node) {
    //TODO: collect distinct dim settings by using temporary matrix?
    for (RelateEdge e : node.getEdges()) {
      //-- An optimization to avoid updates for cases with a linear geometry
      if (isAreaArea()) {
        updateDim(e.location(RelateGeometry.GEOM_A, Position.LEFT), 
                  e.location(RelateGeometry.GEOM_B, Position.LEFT), Dimension.A);
        updateDim(e.location(RelateGeometry.GEOM_A, Position.RIGHT), 
                  e.location(RelateGeometry.GEOM_B, Position.RIGHT), Dimension.A);
      }
      updateDim(e.location(RelateGeometry.GEOM_A, Position.ON), 
                e.location(RelateGeometry.GEOM_B, Position.ON), Dimension.L);
    }
  }

}
