/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;

/**
 * Creates all the raw offset curves for a buffer of a {@link Geometry}.
 * Raw curves need to be noded together and polygonized to form the final buffer area.
 *
 * @version 1.7
 */
public class BufferCurveSetBuilder {
  
  private Geometry inputGeom;
  private double distance;
  private OffsetCurveBuilder curveBuilder;

  private List curveList = new ArrayList();

  private boolean isInvertOrientation = false;

  public BufferCurveSetBuilder(
      Geometry inputGeom,
          double distance,
          PrecisionModel precisionModel,
          BufferParameters bufParams)
  {
    this.inputGeom = inputGeom;
    this.distance = distance;
    this.curveBuilder = new OffsetCurveBuilder(precisionModel, bufParams);
  }

  /**
   * Sets whether the offset curve is generated 
   * using the inverted orientation of input rings.
   * This allows generating a buffer(0) polygon from the smaller lobes
   * of self-crossing rings.
   * 
   * @param isInvertOrientation true if input ring orientation should be inverted
   */
  void setInvertOrientation(boolean isInvertOrientation) {
    this.isInvertOrientation = isInvertOrientation;
  }
  
  /**
   * Computes orientation of a ring using a signed-area orientation test. 
   * For invalid (self-crossing) rings this ensures the largest enclosed area
   * is taken to be the interior of the ring.
   * This produces a more sensible result when
   * used for repairing polygonal geometry via buffer-by-zero.
   * For buffer  use the lower robustness of orientation-by-area
   * doesn't matter, since narrow or flat rings
   * produce an acceptable offset curve for either orientation.
   * 
   * @param coord the ring coordinates
   * @return true if the ring is CCW
   */
  private boolean isRingCCW(Coordinate[] coord) {
    boolean isCCW = Orientation.isCCWArea(coord);
    //--- invert orientation if required
    if (isInvertOrientation) return ! isCCW;
    return isCCW;
  }
  
  /**
   * Computes the set of raw offset curves for the buffer.
   * Each offset curve has an attached {@link Label} indicating
   * its left and right location.
   *
   * @return a Collection of SegmentStrings representing the raw buffer curves
   */
  public List getCurves()
  {
    add(inputGeom);
    return curveList;
  }

  /**
   * Creates a {@link SegmentString} for a coordinate list which is a raw offset curve,
   * and adds it to the list of buffer curves.
   * The SegmentString is tagged with a Label giving the topology of the curve.
   * The curve may be oriented in either direction.
   * If the curve is oriented CW, the locations will be:
   * <br>Left: Location.EXTERIOR
   * <br>Right: Location.INTERIOR
   */
  private void addCurve(Coordinate[] coord, int leftLoc, int rightLoc)
  {
    // don't add null or trivial curves
    if (coord == null || coord.length < 2) return;
    // add the edge for a coordinate list which is a raw offset curve
    SegmentString e = new NodedSegmentString(coord,
                        new Label(0, Location.BOUNDARY, leftLoc, rightLoc));
    curveList.add(e);
  }


