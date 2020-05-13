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

  private static final int SAFE_ENV_EXPAND_FACTOR = 3;

  static double expandDistance(Envelope env, PrecisionModel pm) {
    double envExpandDist;
    if (pm.isFloating()) {
      // if PM is FLOAT then there is no scale factor, so add 10%
      double minSize = Math.min(env.getHeight(), env.getWidth());
      envExpandDist = 0.1 * minSize;
    }
    else {
      // if PM is fixed, add a small multiple of the grid size
      double gridSize = 1.0 / pm.getScale();
      envExpandDist = SAFE_ENV_EXPAND_FACTOR * gridSize;
    }
    return envExpandDist;
  }

  static Envelope safeOverlapEnv(Envelope env, PrecisionModel pm) {
    double envExpandDist = expandDistance(env, pm);
    Envelope safeEnv = env.copy();
    safeEnv.expandBy(envExpandDist);
    return safeEnv;
  }

  static Envelope clippingEnvelope(int opCode, InputGeometry inputGeom, PrecisionModel pm) {   
    Envelope clipEnv = null;
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      Envelope envA = safeOverlapEnv( inputGeom.getEnvelope(0), pm );
      Envelope envB = safeOverlapEnv( inputGeom.getEnvelope(1), pm );
      clipEnv = envA.intersection(envB);   
      break;
    case OverlayNG.DIFFERENCE:
      clipEnv = safeOverlapEnv( inputGeom.getEnvelope(0), pm );
      break;
    }
    return clipEnv;
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
  static boolean isEmptyResult(int opCode, Geometry a, Geometry b) {
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      if (isEnvDisjoint(a, b)) 
        return true;
      break;
    case OverlayNG.DIFFERENCE:
      if ( a.isEmpty() )     
        return true;
      break;
    case OverlayNG.UNION:
    case OverlayNG.SYMDIFFERENCE:
      if ( a.isEmpty() && b.isEmpty() )     
        return true;
      break;
    }
    return false;
  }
  
  /**
   * Tests if the geometry envelopes are disjoint, or empty.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return true if the geometry envelopes are disjoint or empty
   */
  static boolean isEnvDisjoint(Geometry a, Geometry b) {
    if (a.isEmpty() || b.isEmpty()) return true;
    return a.getEnvelopeInternal().disjoint(b.getEnvelopeInternal());
  }

  /**
   * Creates an empty result geometry of the appropriate dimension,
   * based on the given overlay operation and the dimensions of the inputs.
   * The created geometry is always an atomic geometry, 
   * not a collection.
   * <p>
   * The empty result is constructed using the following rules:
   * <ul>
   * <li>{@link OverlayNG#INTERSECTION} - result has the dimension of the lowest input dimension
   * <li>{@link OverlayNG#UNION} - result has the dimension of the highest input dimension
   * <li>{@link OverlayNG#DIFFERENCE} - result has the dimension of the left-hand input
   * <li>{@link OverlayNG#SYMDIFFERENCE} - result has the dimension of the highest input dimension
   * (since the Symmetric Difference is the Union of the Differences).
   * </ul>
   * 
   * @param overlayOpCode the code for the overlay operation being performed
   * @param a an input geometry
   * @param b an input geometry
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
   * 
   * @param opCode the overlay operation
   * @param dim0 dimension of the LH input
   * @param dim1 dimension of the RH input
   * @return
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
   * @param pt
   * @return
   */
  public static Coordinate round(Point pt, PrecisionModel pm) {
    Coordinate p = pt.getCoordinate().copy();
    if (! pm.isFloating()) {
      pm.makePrecise(p);
    }
    return p;
  }
  
  /*
  private void checkSanity(Geometry result) {
    // for Union, area should be greater than largest of inputs
    double areaA = inputGeom.getGeometry(0).getArea();
    double areaB = inputGeom.getGeometry(1).getArea();
    double area = result.getArea();
    
    // if result is empty probably had a complete collapse, so can't use this check
    if (area == 0) return;
    
    if (opCode == UNION) {
      double minAreaLimit = 0.5 * Math.max(areaA, areaB);
      if (area < minAreaLimit ) {
        throw new TopologyException("Result area sanity issue");
      }
    }
  }
*/
  
}
