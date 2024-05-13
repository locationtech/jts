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

import static org.locationtech.jts.operation.relateng.RelateGeometry.GEOM_A;
import static org.locationtech.jts.operation.relateng.RelateGeometry.GEOM_B;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.operation.relate.RelateOp;

/**
 * Computes the value of topological predicates between two geometries based on the 
 * <a href="https://en.wikipedia.org/wiki/DE-9IM">Dimensionally-Extended 9-Intersection Model</a> (DE-9IM).
 * Standard and custom topological predicates are provided by {@link RelatePredicate}.
 * <p>
 * The RelateNG algorithm has the following capabilities:
 * <ol>
 * <li>Efficient short-circuited evaluation of topological predicates
 *     (including matching custom DE-9IM matrix patterns)
 * <li>Optimized repeated evaluation of predicates against a single geometry 
 *     via cached spatial indexes (AKA "prepared mode")
 * <li>Robust computation (only point-local topology is required,
 *     so invalid geometry topology does not cause failures)
 * <li>{@link GeometryCollection} inputs containing mixed types and overlapping polygons
 *     are supported, using <i>union semantics</i>.
 * <li>Zero-length LineStrings are treated as being topologically identical to Points.
 * <li>Support for {@link BoundaryNodeRule}s.
 * </ol>
 * 
 * See {@link IntersectionMatrixPattern} for a description of DE-9IM patterns.
 * 
 * If not specified, the standard {@link BoundaryNodeRule#MOD2_BOUNDARY_RULE} is used.
 * 
 * RelateNG operates in 2D only; it ignores any Z ordinates.
 * 
 * This implementation replaces {@link RelateOp} and {@link PreparedGeometry}.
 * 
 * <h3>FUTURE WORK</h3>
 * <ul>
 * <li>Support for a distance tolerance to provide "approximate" predicate evaluation
 * </ul>
 * 
 * 
 * @author Martin Davis
 *
 * @see RelateOp
 * @see PreparedGeometry
 */
public class RelateNG 
{
 
  /**
   * Tests whether the topological relationship between two geometries
   * satisfies a topological predicate.
   * 
   * @param a the A input geometry
   * @param b the A input geometry
   * @param pred the topological predicate
   * @return true if the topological relationship is satisfied
   */
  public static boolean relate(Geometry a, Geometry b, TopologyPredicate pred) {
    RelateNG rng = new RelateNG(a, false);
    return rng.evaluate(b, pred);
  } 
  
  /**
   * Tests whether the topological relationship between two geometries
   * satisfies a topological predicate,
   * using a given {@link BoundaryNodeRule}.
   * 
   * @param a the A input geometry
   * @param b the A input geometry
   * @param pred the topological predicate
   * @param bnRule the Boundary Node Rule to use
   * @return true if the topological relationship is satisfied
   */
  public static boolean relate(Geometry a, Geometry b, TopologyPredicate pred, BoundaryNodeRule bnRule) {
    RelateNG rng = new RelateNG(a, false, bnRule);
    return rng.evaluate(b, pred);
  } 
  
  /**
   * Tests whether the topological relationship to a geometry 
   * matches a DE-9IM matrix pattern.
   * 
   * @param a the A input geometry
   * @param b the A input geometry
   * @param imPattern the DE-9IM pattern to match
   * @return true if the geometries relationship matches the DE-9IM pattern
   * 
   * @see IntersectionMatrixPattern
   */
  public static boolean relate(Geometry a, Geometry b, String imPattern) {
    RelateNG rng = new RelateNG(a, false);
    return rng.evaluate(b, imPattern); 
  }

  /**
   * Computes the DE-9IM matrix 
   * for the topological relationship between two geometries.
   * 
   * @param a the A input geometry
   * @param b the A input geometry
   * @return the DE-9IM matrix for the topological relationship
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b) {
    RelateNG rng = new RelateNG(a, false);
    return rng.evaluate(b);
  } 
  
  /**
   * Computes the DE-9IM matrix 
   * for the topological relationship between two geometries.
   * 
   * @param a the A input geometry
   * @param b the A input geometry
   * @param bnRule the Boundary Node Rule to use
   * @return the DE-9IM matrix for the relationship
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b, BoundaryNodeRule bnRule) {
    RelateNG rng = new RelateNG(a, false, bnRule);
    return rng.evaluate(b); 
  }
  
  /**
   * Creates a prepared RelateNG instance to optimize the
   * evaluation of relationships against a single geometry.
   * 
   * @param a the A input geometry
   * @return a prepared instance
   */
  public static RelateNG prepare(Geometry a) {
    return new RelateNG(a, true);
  }
  