  private void add(Geometry g)
  {
    if (g.isEmpty()) return;

    if (g instanceof Polygon)                 addPolygon((Polygon) g);
                        // LineString also handles LinearRings
    else if (g instanceof LineString)         addLineString((LineString) g);
    else if (g instanceof Point)              addPoint((Point) g);
    else if (g instanceof MultiPoint)         addCollection((MultiPoint) g);
    else if (g instanceof MultiLineString)    addCollection((MultiLineString) g);
    else if (g instanceof MultiPolygon)       addCollection((MultiPolygon) g);
    else if (g instanceof GeometryCollection) addCollection((GeometryCollection) g);
    else  throw new UnsupportedOperationException(g.getClass().getName());
  }
  private void addCollection(GeometryCollection gc)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      add(g);
    }
  }
  /**
   * Add a Point to the graph.
   */
  private void addPoint(Point p)
  {
    // a zero or negative width buffer of a point is empty
    if (distance <= 0.0) 
      return;
    Coordinate[] coord = p.getCoordinates();
    // skip if coordinate is invalid
    if (coord.length >= 1 && ! coord[0].isValid())
      return;
    Coordinate[] curve = curveBuilder.getLineCurve(coord, distance);
    addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
  }
  
  private void addLineString(LineString line)
  {
    if (curveBuilder.isLineOffsetEmpty(distance)) return;
    
    Coordinate[] coord = clean(line.getCoordinates());
    
    //-- skip if no valid coordinates
    if (coord.length == 0)
      return;
    
    /**
     * Rings (closed lines) are generated with a continuous curve, 
     * with no end arcs. This produces better quality linework, 
     * and avoids noding issues with arcs around almost-parallel end segments.
     * See JTS #523 and #518.
     * 
     * Singled-sided buffers currently treat rings as if they are lines.
     */
    if (CoordinateArrays.isRing(coord) && ! curveBuilder.getBufferParameters().isSingleSided()) {
      addLinearRingSides(coord, distance);
    }
    else {
      Coordinate[] curve = curveBuilder.getLineCurve(coord, distance);
      addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
    }
    // TESTING
    //Coordinate[] curveTrim = BufferCurveLoopPruner.prune(curve); 
    //addCurve(curveTrim, Location.EXTERIOR, Location.INTERIOR);
  }
  
  /**
   * Keeps only valid coordinates, and removes repeated points.
   * 
   * @param coordinates the coordinates to clean
   * @return an array of clean coordinates
   */
  private static Coordinate[] clean(Coordinate[] coords) {
    return CoordinateArrays.removeRepeatedOrInvalidPoints(coords);
  }

  private void addPolygon(Polygon p)
  {
    double offsetDistance = distance;
    int offsetSide = Position.LEFT;
    if (distance < 0.0) {
      offsetDistance = -distance;
      offsetSide = Position.RIGHT;
    }

    LinearRing shell = p.getExteriorRing();
    // optimization - don't compute buffer
    // if the polygon would be completely eroded
    if (distance < 0.0 && isRingFullyEroded(shell, false, distance))
        return;
    
    Coordinate[] shellCoords = clean(shell.getCoordinates());
    
    //-- skip if no valid coordinates
    if (shellCoords.length == 0)
      return;
    
    // don't attempt to buffer a polygon with too few distinct vertices
    if (distance <= 0.0 && shellCoords.length < 3)
    	return;

    addPolygonRingSide(
            shellCoords,
            offsetDistance,
            offsetSide,
            Location.EXTERIOR,
            Location.INTERIOR);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {

      LinearRing hole = p.getInteriorRingN(i);
      
      // optimization - don't compute buffer for this hole
      // if the hole would be completely covered
      if (distance > 0.0 && isRingFullyEroded(hole, true, distance))
          continue;

      Coordinate[] holeCoords = clean(hole.getCoordinates());

      //-- skip if no valid coordinates
      if (holeCoords.length == 0)
        continue;

      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CCW)
      addPolygonRingSide(
            holeCoords,
            offsetDistance,
            Position.opposite(offsetSide),
            Location.INTERIOR,
            Location.EXTERIOR);
    }
  }
  
  /**
   * Adds an offset curve for one side of a polygon ring.
   * The side and left and right topological location arguments
   * are provided as if the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations are interchanged and the side is flipped.
   *
   * @param coord the coordinates of the ring (must not contain repeated points)
   * @param offsetDistance the positive distance at which to create the buffer
   * @param side the side {@link Position} of the ring on which to construct the buffer line
   * @param cwLeftLoc the location on the L side of the ring (if it is CW)
   * @param cwRightLoc the location on the R side of the ring (if it is CW)
   */
  private void addPolygonRingSide(Coordinate[] coord, double offsetDistance, int side, int cwLeftLoc, int cwRightLoc)
  {
    // don't bother adding ring if it is "flat" and will disappear in the output
    if (offsetDistance == 0.0 && coord.length < LinearRing.MINIMUM_VALID_SIZE)
      return;
    
    int leftLoc  = cwLeftLoc;
    int rightLoc = cwRightLoc;
    boolean isCCW = isRingCCW(coord);
    if (coord.length >= LinearRing.MINIMUM_VALID_SIZE 
      && isCCW) {
      leftLoc = cwRightLoc;
      rightLoc = cwLeftLoc;
      side = Position.opposite(side);
    }
    addRingSide(coord, offsetDistance, side, leftLoc, rightLoc);
  }
  
  /**
   * Add both sides of a linear ring.
   * Checks for erosion of the hole side.
   * 
   * @param coord ring vertices
   * @param distance offset distance (must be non-zero positive)
   */
  private void addLinearRingSides(Coordinate[] coord, double distance)
  {
    /*
     * (f "hole" side will be eroded completely, avoid generating it.
     * This prevents hole artifacts (e.g. https://github.com/libgeos/geos/issues/1223)
     */
    //-- distance is assumed > 0, due to previous checks
    boolean isHoleComputed = ! isRingFullyEroded(coord, CoordinateArrays.envelope(coord), true, distance);
    
    boolean isCCW = isRingCCW(coord);
    
    boolean isShellLeft = ! isCCW;
    if (isShellLeft || isHoleComputed) {
      addRingSide(coord, distance,
        Position.LEFT, 
        Location.EXTERIOR, Location.INTERIOR);
    }
    boolean isShellRight = isCCW;
    if (isShellRight || isHoleComputed) {
      addRingSide(coord, distance,
        Position.RIGHT,
        Location.INTERIOR, Location.EXTERIOR);
    }
  }
  
  private void addRingSide(Coordinate[] coord, double offsetDistance, int side, int leftLoc, int rightLoc)
  {
    Coordinate[] curve = curveBuilder.getRingCurve(coord, side, offsetDistance);
    /**
     * If the offset curve has inverted completely it will produce
     * an unwanted artifact in the result, so skip it. 
     */
    if (isRingCurveInverted(coord, offsetDistance, curve)) {
      return;
    }
    addCurve(curve, leftLoc, rightLoc);
  }

  private static final int MAX_INVERTED_RING_SIZE = 9;
  private static final int INVERTED_CURVE_VERTEX_FACTOR = 4;
  private static final double NEARNESS_FACTOR = 0.99;

  /**
   * Tests whether the offset curve for a ring is fully inverted. 
   * An inverted ("inside-out") curve occurs in some specific situations 
   * involving a buffer distance which should result in a fully-eroded (empty) buffer.
   * It can happen that the sides of a small, convex polygon 
   * produce offset segments which all cross one another to form
   * a curve with inverted orientation.
   * This happens at buffer distances slightly greater than the distance at 
   * which the buffer should disappear.
   * The inverted curve will produce an incorrect non-empty buffer (for a shell)
   * or an incorrect hole (for a hole).
   * It must be discarded from the set of offset curves used in the buffer.
   * Heuristics are used to reduce the number of cases which area checked,
   * for efficiency and correctness.
   * <p>
   * See https://github.com/locationtech/jts/issues/472
   * 
   * @param inputRing the input ring
   * @param distance the buffer distance
   * @param curveRing the generated offset curve ring
   * @return true if the offset curve is inverted
   */
  private static boolean isRingCurveInverted(Coordinate[] inputRing, double distance, Coordinate[] curveRing) {
    if (distance == 0.0) return false;
    /**
     * Only proper rings can invert.
     */
    if (inputRing.length <= 3) return false;
   /**
     * Heuristic based on low chance that a ring with many vertices will invert.
     * This low limit ensures this test is fairly efficient.
     */
    if (inputRing.length >= MAX_INVERTED_RING_SIZE) return false;
    
    /**
     * Don't check curves which are much larger than the input.
     * This improves performance by avoiding checking some concave inputs 
     * (which can produce fillet arcs with many more vertices)
     */
    if (curveRing.length > INVERTED_CURVE_VERTEX_FACTOR * inputRing.length) return false;
    
    /**
     * If curve contains points which are on the buffer, 
     * it is not inverted and can be included in the raw curves.
     */
    if (hasPointOnBuffer(inputRing, distance, curveRing))
      return false;
    
    //-- curve is inverted, so discard it
    return true;
  }

  /**
   * Tests if there are points on the raw offset curve which may
   * lie on the final buffer curve
   * (i.e. they are (approximately) at the buffer distance from the input ring). 
   * For efficiency this only tests a limited set of points on the curve.
   * 
   * @param inputRing
   * @param distance
   * @param curveRing
   * @return true if the curve contains points lying at the required buffer distance
   */
  private static boolean hasPointOnBuffer(Coordinate[] inputRing, double distance, Coordinate[] curveRing) {
    double distTol = NEARNESS_FACTOR * Math.abs(distance);
    
    for (int i = 0; i < curveRing.length - 1; i++) {
      Coordinate v = curveRing[i];
      
      //-- check curve vertices
      double dist = Distance.pointToSegmentString(v, inputRing);
      if (dist > distTol) {
        return true; 
      }
      
      //-- check curve segment midpoints
      int iNext = (i < curveRing.length - 1) ? i + 1 : 0;
      Coordinate vnext = curveRing[iNext];
      Coordinate midPt = LineSegment.midPoint(v, vnext);
      
      double distMid = Distance.pointToSegmentString(midPt, inputRing);
      if (distMid > distTol) {
        return true; 
      }
    }
    return false;
  }

  /**
   * Tests whether a ring buffer is eroded completely (is empty)
   * based on simple heuristics.
   * 
   * The ringCoord is assumed to contain no repeated points.
   * It may be degenerate (i.e. contain only 1, 2, or 3 points).
   * In this case it has no area, and hence has a minimum diameter of 0.
   *
   * @param ringCoord
   * @param offsetDistance
   * @return
   */
  private static boolean isRingFullyEroded(LinearRing ring, boolean isHole, double bufferDistance)
  {
    return isRingFullyEroded(ring.getCoordinates(), ring.getEnvelopeInternal(), isHole, bufferDistance);
  }
  
  private static boolean isRingFullyEroded(Coordinate[] ringCoord, Envelope ringEnv, boolean isHole, double bufferDistance)
  {
    // degenerate ring has no area
    if (ringCoord.length < 4)
      return true;

    // important test to eliminate inverted triangle bug
    // also optimizes erosion test for triangles
    if (ringCoord.length == 4)
      return isTriangleErodedCompletely(ringCoord, bufferDistance);

    boolean isErodable = 
        (  isHole && bufferDistance > 0) ||
        (! isHole && bufferDistance < 0);
    
    if (isErodable) {
      //-- if envelope is narrower than twice the buffer distance, ring is eroded
      double envMinDimension = Math.min(ringEnv.getHeight(), ringEnv.getWidth());
      if (2 * Math.abs(bufferDistance) > envMinDimension)
        return true;
    }
    return false;
  }

  /**
   * Tests whether a triangular ring would be eroded completely by the given
   * buffer distance.
   * This is a precise test.  It uses the fact that the inner buffer of a
   * triangle converges on the inCentre of the triangle (the point
   * equidistant from all sides).  If the buffer distance is greater than the
   * distance of the inCentre from a side, the triangle will be eroded completely.
   *
   * This test is important, since it removes a problematic case where
   * the buffer distance is slightly larger than the inCentre distance.
   * In this case the triangle buffer curve "inverts" with incorrect topology,
   * producing an incorrect hole in the buffer.
   *
   * @param triangleCoord
   * @param bufferDistance
   * @return
   */
  private static boolean isTriangleErodedCompletely(
      Coordinate[] triangleCoord,
      double bufferDistance)
  {
    Triangle tri = new Triangle(triangleCoord[0], triangleCoord[1], triangleCoord[2]);
    Coordinate inCentre = tri.inCentre();
    double distToCentre = Distance.pointToSegment(inCentre, tri.p0, tri.p1);
    return distToCentre < Math.abs(bufferDistance);
  }



}
