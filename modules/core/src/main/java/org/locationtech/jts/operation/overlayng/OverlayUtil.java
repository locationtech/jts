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

  static boolean isEmptyResult(int opCode, InputGeometry inputGeom) {
    switch (opCode) {
    case OverlayNG.INTERSECTION:
      if ( inputGeom.isEmpty(0) || inputGeom.isEmpty(1) )
        return true;
      if (inputGeom.isDisjointEnv()) 
        return true;
    case OverlayNG.DIFFERENCE:
      if ( inputGeom.isEmpty(0) )     
        return true;
    }
    return false;
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

  static Geometry buildResultGeometry(int opcode, List<Polygon> resultPolyList, List<LineString> resultLineList, List<Point> resultPointList, GeometryFactory geometryFactory) {
    List<Geometry> geomList = new ArrayList<Geometry>();
    
    // TODO: return Multi geoms for all output dimensions
    
    // element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPolyList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPointList);
  
    // build the most specific geometry possible
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
