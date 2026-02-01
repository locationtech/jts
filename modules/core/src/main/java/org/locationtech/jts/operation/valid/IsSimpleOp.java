/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.valid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Tests whether a <code>Geometry</code> is simple as defined by the OGC SFS specification.
 * <p>
 * Simplicity is defined for each {@link Geometry} type as follows:
 * <ul>
 * <li><b>Point</b> geometries are simple.
 * <li><b>MultiPoint</b> geometries are simple if every point is unique
 * <li><b>LineString</b> geometries are simple if they do not self-intersect at interior points
 * (i.e. points other than the endpoints).
 * Closed linestrings which intersect only at their endpoints are simple
 * (i.e. valid <b>LinearRings</b>s.
 * <li><b>MultiLineString</b> geometries are simple if
 * their elements are simple and they intersect only at points
 * which are boundary points of both elements.
 * (The notion of boundary points can be user-specified - see below).
 * <li><b>Polygonal</b> geometries have no definition of simplicity.
 * The <code>isSimple</code> code checks if all polygon rings are simple.
 * (Note: this means that <tt>isSimple</tt> cannot be used to test
 * for <i>all</i> self-intersections in <tt>Polygon</tt>s.
 * In order to check if a <tt>Polygonal</tt> geometry has self-intersections,
 * use {@link Geometry#isValid()}).
 * <li><b>GeometryCollection</b> geometries are simple if all their elements are simple.
 * <li>Empty geometries are simple
 * </ul>
 * For {@link Lineal} geometries the evaluation of simplicity
 * can be customized by supplying a {@link BoundaryNodeRule}
 * to define how boundary points are determined.
 * The default is the SFS-standard {@link BoundaryNodeRule#MOD2_BOUNDARY_RULE}.
 * <p>
 * Note that under the <tt>Mod-2</tt> rule, closed <tt>LineString</tt>s (rings)
 * have no boundary.
 * This means that an intersection at the endpoints of
 * two closed LineStrings makes the geometry non-simple.
 * If it is required to test whether a set of <code>LineString</code>s touch
 * only at their endpoints, use {@link BoundaryNodeRule#ENDPOINT_BOUNDARY_RULE}.
 * For example, this can be used to validate that a collection of lines
 * form a topologically valid linear network.
 * <P>
 * By default this class finds a single non-simple location.
 * To find all non-simple locations, set {@link #setFindAllLocations(boolean)}
 * before calling {@link #isSimple()}, and retrieve the locations
 * via {@link #getNonSimpleLocations()}.
 * This can be used to find all intersection points in a linear network.
 *
 * @see BoundaryNodeRule
 * @see Geometry#isValid()
 *
 * @version 1.7
 */
public class IsSimpleOp
{
  /**
   * Tests whether a geometry is simple.
   *
   * @param geom the geometry to test
   * @return true if the geometry is simple
   */
  public static boolean isSimple(Geometry geom) {
    IsSimpleOp op = new IsSimpleOp(geom);
    return op.isSimple();
  }

  /**
   * Gets a non-simple location in a geometry, if any.
   *
   * @param geom the input geometry
   * @return a non-simple location, or null if the geometry is simple
   */
  public static Coordinate getNonSimpleLocation(Geometry geom) {
    IsSimpleOp op = new IsSimpleOp(geom);
    return op.getNonSimpleLocation();
  }

  private final Geometry inputGeom;
  private final boolean isClosedEndpointsInInterior;
  private boolean isFindAllLocations;

  private boolean isSimple = false;
  private List<Coordinate> nonSimplePts;

  /**
   * Creates a simplicity checker using the default SFS Mod-2 Boundary Node Rule
   *
   * @param geom the geometry to test
   */
  public IsSimpleOp(Geometry geom) {
    this(geom, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
  }

  /**
   * Creates a simplicity checker using a given {@link BoundaryNodeRule}
   *
   * @param geom the geometry to test
   * @param boundaryNodeRule the boundary node rule to use.
   */
  public IsSimpleOp(Geometry geom, BoundaryNodeRule boundaryNodeRule)
  {
    this.inputGeom = geom;
    isClosedEndpointsInInterior = ! boundaryNodeRule.isInBoundary(2);
  }

  /**
   * Sets whether all non-simple intersection points
   * will be found.
   *
   * @param isFindAll whether to find all non-simple points
   */
  public void setFindAllLocations(boolean isFindAll) {
    this.isFindAllLocations = isFindAll;
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple()
  {
    compute();
    return isSimple;
  }

  /**
   * Gets the coordinate for an location where the geometry
   * fails to be simple.
   * (i.e. where it has a non-boundary self-intersection).
   *
   * @return a coordinate for the location of the non-boundary self-intersection
   * or null if the geometry is simple
   */
  public Coordinate getNonSimpleLocation()
  {
    compute();
    if (nonSimplePts.size() == 0) return null;
    return nonSimplePts.get(0);
  }

  /**
   * Gets all non-simple intersection locations.
   *
   * @return a list of the coordinates of non-simple locations
   */
  public List<Coordinate> getNonSimpleLocations()
  {
    compute();
    return nonSimplePts;
  }

  private void compute() {
    if (nonSimplePts != null) return;
    nonSimplePts = new ArrayList<Coordinate>();
    isSimple = computeSimple(inputGeom);
  }

  private boolean computeSimple(Geometry geom)
  {
    if (geom.isEmpty()) return true;
    if (geom instanceof Point) return true;
    if (geom instanceof LineString) return isSimpleLinearGeometry(geom);
    if (geom instanceof MultiLineString) return isSimpleLinearGeometry(geom);
    if (geom instanceof MultiPoint) return isSimpleMultiPoint((MultiPoint) geom);
    if (geom instanceof Polygonal) return isSimplePolygonal(geom);
    if (geom instanceof GeometryCollection) return isSimpleGeometryCollection(geom);
    // all other geometry types are simple by definition
    return true;
  }

  private boolean isSimpleMultiPoint(MultiPoint mp)
  {
    if (mp.isEmpty()) return true;
    boolean isSimple = true;
    Set<Coordinate> points = new HashSet<Coordinate>();
    for (int i = 0; i < mp.getNumGeometries(); i++) {
      Point pt = (Point) mp.getGeometryN(i);
      Coordinate p = pt.getCoordinate();
      if (points.contains(p)) {
        nonSimplePts.add(p);
        isSimple = false;
        if (!isFindAllLocations)
          break;
      }
      else
        points.add(p);
    }
    return isSimple;
  }

  /**
   * Computes simplicity for polygonal geometries.
   * Polygonal geometries are simple if and only if
   * all of their component rings are simple.
   *
   * @param geom a Polygonal geometry
   * @return true if the geometry is simple
   */
  private boolean isSimplePolygonal(Geometry geom)
  {
    boolean isSimple = true;
    List<LineString> rings = LinearComponentExtracter.getLines(geom);
    for (Geometry ring : rings) {
      if (! isSimpleLinearGeometry(ring))
      {
        isSimple = false;
        if (!isFindAllLocations)
          break;
      }
    }
    return isSimple;
  }

  /**
   * Semantics for GeometryCollection is
   * simple iff all components are simple.
   *
   * @param geom a geometry collection
   * @return true if the geometry is simple
   */
  private boolean isSimpleGeometryCollection(Geometry geom)
  {
    boolean isSimple = true;
    for (int i = 0; i < geom.getNumGeometries(); i++ ) {
      Geometry comp = geom.getGeometryN(i);
      if (! computeSimple(comp))
      {
        isSimple = false;
        if (!isFindAllLocations)
          break;
      }
    }
    return isSimple;
  }

  private boolean isSimpleLinearGeometry(Geometry geom)
  {
    if (geom.isEmpty()) return true;
    List<SegmentString> segStrings = extractSegmentStrings(geom);
    NonSimpleIntersectionFinder segInt = new NonSimpleIntersectionFinder(isClosedEndpointsInInterior, isFindAllLocations, nonSimplePts);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(segInt);
    noder.computeNodes(segStrings);
    if (segInt.hasIntersection()) {
      return false;
    }
    return true;
  }

  private static List<SegmentString> extractSegmentStrings(Geometry geom) {
    List<SegmentString> segStrings = new ArrayList<SegmentString>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      LineString line = (LineString) geom.getGeometryN(i);
      Coordinate[] trimPts = trimRepeatedPoints(line.getCoordinates());
      if (trimPts != null) {
        SegmentString ss = new BasicSegmentString(trimPts, null);
        segStrings.add(ss);
      }
    }
    return segStrings;
  }

  private static Coordinate[] trimRepeatedPoints(Coordinate[] pts) {
    if (pts.length <= 2)
      return pts;
    
    int len = pts.length;
    boolean hasRepeatedStart = pts[0].equals2D(pts[1]);
    boolean hasRepeatedEnd = pts[len - 1].equals2D(pts[len - 2]);
    if (! hasRepeatedStart && ! hasRepeatedEnd)
      return pts;
    
    //-- trim ends
    int startIndex = 0;
    Coordinate startPt = pts[0];
    while (startIndex < len - 1 && startPt.equals2D(pts[startIndex+1])) {
      startIndex++;
    }
    int endIndex = len-1;
    Coordinate endPt = pts[endIndex];
    while (endIndex > 0 && endPt.equals2D(pts[endIndex - 1])) {
      endIndex--;
    }
    //-- are all points identical?
    if (endIndex - startIndex < 1) {
      return null;
    }
    Coordinate[] trimPts = CoordinateArrays.extract(pts, startIndex, endIndex);
    return trimPts;
  }
  
  private static class NonSimpleIntersectionFinder
  implements SegmentIntersector
  {
    private final boolean isClosedEndpointsInInterior;
    private final boolean isFindAll;

    LineIntersector li = new RobustLineIntersector();
    private final List<Coordinate> intersectionPts;

    public NonSimpleIntersectionFinder(boolean isClosedEndpointsInInterior, boolean isFindAll, List<Coordinate> intersectionPts) {
      this.isClosedEndpointsInInterior =isClosedEndpointsInInterior;
      this.isFindAll = isFindAll;
      this.intersectionPts = intersectionPts;
    }

    /**
     * Tests whether an intersection was found.
     *
     * @return true if an intersection was found
     */
    public boolean hasIntersection()
    {
      return intersectionPts.size() > 0;
    }

    @Override
    public void processIntersections(SegmentString ss0, int segIndex0, SegmentString ss1, int segIndex1) {

      // don't test a segment with itself
      boolean isSameSegString = ss0 == ss1;
      boolean isSameSegment = isSameSegString && segIndex0 == segIndex1;
      if (isSameSegment) return;

      boolean hasInt = findIntersection(ss0, segIndex0, ss1, segIndex1);

      if (hasInt) {
        // found an intersection!
        intersectionPts.add(li.getIntersection(0));
      }
    }

    private boolean findIntersection(SegmentString ss0, int segIndex0,
        SegmentString ss1, int segIndex1) {

      Coordinate p00 = ss0.getCoordinate(segIndex0);
      Coordinate p01 = ss0.getCoordinate(segIndex0 + 1);
      Coordinate p10 = ss1.getCoordinate(segIndex1);
      Coordinate p11 = ss1.getCoordinate(segIndex1 + 1);

      li.computeIntersection(p00, p01, p10, p11);
      if (! li.hasIntersection()) return false;

      /**
       * Check for an intersection in the interior of a segment.
       */
      boolean hasInteriorInt = li.isInteriorIntersection();
      if (hasInteriorInt) return true;

      /**
       * Check for equal segments (which will produce two intersection points).
       * These also intersect in interior points, so are non-simple.
       * (This is not triggered by zero-length segments, since they
       * are filtered out by the MC index).
       */
      boolean hasEqualSegments = li.getIntersectionNum() >= 2;
      if (hasEqualSegments) return true;

      /**
       * Following tests assume non-adjacent segments.
       */
      boolean isSameSegString = ss0 == ss1;
      boolean isAdjacentSegment = isSameSegString && Math.abs(segIndex1 - segIndex0) <= 1;
      if (isAdjacentSegment) return false;

      /**
       * At this point there is a single intersection point
       * which is a vertex in each segString.
       * Classify them as endpoints or interior
       */
      boolean isIntersectionEndpt0 = isIntersectionEndpoint(ss0, segIndex0, li, 0);
      boolean isIntersectionEndpt1 = isIntersectionEndpoint(ss1, segIndex1, li, 1);

      boolean hasInteriorVertexInt = ! (isIntersectionEndpt0 && isIntersectionEndpt1);
      if (hasInteriorVertexInt) return true;

      /**
       * Both intersection vertices must be endpoints.
       * Final check is if one or both of them is interior due
       * to being endpoint of a closed ring.
       * This only applies to different lines
       * (which avoids reporting ring endpoints).
       */
      if (isClosedEndpointsInInterior && !isSameSegString) {
        boolean hasInteriorEndpointInt = ss0.isClosed() || ss1.isClosed();
        if (hasInteriorEndpointInt) return true;
      }
      return false;
    }

    /**
     * Tests whether an intersection vertex is an endpoint of a segment string.
     *
     * @param ss the segmentString
     * @param ssIndex index of segment in segmentString
     * @param li the line intersector
     * @param liSegmentIndex index of segment in intersector
     * @return true if the intersection vertex is an endpoint
     */
    private static boolean isIntersectionEndpoint(SegmentString ss, int ssIndex,
        LineIntersector li, int liSegmentIndex) {
      int vertexIndex = intersectionVertexIndex(li, liSegmentIndex);
      /**
       * If the vertex is the first one of the segment, check if it is the start endpoint.
       * Otherwise check if it is the end endpoint.
       */
      if (vertexIndex == 0) {
        return ssIndex == 0;
      }
      else {
        return ssIndex + 2 == ss.size();
      }
    }

    /**
     * Finds the vertex index in a segment of an intersection
     * which is known to be a vertex.
     *
     * @param li the line intersector
     * @param segmentIndex the intersection segment index
     * @return the vertex index (0 or 1) in the segment vertex of the intersection point
     */
    private static int intersectionVertexIndex(LineIntersector li, int segmentIndex) {
      Coordinate intPt = li.getIntersection(0);
      Coordinate endPt0 = li.getEndpoint(segmentIndex, 0);
      return intPt.equals2D(endPt0) ? 0 : 1;
    }

    @Override
    public boolean isDone() {
      if (isFindAll) return false;
      return intersectionPts.size() > 0;
    }

  }

}
