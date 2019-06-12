package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

public class OverlaySR {

  /**
   * The spatial functions supported by this class.
   * These operations implement various boolean combinations of the resultants of the overlay.
   */
    
    /**
     * The code for the Intersection overlay operation.
     */
    public static final int INTERSECTION  = 1;
    
    /**
     * The code for the Union overlay operation.
     */
    public static final int UNION         = 2;
    
    /**
     *  The code for the Difference overlay operation.
     */
    public static final int DIFFERENCE    = 3;
    
    /**
     *  The code for the Symmetric Difference overlay operation.
     */
    public static final int SYMDIFFERENCE = 4;
    
  /**
   * Tests whether a point with a given topological {@link Label}
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   * 
   * @param label the topological label of the point
   * @param opCode the code for the overlay operation to test
   * @return true if the label locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(OverlayLabel label, int opCode)
  {
    int loc0 = label.getLocation(0);
    int loc1 = label.getLocation(1);
    return isResultOfOp(loc0, loc1, opCode);
  }

  /**
   * Tests whether a point with given {@link Location}s
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   *
   * @param loc0 the code for the location in the first geometry 
   * @param loc1 the code for the location in the second geometry 
   * @param overlayOpCode the code for the overlay operation to test
   * @return true if the locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(int loc0, int loc1, int overlayOpCode)
  {
    if (loc0 == Location.BOUNDARY) loc0 = Location.INTERIOR;
    if (loc1 == Location.BOUNDARY) loc1 = Location.INTERIOR;
    switch (overlayOpCode) {
    case INTERSECTION:
      return loc0 == Location.INTERIOR
          && loc1 == Location.INTERIOR;
    case UNION:
      return loc0 == Location.INTERIOR
          || loc1 == Location.INTERIOR;
    case DIFFERENCE:
      return loc0 == Location.INTERIOR
          && loc1 != Location.INTERIOR;
    case SYMDIFFERENCE:
      return   (     loc0 == Location.INTERIOR &&  loc1 != Location.INTERIOR)
            || (     loc0 != Location.INTERIOR &&  loc1 == Location.INTERIOR);
    }
    return false;
  }
  
  /**
   * Computes an overlay operation for 
   * the given geometry arguments.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   */
  public static Geometry overlayOp(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode)
  {
    OverlaySR gov = new OverlaySR(geom0, geom1, pm);
    Geometry geomOv = gov.getResultGeometry(opCode);
    return geomOv;
  }

  private Geometry[] geom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;

  public OverlaySR(Geometry geom0, Geometry geom1, PrecisionModel pm) {
    geom = new Geometry[] { geom0, geom1 };
    this.pm = pm;
    geomFact = geom0.getFactory();
  }  
  
  private Geometry getResultGeometry(int overlayOpCode) {
    Geometry resultGeom = computeOverlay(overlayOpCode);
    return resultGeom;
    //return TESToverlay(overlayOpCode);
  }

  private Geometry TESToverlay(int overlayOpCode) {
    Geometry gr0 = GeometryPrecisionReducer.reduce(geom[0], pm);
    Geometry gr1 = GeometryPrecisionReducer.reduce(geom[1], pm);
    if (overlayOpCode == OverlayOp.UNION) {
      
      // **********  TESTING ONLY  **********
      return gr0.union(gr1);
    }
    else if (overlayOpCode == OverlayOp.INTERSECTION) {
      return gr0.intersection(gr1);
    }
    // MD - will not implement other overlay ops yet
    throw new UnsupportedOperationException();
  }

  private Geometry computeOverlay(int opCode) {
    Collection<SegmentString> edges = node();
    Collection<SegmentString> edgesMerged = merge(edges);
    OverlayGraph graph = buildTopology(edgesMerged);
    //TODO: extract included linework from graph
    markResultAreaEdges(graph, opCode);

    PolygonBuilder polyBuilder = new PolygonBuilder(graph, geomFact);
    List<Geometry> resultPolyList = polyBuilder.getPolygons();

    //TODO: build geometries
    //return toLines(edges, geomFact );
        
    // gather the results from all calculations into a single Geometry for the result set
    Geometry resultGeom = computeGeometry(null, null, resultPolyList, opCode);
    return resultGeom;
  }