  /**
   * Creates a prepared RelateNG instance to optimize the
   * computation of predicates against a single geometry,
   * using a given {@link BoundaryNodeRule}.
   * 
   * @param a the A input geometry
   * @param bnRule the required BoundaryNodeRule
   * @return a prepared instance
   */
  public static RelateNG prepare(Geometry a, BoundaryNodeRule bnRule) {
    return new RelateNG(a, true, bnRule);
  }
  
  private BoundaryNodeRule boundaryNodeRule;
  private RelateGeometry geomA;
  private MCIndexSegmentSetMutualIntersector edgeMutualInt;
  
  private RelateNG(Geometry inputA, boolean isPrepared) {
    this(inputA, isPrepared, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }
  
  private RelateNG(Geometry inputA, boolean isPrepared, BoundaryNodeRule bnRule) {
    this.boundaryNodeRule = bnRule;
    geomA = new RelateGeometry(inputA, isPrepared, boundaryNodeRule);
  }
  
  /**
   * Computes the DE-9IM matrix for the topological relationship to a geometry.
   * 
   * @param b the B geometry to test against
   * @return the DE-9IM matrix
   */
  public IntersectionMatrix evaluate(Geometry b) {
    RelateMatrixPredicate rel = new RelateMatrixPredicate();
    evaluate(b, rel); 
    return rel.getIM();
  }   
  
  /**
   * Tests whether the topological relationship to a geometry 
   * matches a DE-9IM matrix pattern.
   * 
   * @param b the B geometry to test against
   * @param imPattern the DE-9IM pattern to match
   * @return true if the geometries' topological relationship matches the DE-9IM pattern
   * 
   * @see IntersectionMatrixPattern
   */
  public boolean evaluate(Geometry b, String imPattern) {
    return evaluate(b, RelatePredicate.matches(imPattern)); 
  }
  
  /**
   * Tests whether the topological relationship to a geometry
   * satisfies a topology predicate.
   * 
   * @param b the B geometry to test against
   * @param predicate the topological predicate
   * @return true if the predicate is satisfied
   */
  public boolean evaluate(Geometry b, TopologyPredicate predicate) {

    RelateGeometry geomB = new RelateGeometry(b, boundaryNodeRule);
    
    if (geomA.isEmpty() && geomB.isEmpty()) {
      //TODO: what if predicate is disjoint?  Perhaps use result on disjoint envs?
      return finishValue(predicate);
    }
    int dimA = geomA.getDimensionEffective();
    int dimB = geomB.getDimensionEffective();
    
    //-- check if predicate is determined by dimension or envelope
    predicate.init(dimA, dimB);
    if (predicate.isKnown())
      return finishValue(predicate);
    
    predicate.init(geomA.getEnvelope(), geomB.getEnvelope());
    if (predicate.isKnown())
      return finishValue(predicate);
    
    TopologyComputer topoComputer = new TopologyComputer(predicate, geomA, geomB);
    
    //-- optimized P/P evaluation
    if (dimA == Dimension.P && dimB == Dimension.P) {
      computePP(geomB, topoComputer);
      topoComputer.finish();
      return topoComputer.getResult();
    }

    //-- test points against (potentially) indexed geometry first
    computeAtPoints(geomB, GEOM_B, geomA, topoComputer);
    if (topoComputer.isResultKnown()) {
      return topoComputer.getResult();
    }   
    computeAtPoints(geomA, GEOM_A, geomB, topoComputer);
    if (topoComputer.isResultKnown()) {
      return topoComputer.getResult();
    }   
    
    if (geomA.hasEdges() && geomB.hasEdges()) {
      computeAtEdges(geomB, topoComputer);
    }

    //-- after all processing, set remaining unknown values in IM
    topoComputer.finish();
    return topoComputer.getResult();
  }

  private boolean finishValue(TopologyPredicate predicate) {
    predicate.finish();
    return predicate.value();
  }

  /**
   * An optimized algorithm for evaluating P/P cases.
   * It tests one point set against the other.
   * 
   * @param geomB
   * @param topoComputer
   */
  private void computePP(RelateGeometry geomB, TopologyComputer topoComputer) {
    Set<Coordinate> ptsA = geomA.getUniquePoints();
    //TODO: only query points in interaction extent? 
    Set<Coordinate> ptsB = geomB.getUniquePoints();
    
    int numBinA = 0;
    for (Coordinate ptB : ptsB) {
      if (ptsA.contains(ptB)) {
        numBinA++;
        topoComputer.addPointOnPointInterior(ptB);
      }
      else {
        topoComputer.addPointOnPointExterior(GEOM_B, ptB);
      }
      if (topoComputer.isResultKnown()) {
        return;
      }
    }
    /**
     * If number of matched B points is less than size of A, 
     * there must be at least one A point in the exterior of B
     */
    if (numBinA < ptsA.size()) {
      //TODO: determine actual exterior point?
      topoComputer.addPointOnPointExterior(GEOM_A, null);
    }
  }  

  private void computeAtPoints(RelateGeometry geomSrc, boolean isA, 
      RelateGeometry geomTarget, TopologyComputer topoComputer) {
    
    boolean isResultKnown = false;
    isResultKnown = computePoints(geomSrc, isA, geomTarget, topoComputer);
    if (isResultKnown) 
      return;
    
    isResultKnown = computeLineEnds(geomSrc, isA, geomTarget, topoComputer);
    if (isResultKnown) 
      return;
    
    computeAreaVertices(geomSrc, isA, geomTarget, topoComputer);
  }
  
  private boolean computePoints(RelateGeometry geom, boolean isA, RelateGeometry geomTarget,
      TopologyComputer topoComputer) { 
    if (! geom.hasDimension(Dimension.P)) {
      return false;
    }
    
    List<Point> points = geom.getEffectivePoints();
    for (Point point : points) {
      //TODO: exit when all possible target locations (E,I,B) have been found?
      if (point.isEmpty())
        continue;
      
      Coordinate pt = point.getCoordinate();
      computePoint(isA, pt, geomTarget, topoComputer);
      if (topoComputer.isResultKnown()) {
        return true;
      }
    }
    return false;
  }

  private void computePoint(boolean isA, Coordinate pt, RelateGeometry geomTarget, TopologyComputer topoComputer) {
      int locDimTarget = geomTarget.locateWithDim(pt);
      int locTarget = DimensionLocation.location(locDimTarget);
      int dimTarget = DimensionLocation.dimension(locDimTarget, topoComputer.getDimension(! isA));
      topoComputer.addPointOnGeometry(isA, locTarget, dimTarget, pt);
  }
  
  private boolean computeLineEnds(RelateGeometry geom, boolean isA, RelateGeometry geomTarget,
      TopologyComputer topoComputer) {
    if (! geom.hasDimension(Dimension.L)) {
      return false;
    }
    
    boolean hasExteriorIntersection = false;
    Iterator geomi = new GeometryCollectionIterator(geom.getGeometry());
    while (geomi.hasNext()) {
      Geometry elem = (Geometry) geomi.next();
      if (elem.isEmpty()) 
        continue;
      
      if (elem instanceof LineString) {
        //-- once an intersection with target exterior is recorded, skip further known exterior points
        if (hasExteriorIntersection 
            && elem.getEnvelopeInternal().disjoint(geomTarget.getEnvelope()))
          continue;
       
        LineString line = (LineString) elem;
        //TODO: add optimzation to skip disjoint elements once exterior point found
        Coordinate e0 = line.getCoordinateN(0);
        hasExteriorIntersection |= computeLineEnd(geom, isA, e0, geomTarget, topoComputer);
        if (topoComputer.isResultKnown()) {
          return true;
        }

        if (! line.isClosed()) {
          Coordinate e1 = line.getCoordinateN(line.getNumPoints() - 1);
          hasExteriorIntersection |= computeLineEnd(geom, isA, e1, geomTarget, topoComputer);          
          if (topoComputer.isResultKnown()) {
            return true;
          }
        }
        //TODO: break when all possible locations have been found?
      }
    }
    return false;
  }

  private boolean computeLineEnd(RelateGeometry geom, boolean isA, Coordinate pt,
      RelateGeometry geomTarget, TopologyComputer topoComputer) {
    int locLineEnd = geom.locateLineEnd(pt);
    int locDimTarget = geomTarget.locateWithDim(pt);
    int locTarget = DimensionLocation.location(locDimTarget);
    int dimTarget = DimensionLocation.dimension(locDimTarget, topoComputer.getDimension(! isA));
    topoComputer.addLineEndOnGeometry(isA, locLineEnd, locTarget, dimTarget, pt);
    return locTarget == Location.EXTERIOR;
  }

  private boolean computeAreaVertices(RelateGeometry geom, boolean isA, RelateGeometry geomTarget, TopologyComputer topoComputer) {
    if (! geom.hasDimension(Dimension.A)) {
      return false;
    }
    //-- evaluate for line and area targets only, since points are handled in the reverse direction
    if (geomTarget.getDimension() < Dimension.L)
      return false;

    boolean hasExteriorIntersection = false;
    Iterator geomi = new GeometryCollectionIterator(geom.getGeometry());
    while (geomi.hasNext()) {
      Geometry elem = (Geometry) geomi.next();
      if (elem.isEmpty()) 
        continue;
      
      if (elem instanceof Polygon) {
        //-- once an intersection with target exterior is recorded, skip further known exterior points
        if (hasExteriorIntersection 
            && elem.getEnvelopeInternal().disjoint(geomTarget.getEnvelope()))
          continue;
        
        Polygon poly = (Polygon) elem;
        hasExteriorIntersection |= computeAreaVertex(geom, isA, poly.getExteriorRing(), geomTarget, topoComputer);
        if (topoComputer.isResultKnown()) {
          return true;
        }
        for (int j = 0; j < poly.getNumInteriorRing(); j++) {
          hasExteriorIntersection |= computeAreaVertex(geom, isA, poly.getInteriorRingN(j), geomTarget, topoComputer);        
          if (topoComputer.isResultKnown()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean computeAreaVertex(RelateGeometry geom, boolean isA, LinearRing ring, RelateGeometry geomTarget, TopologyComputer topoComputer) {
    //TODO: use extremal (highest) point to ensure one is on boundary of polygon cluster
    Coordinate pt = ring.getCoordinate();
    
    int locArea = geom.locateAreaVertex(pt);
    int locDimTarget = geomTarget.locateWithDim(pt);
    int locTarget = DimensionLocation.location(locDimTarget);
    int dimTarget = DimensionLocation.dimension(locDimTarget, topoComputer.getDimension(! isA));
    topoComputer.addAreaVertex(isA, locArea, locTarget, dimTarget, pt);
    return locTarget == Location.EXTERIOR;
  }

  private void computeAtEdges(RelateGeometry geomB, TopologyComputer topoComputer) {
    Envelope envInt = geomA.getEnvelope().intersection(geomB.getEnvelope());
    if (envInt.isNull())
      return;
    
    List<RelateSegmentString> edgesB = geomB.extractSegmentStrings(GEOM_B, envInt);
    EdgeSegmentIntersector intersector = new EdgeSegmentIntersector(topoComputer);
    
    if (topoComputer.isSelfNodingRequired()) {
      computeEdgesAll(edgesB, envInt, intersector);      
    }
    else {
      computeEdgesMutual(edgesB, envInt, intersector);
    }
    if (topoComputer.isResultKnown()) {
      return;
    }
    
    topoComputer.evaluateNodes();
  }
  
  private void computeEdgesAll(List<RelateSegmentString> edgesB, Envelope envInt, EdgeSegmentIntersector intersector) {
    //TODO: find a way to reuse prepared index?
    List<RelateSegmentString> edgesA = geomA.extractSegmentStrings(GEOM_A, envInt);
    
    EdgeSetIntersector edgeInt = new EdgeSetIntersector(edgesA, edgesB, envInt);
    edgeInt.process(intersector);
  }
  
  private void computeEdgesMutual(List<RelateSegmentString> edgesB, Envelope envInt, EdgeSegmentIntersector intersector) {
    //-- in prepared mode the A edge index is reused
    if (edgeMutualInt == null) {  
      Envelope envExtract = geomA.isPrepared() ? null : envInt;
      List<RelateSegmentString> edgesA = geomA.extractSegmentStrings(GEOM_A, envExtract);
      edgeMutualInt = new MCIndexSegmentSetMutualIntersector(edgesA, envExtract);
    }
    
    edgeMutualInt.process(edgesB, intersector);
  }


}
