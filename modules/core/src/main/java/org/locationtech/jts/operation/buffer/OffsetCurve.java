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
package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;

/**
 * Computes an offset curve from a geometry.
 * The offset curve is a linear geometry which is offset a specified distance
 * from the input.
 * If the offset distance is positive the curve lies on the left side of the input;
 * if it is negative the curve is on the right side.
 * <p>
 * The offset curve of a line is a {@link LineString} which
 * The offset curve of a Point is an empty {@link LineString}.
 * The offset curve of a Polygon is the boundary of the polygon buffer (which
 * may be a {@link MultiLineString}.
 * For a collection the output is a {@link MultiLineString} of the element offset curves.
 * <p>
 * The offset curve is computed as a single contiguous section of the geometry buffer boundary.
 * In some geometric situations this definition is ill-defined.
 * This algorithm provides a "best-effort" interpretation.
 * In particular:
 * <ul>
 * <li>For self-intersecting lines, the buffer boundary includes
 * offset lines for both left and right sides of the input line.
 * Only a single contiguous portion on the specified side is returned.</li>
 * <li>If the offset corresponds to buffer holes, only the largest hole is used. 
 * </li>
 * </ul>
 * Offset curves support setting the number of quadrant segments, 
 * the join style, and the mitre limit (if applicable) via 
 * the {@link BufferParameters}.
 * 
 * @author Martin Davis
 *
 */
public class OffsetCurve {
  
  /**
   * The nearness tolerance between the raw offset linework and the buffer curve.
   */
  private static final int NEARNESS_FACTOR = 10000;

  /**
   * Computes the offset curve of a geometry at a given distance.
   * 
   * @param geom a geometry
   * @param distance the offset distance (positive = left, negative = right)
   * @return the offset curve
   */
  public static Geometry getCurve(Geometry geom, double distance) {
    OffsetCurve oc = new OffsetCurve(geom, distance);
    return oc.getCurve();
  }
  
  /**
   * Computes the offset curve of a geometry at a given distance,
   * and for a specified quadrant segments, join style and mitre limit.
   * 
   * @param geom a geometry
   * @param distance the offset distance (positive = left, negative = right)
   * @param quadSegs the quadrant segments (-1 for default)
   * @param joinStyle the join style (-1 for default)
   * @param mitreLimit the mitre limit (-1 for default)
   * @return the offset curve
   */
  public static Geometry getCurve(Geometry geom, double distance, int quadSegs, int joinStyle, double mitreLimit) {
    BufferParameters bufferParams = new BufferParameters();
    if (quadSegs >= 0) bufferParams.setQuadrantSegments(quadSegs);
    if (joinStyle >= 0) bufferParams.setJoinStyle(joinStyle);
    if (mitreLimit >= 0) bufferParams.setMitreLimit(mitreLimit);    
    OffsetCurve oc = new OffsetCurve(geom, distance, bufferParams);
    return oc.getCurve();
  }
  
  private Geometry inputGeom;
  private double distance;
  private BufferParameters bufferParams;
  private double matchDistance;
  private GeometryFactory geomFactory;

  /**
   * Creates a new instance for computing an offset curve for a geometryat a given distance.
   * with default quadrant segments ({@link BufferParameters#DEFAULT_QUADRANT_SEGMENTS})
   * and join style ({@link BufferParameters#JOIN_STYLE}).
   * 
   * @param geom the geometry to offset
   * @param distance the offset distance (positive = left, negative = right)
   * 
   * @see BufferParameters
   */
  public OffsetCurve(Geometry geom, double distance) {
    this(geom, distance, null);
  }
  
