/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Assert;

/**
 * Utility methods for overlay processing.
 * 
 * @author mdavis
 *
 */
class OverlayUtil {

  /**
   * A null-handling wrapper for {@link PrecisionModel#isFloating()}
   * 
   * @param pm
   * @return
   */
  static boolean isFloating(PrecisionModel pm) {
    if (pm == null) return true;
    return pm.isFloating();
  }
  
  /**
   * Computes a clipping envelope for overlay input geometries.
   * The clipping envelope encloses all geometry line segments which 
   * might participate in the overlay, with a buffer to
   * account for numerical precision 
   * (in particular, rounding due to a precision model.
   * The clipping envelope is used in both the {@link RingClipper} 
   * and in the {@link LineLimiter}.
   * <p>
   * Some overlay operations (i.e. {@link OverlayNG#UNION and OverlayNG#SYMDIFFERENCE}
   * cannot use clipping as an optimization,
   * since the result envelope is the full extent of the two input geometries.
   * In this case the returned
   * envelope is <code>null</code> to indicate this.
   * 
   * @param opCode the overlay op code
   * @param inputGeom the input geometries
   * @param pm the precision model being used
   * @return an envelope for clipping and line limiting, or null if no clipping is performed
   */
  static Envelope clippingEnvelope(int opCode, InputGeometry inputGeom, PrecisionModel pm) {   
    Envelope resultEnv = resultEnvelope(opCode, inputGeom, pm);
    if (resultEnv == null) 
      return null;
    
    Envelope clipEnv = RobustClipEnvelopeComputer.getEnvelope(
        inputGeom.getGeometry(0), 
        inputGeom.getGeometry(1), 
        resultEnv);
    
    Envelope safeEnv = safeEnv( clipEnv, pm );
    return safeEnv;
  }