  private Geometry computeGeometry(List resultPointList, List resultLineList, List resultPolyList, int opcode) {
    List<Geometry> geomList = new ArrayList<Geometry>();
// element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPointList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPolyList);

//*
    if ( geomList.isEmpty() )
      return createEmptyResult(opcode, geom[0], geom[1], geomFact);
//*/

// build the most specific geometry possible
    return geomFact.buildGeometry(geomList);
  }

  
  /**
   * Creates an empty result geometry of the appropriate dimension,
   * based on the given overlay operation and the dimensions of the inputs.
   * The created geometry is always an atomic geometry, 
   * not a collection.
   * <p>
   * The empty result is constructed using the following rules:
   * <ul>
   * <li>{@link #INTERSECTION} - result has the dimension of the lowest input dimension
   * <li>{@link #UNION} - result has the dimension of the highest input dimension
   * <li>{@link #DIFFERENCE} - result has the dimension of the left-hand input
   * <li>{@link #SYMDIFFERENCE} - result has the dimension of the highest input dimension
   * (since the symmetric Difference is the union of the differences).
   * </ul>
   * 
   * @param overlayOpCode the code for the overlay operation being performed
   * @param a an input geometry
   * @param b an input geometry
   * @param geomFact the geometry factory being used for the operation
   * @return an empty atomic geometry of the appropriate dimension
   */
  public static Geometry createEmptyResult(int overlayOpCode, Geometry a, Geometry b, GeometryFactory geomFact)
  {
    Geometry result = null;
    switch (resultDimension(overlayOpCode, a, b)) {
    case -1:
      result = geomFact.createGeometryCollection();
      break;
    case 0:
      result =  geomFact.createPoint();
      break;
    case 1:
      result =  geomFact.createLineString();
      break;
    case 2:
      result =  geomFact.createPolygon();
      break;
    }
    return result;
  }
  
  private static int resultDimension(int opCode, Geometry g0, Geometry g1)
  {
    int dim0 = g0.getDimension();
    int dim1 = g1.getDimension();
    
    int resultDimension = -1;
    switch (opCode) {
    case INTERSECTION: 
      resultDimension = Math.min(dim0, dim1);
      break;
    case UNION: 
      resultDimension = Math.max(dim0, dim1);
      break;
    case DIFFERENCE: 
      resultDimension = dim0;
      break;
    case SYMDIFFERENCE: 
      /**
       * This result is chosen because
       * <pre>
       * SymDiff = Union(Diff(A, B), Diff(B, A)
       * </pre>
       * and Union has the dimension of the highest-dimension argument.
       */
      resultDimension = Math.max(dim0, dim1);
      break;
    }
    return resultDimension;
  }
  
  
  private Collection<SegmentString> node() {
    OverlayNoder noder = new OverlayNoder(pm);
    noder.add(geom[0], 0);
    noder.add(geom[1], 1);
    Collection<SegmentString> edges = noder.node();
    Collection<SegmentString> mergedEdges = merge(edges);
    return mergedEdges;
  }
  
  private Collection<SegmentString> merge(Collection<SegmentString> edges) {
    // TODO implement merging here
    
    //computeLabelsFromDepths();
    //replaceCollapsedEdges();
    
    return edges;
  }

  private OverlayGraph buildTopology(Collection<SegmentString> edges) {
    OverlayGraph graph = OverlayGraph.buildGraph( edges );
    graph.computeLabelling();
    return graph;
  }

  private void markResultAreaEdges(OverlayGraph graph, int overlayOpCode) {
    for (OverlayEdge edge : graph.getEdges()) {
      edge.markInResultArea(overlayOpCode);
      ((OverlayEdge) edge.sym()).markInResultArea(overlayOpCode);      
    }
  }

  private static Geometry toLines(Collection<SegmentString> segStrings, GeometryFactory geomFact) {
    List lines = new ArrayList();
    for (SegmentString ss : segStrings ) {
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = ss.getCoordinates();
      
      lines.add(geomFact.createLineString(pts));
    }
    return geomFact.buildGeometry(lines);
  }

}