  /**
   * Creates a new instance for computing an offset curve for a geometry at a given distance.
   * allowing the quadrant segments and join style and mitre limit to be set
   * via {@link BufferParameters}.
   * 
   * @param geom
   * @param distance
   * @param bufParams
   */
  public OffsetCurve(Geometry geom, double distance, BufferParameters bufParams) {
    this.inputGeom = geom;
    this.distance = distance;
    
    matchDistance = Math.abs(distance) / NEARNESS_FACTOR;
    geomFactory = inputGeom.getFactory();
    
    //-- make new buffer params since the end cap style must be the default
    this.bufferParams = new BufferParameters();
    if (bufParams != null) {
      bufferParams.setQuadrantSegments(bufParams.getQuadrantSegments());
      bufferParams.setJoinStyle(bufParams.getJoinStyle());
      bufferParams.setMitreLimit(bufParams.getMitreLimit());
    }
  }
  
  /**
   * Gets the computed offset curve.
   * 
   * @return the offset curve geometry
   */
  public Geometry getCurve() {
    return GeometryMapper.flatMap(inputGeom, 1, new GeometryMapper.MapOp() {
      
      @Override
      public Geometry map(Geometry geom) {
        if (geom instanceof Point) return null;
        if (geom instanceof Polygon ) {
          return toLineString(geom.buffer(distance).getBoundary());
        } 
        return computeCurve((LineString) geom, distance);
      }

      /**
       * Force LinearRings to be LineStrings.
       * 
       * @param geom a geometry which may be a LinearRing
       * @return a geometry which will be a LineString or MultiLineString
       */
      private Geometry toLineString(Geometry geom) {
        if (geom instanceof LinearRing) {
          LinearRing ring = (LinearRing) geom;
          return geom.getFactory().createLineString(ring.getCoordinateSequence());
        }
        return geom;
      }
    });
  }
  