  /**
   * Computes an envelope which covers the extent of the result of
   * a given overlay operation for given inputs.
   * The operations which have a result envelope smaller than the extent of the inputs
   * are:
   * <ul>
   * <li>{@link OverlayNG#INTERSECTION}: result envelope is the intersection of the input envelopes
   * <li>{@link OverlayNG#DIFERENCE}: result envelope is the envelope of the A input geometry
   * </ul>
   * Otherwise, <code>null</code> is returned to indicate full extent.
   * 
   * @param opCode
   * @param inputGeom
   * @param pm
   * @return the result envelope, or null if the full extent
   */
  private static Envelope resultEnvelope(int opCode, InputGeometry inputGeom, PrecisionModel pm) {
    Envelope overlapEnv = null;
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      // use safe envelopes for intersection to ensure they contain rounded coordinates
      Envelope envA = safeEnv( inputGeom.getEnvelope(0), pm);
      Envelope envB = safeEnv( inputGeom.getEnvelope(1), pm);
      overlapEnv = envA.intersection(envB);   
      break;
    case OverlayNG.DIFFERENCE:
      overlapEnv = safeEnv( inputGeom.getEnvelope(0), pm);
      break;
    }
    // return null for UNION and SYMDIFFERENCE to indicate no clipping
    return overlapEnv;
  }

  /**
   * Determines a safe geometry envelope for clipping,
   * taking into account the precision model being used.
   * 
   * @param env a geometry envelope
   * @param pm the precision model
   * @return a safe envelope to use for clipping
   */
  private static Envelope safeEnv(Envelope env, PrecisionModel pm) {
    double envExpandDist = safeExpandDistance(env, pm);
    Envelope safeEnv = env.copy();
    safeEnv.expandBy(envExpandDist);
    return safeEnv;
  }
  
  private static final double SAFE_ENV_BUFFER_FACTOR = 0.1;

  private static final int SAFE_ENV_GRID_FACTOR = 3;

  private static double safeExpandDistance(Envelope env, PrecisionModel pm) {
    double envExpandDist;
    if (isFloating(pm)) {
      // if PM is FLOAT then there is no scale factor, so add 10%
      double minSize = Math.min(env.getHeight(), env.getWidth());
      // heuristic to ensure zero-width envelopes don't cause total clipping
      if (minSize <= 0.0) {
        minSize = Math.max(env.getHeight(), env.getWidth());
      }
      envExpandDist = SAFE_ENV_BUFFER_FACTOR * minSize;
    }
    else {
      // if PM is fixed, add a small multiple of the grid size
      double gridSize = 1.0 / pm.getScale();
      envExpandDist = SAFE_ENV_GRID_FACTOR * gridSize;
    }
    return envExpandDist;
  }

  
  /**
   * Tests if the result can be determined to be empty
   * based on simple properties of the input geometries
   * (such as whether one or both are empty, 
   * or their envelopes are disjoint).
   * 
   * @param opCode the overlay operation
   * @param inputGeom the input geometries
   * @return true if the overlay result is determined to be empty
   */
  static boolean isEmptyResult(int opCode, Geometry a, Geometry b, PrecisionModel pm) {
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      if (isEnvDisjoint(a, b, pm)) 
        return true;
      break;
    case OverlayNG.DIFFERENCE:
      if ( isEmpty(a) )     
        return true;
      break;
    case OverlayNG.UNION:
    case OverlayNG.SYMDIFFERENCE:
      if ( isEmpty(a) && isEmpty(b) )     
        return true;
      break;
    }
    return false;
  }
  
  private static boolean isEmpty(Geometry geom) {
    return geom == null || geom.isEmpty();
  }

  /**
   * Tests if the geometry envelopes are disjoint, or empty.
   * The disjoint test must take into account the precision model
   * being used, since geometry coordinates may shift under rounding.
   * 
   * @param a a geometry
   * @param b a geometry
   * @param pm the precision model being used
   * @return true if the geometry envelopes are disjoint or empty
   */
  static boolean isEnvDisjoint(Geometry a, Geometry b, PrecisionModel pm) {
    if (isEmpty(a) || isEmpty(b)) return true;
    if (isFloating(pm)) {
      return a.getEnvelopeInternal().disjoint(b.getEnvelopeInternal());
    }
    return isDisjoint(a.getEnvelopeInternal(), b.getEnvelopeInternal(), pm);
  }

  /**
   * Tests for disjoint envelopes adjusting for rounding 
   * caused by a fixed precision model.
   * Assumes envelopes are non-empty.
   * 
   * @param envA an envelope
   * @param envB an envelope
   * @param pm the precision model
   * @return true if the envelopes are disjoint
   */
  private static boolean isDisjoint(Envelope envA, Envelope envB, PrecisionModel pm) {
    if (pm.makePrecise(envB.getMinX()) > pm.makePrecise(envA.getMaxX())) return true;
    if (pm.makePrecise(envB.getMaxX()) < pm.makePrecise(envA.getMinX())) return true;
    if (pm.makePrecise(envB.getMinY()) > pm.makePrecise(envA.getMaxY())) return true;
    if (pm.makePrecise(envB.getMaxY()) < pm.makePrecise(envA.getMinY())) return true;
    return false;
  }

  /**
   * Creates an empty result geometry of the appropriate dimension,
   * based on the given overlay operation and the dimensions of the inputs.
   * The created geometry is an atomic geometry, 
   * not a collection (unless the dimension is -1,
   * in which case a <code>GEOMETRYCOLLECTION EMPTY</code> is created.)
   * 
   * @param dim the dimension of the empty geometry to create
   * @param geomFact the geometry factory being used for the operation
   * @return an empty atomic geometry of the appropriate dimension
   */
  static Geometry createEmptyResult(int dim, GeometryFactory geomFact)
  {
    Geometry result = null;
    switch (dim) {
    case 0:
      result =  geomFact.createPoint();
      break;
    case 1:
      result =  geomFact.createLineString();
      break;
    case 2:
      result =  geomFact.createPolygon();
      break;
    case -1:
      result =  geomFact.createGeometryCollection();
      break;
    default:
      Assert.shouldNeverReachHere("Unable to determine overlay result geometry dimension");
    }
    return result;
  }

  /**
   * Computes the dimension of the result of
   * applying the given operation to inputs
   * with the given dimensions.
   * This assumes that complete collapse does not occur.
   * <p>
   * The result dimension is computed according to the following rules:
   * <ul>
   * <li>{@link OverlayNG#INTERSECTION} - result has the dimension of the lowest input dimension
   * <li>{@link OverlayNG#UNION} - result has the dimension of the highest input dimension
   * <li>{@link OverlayNG#DIFFERENCE} - result has the dimension of the left-hand input
   * <li>{@link OverlayNG#SYMDIFFERENCE} - result has the dimension of the highest input dimension
   * (since the Symmetric Difference is the Union of the Differences).
   * </ul>
   * 
   * @param opCode the overlay operation
   * @param dim0 dimension of the LH input
   * @param dim1 dimension of the RH input
   * @return the dimension of the result
   */
  public static int resultDimension(int opCode, int dim0, int dim1)
  { 
    int resultDimension = -1;
    switch (opCode) {
    case OverlayNG.INTERSECTION: 
      resultDimension = Math.min(dim0, dim1);
      break;
    case OverlayNG.UNION: 
      resultDimension = Math.max(dim0, dim1);
      break;
    case OverlayNG.DIFFERENCE: 
      resultDimension = dim0;
      break;
    case OverlayNG.SYMDIFFERENCE: 
      /**
       * This result is chosen because
       * <pre>
       * SymDiff = Union( Diff(A, B), Diff(B, A) )
       * </pre>
       * and Union has the dimension of the highest-dimension argument.
       */
      resultDimension = Math.max(dim0, dim1);
      break;
    }
    return resultDimension;
  }

  /**
   * Creates an overlay result geometry for homogeneous or mixed components.
   *  
   * @param resultPolyList the list of result polygons (may be empty or null)
   * @param resultLineList the list of result lines (may be empty or null)
   * @param resultPointList the list of result points (may be empty or null)
   * @param geometryFactory the geometry factory to use
   * @return a geometry structured according to the overlay result semantics
   */
  static Geometry createResultGeometry(List<Polygon> resultPolyList, List<LineString> resultLineList, List<Point> resultPointList, GeometryFactory geometryFactory) {
    List<Geometry> geomList = new ArrayList<Geometry>();
    
    // TODO: for mixed dimension, return collection of Multigeom for each dimension (breaking change)
    
    // element geometries of the result are always in the order A,L,P
    if (resultPolyList != null) geomList.addAll(resultPolyList);
    if (resultLineList != null) geomList.addAll(resultLineList);
    if (resultPointList != null) geomList.addAll(resultPointList);
  
    // build the most specific geometry possible
    // TODO: perhaps do this internally to give more control?
    return geometryFactory.buildGeometry(geomList);
  }

  static Geometry toLines(OverlayGraph graph, boolean isOutputEdges, GeometryFactory geomFact) {
    List<LineString> lines = new ArrayList<LineString>();
    for (OverlayEdge edge : graph.getEdges()) {
      boolean includeEdge = isOutputEdges || edge.isInResultArea();
      if (! includeEdge) continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = edge.getCoordinatesOriented();
      LineString line = geomFact.createLineString(pts);
      line.setUserData(labelForResult(edge) );
      lines.add(line);
    }
    return geomFact.buildGeometry(lines);
  }

  private static String labelForResult(OverlayEdge edge) {
    return edge.getLabel().toString(edge.isForward())
        + (edge.isInResultArea() ? " Res" : "");
  }

  /**
   * Round the key point if precision model is fixed.
   * Note: return value is only copied if rounding is performed.
   * 
   * @param pt the Point to round
   * @return the rounded point coordinate, or null if empty
   */
  public static Coordinate round(Point pt, PrecisionModel pm) {
    if (pt.isEmpty()) return null;
    return round( pt.getCoordinate(), pm );
  }
  
  /**
   * Rounds a coordinate if precision model is fixed.
   * Note: return value is only copied if rounding is performed.
   * 
   * @param p the coordinate to round
   * @return the rounded coordinate
   */
  public static Coordinate round(Coordinate p, PrecisionModel pm) {
    if (! isFloating(pm)) {
      Coordinate pRound = p.copy();
      pm.makePrecise(pRound);
      return pRound;
    }
    return p;
  }
  
  private static final double AREA_HEURISTIC_TOLERANCE = 0.1;

  /**
   * A heuristic check for overlay result correctness
   * comparing the areas of the input and result.
   * The heuristic is necessarily coarse, but it detects some obvious issues.
   * (e.g. https://github.com/locationtech/jts/issues/798)
   * <p>
   * <b>Note:</b> - this check is only safe if the precision model is floating.
   * It should also be safe for snapping noding if the distance tolerance is reasonably small.
   * (Fixed precision models can lead to collapse causing result area to expand.)
   * 
   * @param geom0 input geometry 0
   * @param geom1 input geometry 1
   * @param opCode the overlay opcode
   * @param result the overlay result
   * @return true if the result area is consistent
   */
  public static boolean isResultAreaConsistent(Geometry geom0, Geometry geom1, int opCode, Geometry result) {
    if (geom0 == null || geom1 == null) 
      return true;
    
    if (result.getDimension() < 2) return true;
    
    double areaResult = result.getArea();
    double areaA = geom0.getArea();
    double areaB = geom1.getArea();
    
    boolean isConsistent = true;
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      isConsistent = isLess(areaResult, areaA, AREA_HEURISTIC_TOLERANCE) 
                  && isLess(areaResult, areaB, AREA_HEURISTIC_TOLERANCE);
      break;
    case OverlayNG.DIFFERENCE:
      isConsistent = isDifferenceAreaConsistent(areaA, areaB, areaResult, AREA_HEURISTIC_TOLERANCE);
      break;
    case OverlayNG.SYMDIFFERENCE:
      isConsistent = isLess(areaResult, areaA + areaB, AREA_HEURISTIC_TOLERANCE);
      break;
    case OverlayNG.UNION:
      isConsistent = isLess(areaA, areaResult, AREA_HEURISTIC_TOLERANCE) 
                  && isLess(areaB, areaResult, AREA_HEURISTIC_TOLERANCE)
                  && isGreater(areaResult, areaA - areaB, AREA_HEURISTIC_TOLERANCE);
      break;
    }
    return isConsistent;
  }
  
  /**
   * Tests if the area of a difference is greater than the minimum possible difference area.
   * This is a heuristic which will only detect gross overlay errors.
   * @param areaA the area of A
   * @param areaB the area of B
   * @param areaResult the result area
   * @param tolFrac the area tolerance fraction
   * 
   * @return true if the difference area is consistent.
   */
  private static boolean isDifferenceAreaConsistent(double areaA, double areaB, double areaResult, double tolFrac) {
    if (! isLess(areaResult, areaA, tolFrac))
      return false;
    double areaDiffMin = areaA - areaB - tolFrac * areaA;
    return areaResult > areaDiffMin;
  }

  private static boolean isLess(double v1, double v2, double tol) {
    return v1 <= v2 * (1 + tol);
  }
  
  private static boolean isGreater(double v1, double v2, double tol) {
    return v1 >= v2 * (1 - tol);
  }
  
}