  /**
   * Gets the raw offset line.
   * The quadrant segments and join style and mitre limit to be set
   * via {@link BufferParameters}.
   * <p>
   * The raw offset line may contain loops and other artifacts which are 
   * not present in the true offset curve.
   * The raw offset line is matched to the buffer ring (which is clean)
   * to extract the offset curve.
   * 
   * @param geom the linestring to offset
   * @param distance the offset distance
   * @param bufParams the buffer parameters to use
   * @return the raw offset line
   */
  public static Coordinate[] rawOffset(LineString geom, double distance, BufferParameters bufParams)
  {
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        geom.getFactory().getPrecisionModel(), bufParams
        );
    Coordinate[] pts = ocb.getOffsetCurve(geom.getCoordinates(), distance);
    return pts;
  }
  
  /**
   * Gets the raw offset line, with default buffer parameters.
   * 
   * @param geom the linestring to offset
   * @param distance the offset distance
   * @return the raw offset line
   */
  public static Coordinate[] rawOffset(LineString geom, double distance)
  {
    return rawOffset(geom, distance, new BufferParameters());
  }

  private LineString computeCurve(LineString lineGeom, double distance) {
    //-- first handle special/simple cases
    if (lineGeom.getNumPoints() < 2 || lineGeom.getLength() == 0.0) {
      return geomFactory.createLineString();
    }
    if (lineGeom.getNumPoints() == 2) {
      return offsetSegment(lineGeom.getCoordinates(), distance);
    }

    Coordinate[] rawOffset = rawOffset(lineGeom, distance, bufferParams);
    if (rawOffset.length == 0) {
      return geomFactory.createLineString();
    }
    /**
     * Note: If the raw offset curve has no
     * narrow concave angles or self-intersections it could be returned as is.
     * However, this is likely to be a less frequent situation, 
     * and testing indicates little performance advantage,
     * so not doing this. 
     */
    
    Polygon bufferPoly = getBufferOriented(lineGeom, distance, bufferParams);
    
    //-- first try matching shell to raw curve
    Coordinate[] shell = bufferPoly.getExteriorRing().getCoordinates();
    LineString offsetCurve = computeCurve(shell, rawOffset);
    if (! offsetCurve.isEmpty() 
        || bufferPoly.getNumInteriorRing() == 0)
      return offsetCurve;
    
    //-- if shell didn't work, try matching to largest hole 
    Coordinate[] holePts = extractLongestHole(bufferPoly).getCoordinates();
    offsetCurve = computeCurve(holePts, rawOffset);
    return offsetCurve;
  }

  private LineString offsetSegment(Coordinate[] pts, double distance) {
    LineSegment offsetSeg = (new LineSegment(pts[0], pts[1])).offset(distance);
    return geomFactory.createLineString(new Coordinate[] { offsetSeg.p0, offsetSeg.p1 });
  }

  private static Polygon getBufferOriented(LineString geom, double distance, BufferParameters bufParams) {
    Geometry buffer = BufferOp.bufferOp(geom, Math.abs(distance), bufParams);
    Polygon bufferPoly = extractMaxAreaPolygon(buffer);
    //-- for negative distances (Right of input) reverse buffer direction to match offset curve
    if (distance < 0) {
      bufferPoly = bufferPoly.reverse();
    }
    return bufferPoly;
  }

  /**
   * Extracts the largest polygon by area from a geometry.
   * Used here to avoid issues with non-robust buffer results which have spurious extra polygons.
   * 
   * @param geom a geometry
   * @return the polygon element of largest area
   */
  private static Polygon extractMaxAreaPolygon(Geometry geom) {
    if (geom.getNumGeometries() == 1)
      return (Polygon) geom;
    
    double maxArea = 0;
    Polygon maxPoly = null;
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Polygon poly = (Polygon) geom.getGeometryN(i);
      double area = poly.getArea();
      if (maxPoly == null || area > maxArea) {
        maxPoly = poly;
        maxArea = area;
      }
    }
    return maxPoly;
  }

  private static LinearRing extractLongestHole(Polygon poly) {
    LinearRing largestHole = null;
    double maxLen = -1;
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = poly.getInteriorRingN(i);
      double len = hole.getLength();
      if (len > maxLen) {
        largestHole = hole;
        maxLen = len;
      }
    }
    return largestHole;
  }
  
  private LineString computeCurve(Coordinate[] bufferPts, Coordinate[] rawOffset) {
    boolean[] isInCurve = new boolean[bufferPts.length - 1];
    SegmentMCIndex segIndex = new SegmentMCIndex(bufferPts);
    int curveStart = -1;
    for (int i = 0; i < rawOffset.length - 1; i++) {
      int index = markMatchingSegments(
                      rawOffset[i], rawOffset[i + 1], segIndex, bufferPts, isInCurve);
      if (curveStart < 0) {
        curveStart = index;
      }
    }
    Coordinate[] curvePts = extractSection(bufferPts, curveStart, isInCurve);
    return geomFactory.createLineString(curvePts);
  }
  
  private int markMatchingSegments(Coordinate p0, Coordinate p1, 
      SegmentMCIndex segIndex, Coordinate[] bufferPts, 
      boolean[] isInCurve) {
    Envelope matchEnv = new Envelope(p0, p1);
    matchEnv.expandBy(matchDistance);
    MatchCurveSegmentAction action = new MatchCurveSegmentAction(p0, p1, bufferPts, matchDistance, isInCurve);
    segIndex.query(matchEnv, action);
    return action.getMinCurveIndex();
  }
  
  /**
   * An action to match a raw offset curve segment 
   * to segments in the buffer ring 
   * and mark them as being in the offset curve.
   * 
   * @author Martin Davis
   */
  private static class MatchCurveSegmentAction 
    extends MonotoneChainSelectAction
  {
    private Coordinate p0;
    private Coordinate p1;
    private Coordinate[] bufferPts;
    private double matchDistance;
    private boolean[] isInCurve;
    
    private double minFrac = -1;
    private int minCurveIndex = -1;
    
    public MatchCurveSegmentAction(Coordinate p0, Coordinate p1, 
        Coordinate[] bufferPts, double matchDistance, boolean[] isInCurve) {
      this.p0 = p0;
      this.p1 = p1;
      this.bufferPts = bufferPts;
      this.matchDistance = matchDistance;
      this.isInCurve = isInCurve;
    }
    
    public void select(MonotoneChain mc, int segIndex)
    {
      /**
       * A curveRingPt segment may match all or only a portion of a single raw segment.
       * There may be multiple curve ring segs that match along the raw segment.
       * The one closest to the segment start is recorded as the offset curve start.      
       */
      double frac = subsegmentMatchFrac(bufferPts[segIndex], bufferPts[segIndex+1], p0, p1, matchDistance);
      //-- no match
      if (frac < 0) return;
      
      isInCurve[segIndex] = true;
      
      //-- record lowest index
      if (minFrac < 0 || frac < minFrac) {
        minFrac = frac;
        minCurveIndex = segIndex;
      }
    }
    
    public int getMinCurveIndex() {
      return minCurveIndex;
    }
  }
  
  /*
  // Slower, non-indexed algorithm.  Left here for future testing.
  
  private Coordinate[] OLDcomputeCurve(Coordinate[] curveRingPts, Coordinate[] rawOffset) {
    boolean[] isInCurve = new boolean[curveRingPts.length - 1];
    int curveStart = -1;
    for (int i = 0; i < rawOffset.length - 1; i++) {
      int index = markMatchingSegments(
                      rawOffset[i], rawOffset[i + 1], curveRingPts, isInCurve);
      if (curveStart < 0) {
        curveStart = index;
      }
    }
    Coordinate[] curvePts = extractSection(curveRingPts, isInCurve, curveStart);
    return curvePts;
  }

  private int markMatchingSegments(Coordinate p0, Coordinate p1, Coordinate[] curveRingPts, boolean[] isInCurve) {
    double minFrac = -1;
    int minCurveIndex = -1;
    for (int i = 0; i < curveRingPts.length - 1; i++) {
       // A curveRingPt seg will only match a portion of a single raw segment.
       // But there may be multiple curve ring segs that match along that segment.
       // The one closest to the segment start is recorded.
      double frac = subsegmentMatchFrac(curveRingPts[i], curveRingPts[i+1], p0, p1, matchDistance);
      //-- no match
      if (frac < 0) continue;
      
      isInCurve[i] = true;
      
      //-- record lowest index
      if (minFrac < 0 || frac < minFrac) {
        minFrac = frac;
        minCurveIndex = i;
      }
    }
    return minCurveIndex;
  }
  */
  
  private static double subsegmentMatchFrac(Coordinate p0, Coordinate p1, 
      Coordinate seg0, Coordinate seg1, double matchDistance) {
    if (matchDistance < Distance.pointToSegment(p0, seg0, seg1))
      return -1;
    if (matchDistance < Distance.pointToSegment(p1, seg0, seg1))
      return -1;
    //-- matched - determine position as fraction
    LineSegment seg = new LineSegment(seg0, seg1);
    return seg.segmentFraction(p0);
  }

  /**
   * Extracts a section of a ring of coordinates, starting at a given index, 
   * and keeping coordinates which are flagged as being required.
   * 
   * @param ring the ring of points
   * @param startIndex the index of the start coordinate
   * @param isExtracted flag indicating if coordinate is to be extracted
   * @return
   */
  private static Coordinate[] extractSection(Coordinate[] ring, int startIndex, boolean[] isExtracted) {
    if (startIndex < 0)
      return new Coordinate[0];
    
    CoordinateList coordList = new CoordinateList();
    int i = startIndex;
    do {
      coordList.add(ring[i], false);
      if (! isExtracted[i]) {
        break;
      }
      i = next(i, ring.length - 1);
    } while (i != startIndex);
    //-- handle case where every segment is extracted
    if (isExtracted[i]) {
      coordList.add(ring[i], false);
    }
    
    //-- if only one point found return empty LineString
    if (coordList.size() == 1)
      return new Coordinate[0];
    
    return coordList.toCoordinateArray();  
  }
  
  private static int next(int i, int size) {
    i += 1;
    return (i < size) ? i : 0; 
  }
}
