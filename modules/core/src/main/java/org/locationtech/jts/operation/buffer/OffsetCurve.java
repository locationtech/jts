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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;
import org.locationtech.jts.util.Assert;

/**
 * Computes an offset curve from a geometry.
 * An offset curve is a linear geometry which is offset a given distance
 * from the input.
 * If the offset distance is positive the curve lies on the left side of the input;
 * if it is negative the curve is on the right side.
 * The curve(s) have the same direction as the input line(s).
 * The result for a zero offset distance is a copy of the input linework.
 * <p>
 * The offset curve is based on the boundary of the buffer for the geometry
 * at the offset distance (see {@link BufferOp}.
 * The normal mode of operation is to return the sections of the buffer boundary
 * which lie on the raw offset curve
 * (obtained via {@link #rawOffset(LineString, double)}.  
 * The offset curve will contain multiple sections 
 * if the input self-intersects or has close approaches.
 * The computed sections are ordered along the raw offset curve.
 * Sections are disjoint.  They never self-intersect, but may be rings.
 * <ul>
 * <li>For a {@link LineString} the offset curve is a linear geometry
 * ({@link LineString} or {@link MultiLineString}).
 * <li>For a {@link Point} or {@link MultiPoint} the offset curve is an empty {@link LineString}.
 * <li>For a {@link Polygon} the offset curve is the boundary of the polygon buffer (which
 * may be a {@link MultiLineString}.
 * <li>For a collection the output is a {@link MultiLineString} containing the offset curves of the elements.
 * </ul>
 * In "joined" mode (see {@link #setJoined(boolean)}
 * the sections computed for each input line are joined into a single offset curve line. 
 * The joined curve may self-intersect. 
 * At larger offset distances the curve may contain "flat-line" artifacts 
 * in places where the input self-intersects.
 * <p>
 * Offset curves support setting the number of quadrant segments, 
 * the join style, and the mitre limit (if applicable) via 
 * the {@link BufferParameters}.
 * 
 * @author Martin Davis
 *
 */
public class OffsetCurve {
  
  /**
   * The nearness tolerance for matching the the raw offset linework and the buffer curve.
   */
  private static final int MATCH_DISTANCE_FACTOR = 10000;
  
  /**
   * A QuadSegs minimum value that will prevent generating
   * unwanted offset curve artifacts near end caps.
   */
  private static final int MIN_QUADRANT_SEGMENTS = 8;

  /**
   * Computes the offset curve of a geometry at a given distance.
   * 
   * @param geom a geometry
   * @param distance the offset distance (positive for left, negative for right)
   * @return the offset curve
   */
  public static Geometry getCurve(Geometry geom, double distance) {
    OffsetCurve oc = new OffsetCurve(geom, distance);
    return oc.getCurve();
  }
  
  /**
   * Computes the offset curve of a geometry at a given distance,
   * with specified quadrant segments, join style and mitre limit.
   * 
   * @param geom a geometry
   * @param distance the offset distance (positive for left, negative for right)
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
  
  /**
   * Computes the offset curve of a geometry at a given distance,
   * joining curve sections into a single line for each input line.
   * 
   * @param geom a geometry
   * @param distance the offset distance (positive for left, negative for right)
   * @return the joined offset curve
   */
  public static Geometry getCurveJoined(Geometry geom, double distance) {
    OffsetCurve oc = new OffsetCurve(geom, distance);
    oc.setJoined(true);
    return oc.getCurve();
  }
  
  private Geometry inputGeom;
  private double distance;
  private boolean isJoined = false;
  
  private BufferParameters bufferParams;
  private double matchDistance;
  private GeometryFactory geomFactory;

  /**
   * Creates a new instance for computing an offset curve for a geometry at a given distance.
   * with default quadrant segments ({@link BufferParameters#DEFAULT_QUADRANT_SEGMENTS})
   * and join style ({@link BufferParameters#JOIN_STYLE}).
   * 
   * @param geom the geometry to offset
   * @param distance the offset distance (positive for left, negative for right)
   * 
   * @see BufferParameters
   */
  public OffsetCurve(Geometry geom, double distance) {
    this(geom, distance, null);
  }
  
  /**
   * Creates a new instance for computing an offset curve for a geometry at a given distance.
   * setting the quadrant segments and join style and mitre limit 
   * via {@link BufferParameters}.
   * 
   * @param geom the geometry to offset
   * @param distance the offset distance (positive for left, negative for right)
   * @param bufParams the buffer parameters to use
   */
  public OffsetCurve(Geometry geom, double distance, BufferParameters bufParams) {
    this.inputGeom = geom;
    this.distance = distance;
    
    matchDistance = Math.abs(distance) / MATCH_DISTANCE_FACTOR;
    geomFactory = inputGeom.getFactory();
    
    //-- make new buffer params since the end cap style must be the default
    this.bufferParams = new BufferParameters();
    if (bufParams != null) {
      /**
       * Prevent using a very small QuadSegs value, to avoid 
       * offset curve artifacts near the end caps. 
       */
      int quadSegs = bufParams.getQuadrantSegments();
      if (quadSegs < MIN_QUADRANT_SEGMENTS) {
        quadSegs = MIN_QUADRANT_SEGMENTS;
      }
      bufferParams.setQuadrantSegments(quadSegs);
      bufferParams.setJoinStyle(bufParams.getJoinStyle());
      bufferParams.setMitreLimit(bufParams.getMitreLimit());
      bufferParams.setSimplifyFactor(bufParams.getSimplifyFactor());
    }
  }
  
  /**
   * Computes a single curve line for each input linear component,
   * by joining curve sections in order along the raw offset curve.
   * The default mode is to compute separate curve sections.
   * 
   * @param isJoined true if joined mode should be used.
   */
  public void setJoined(boolean isJoined) {
    this.isJoined = isJoined;
  }
  
  /**
   * Gets the computed offset curve lines.
   * 
   * @return the offset curve geometry
   */
  public Geometry getCurve() {
    return GeometryMapper.flatMap(inputGeom, 1, new GeometryMapper.MapOp() {
      
      @Override
      public Geometry map(Geometry geom) {
        if (geom instanceof Point) return null;
        if (geom instanceof Polygon ) {
          return computePolygonCurve((Polygon) geom, distance);
        } 
        return computeCurve((LineString) geom, distance);
      }
    });
  }
  
  private Geometry computePolygonCurve(Polygon poly, double distance) {
    Geometry buffer;
    if (bufferParams == null)
      buffer = BufferOp.bufferOp(poly, distance);
    else {
      buffer = BufferOp.bufferOp(poly, distance, bufferParams);
    }
    return toLineString(buffer.getBoundary());
  }
  
  /**
   * Force LinearRings to be LineStrings.
   * 
   * @param geom a linear geometry which may be a LinearRing
   * @return a geometry which if linear is a LineString or MultiLineString
   */
  private static Geometry toLineString(Geometry geom) {
    if (geom instanceof LinearRing) {
      LinearRing ring = (LinearRing) geom;
      return geom.getFactory().createLineString(ring.getCoordinateSequence());
    }
    return geom;
  }
  
  /**
   * Gets the raw offset curve for a line at a given distance.
   * The quadrant segments, join style and mitre limit can be specified
   * via {@link BufferParameters}.
   * <p>
   * The raw offset line may contain loops and other artifacts which are 
   * not present in the true offset curve.
   * 
   * @param line the line to offset
   * @param distance the offset distance (positive for left, negative for right)
   * @param bufParams the buffer parameters to use
   * @return the raw offset curve points
   */
  public static Coordinate[] rawOffset(LineString line, double distance, BufferParameters bufParams)
  {
    Coordinate[] pts = line.getCoordinates();
    Coordinate[] cleanPts = CoordinateArrays.removeRepeatedOrInvalidPoints(pts);
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        line.getFactory().getPrecisionModel(), bufParams
        );
    Coordinate[] rawPts = ocb.getOffsetCurve(cleanPts, distance);
    return rawPts;
  }
  
  /**
   * Gets the raw offset curve for a line at a given distance, 
   * with default buffer parameters.
   * 
   * @param line the line to offset
   * @param distance the offset distance (positive for left, negative for right)
   * @return the raw offset curve points
   */
  public static Coordinate[] rawOffset(LineString line, double distance)
  {
    return rawOffset(line, distance, new BufferParameters());
  }

  private Geometry computeCurve(LineString lineGeom, double distance) {
    //-- first handle simple cases
    //-- empty or single-point line
    if (lineGeom.getNumPoints() < 2 || lineGeom.getLength() == 0.0) {
      return geomFactory.createLineString();
    }
    //-- zero offset distance
    if (distance == 0) {
      return lineGeom.copy();
    }
    //-- two-point line
    if (lineGeom.getNumPoints() == 2) {
      return offsetSegment(lineGeom.getCoordinates(), distance);
    }

    List<OffsetCurveSection> sections = computeSections(lineGeom, distance);

    Geometry offsetCurve;
    if (isJoined) {
      offsetCurve = OffsetCurveSection.toLine(sections, geomFactory);
    }
    else {
      offsetCurve = OffsetCurveSection.toGeometry(sections, geomFactory);
    }
    return offsetCurve;
  }

  private List<OffsetCurveSection> computeSections(LineString lineGeom, double distance) {
    Coordinate[] rawCurve = rawOffset(lineGeom, distance, bufferParams);
    List<OffsetCurveSection> sections = new ArrayList<OffsetCurveSection>();
    if (rawCurve.length == 0) {
      return sections;
    }
    
    /**
     * Note: If the raw offset curve has no
     * narrow concave angles or self-intersections it could be returned as is.
     * However, this is likely to be a less frequent situation, 
     * and testing indicates little performance advantage,
     * so not doing this. 
     */
    
    Polygon bufferPoly = getBufferOriented(lineGeom, distance, bufferParams);
    
    //-- first extract offset curve sections from shell
    Coordinate[] shell = bufferPoly.getExteriorRing().getCoordinates();
    computeCurveSections(shell, rawCurve, sections);
    
    //-- extract offset curve sections from holes
    for (int i = 0; i < bufferPoly.getNumInteriorRing(); i++) {
      Coordinate[] hole = bufferPoly.getInteriorRingN(i).getCoordinates();
      computeCurveSections(hole, rawCurve, sections);      
    }
    return sections;
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
   * Used here to avoid issues with non-robust buffer results 
   * which have spurious extra polygons.
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
  
  private static final double NOT_IN_CURVE = -1;
  
  private void computeCurveSections(Coordinate[] bufferRingPts, 
      Coordinate[] rawCurve, List<OffsetCurveSection> sections) {
    double[] rawPosition = new double[bufferRingPts.length - 1];
    for (int i = 0; i < rawPosition.length; i++) {
      rawPosition[i] = NOT_IN_CURVE;
    }
    SegmentMCIndex bufferSegIndex = new SegmentMCIndex(bufferRingPts);
    int bufferFirstIndex = -1;
    double minRawPosition = -1;
    for (int i = 0; i < rawCurve.length - 1; i++) {
      int minBufferIndexForSeg = matchSegments(
                      rawCurve[i], rawCurve[i + 1], i, bufferSegIndex, bufferRingPts, rawPosition);
      if (minBufferIndexForSeg >= 0) {
        double pos = rawPosition[minBufferIndexForSeg];
        if (bufferFirstIndex < 0 || pos < minRawPosition) {
          minRawPosition = pos;
          bufferFirstIndex = minBufferIndexForSeg;
        }
       }
    }
    //-- no matching sections found in this buffer ring
    if (bufferFirstIndex < 0)
      return;
    extractSections(bufferRingPts, rawPosition, bufferFirstIndex, sections);
  }

  /**
   * Matches the segments in a buffer ring to the raw offset curve
   * to obtain their match positions (if any).
   * 
   * @param raw0 a raw curve segment start point
   * @param raw1 a raw curve segment end point
   * @param rawCurveIndex the index of the raw curve segment
   * @param bufferSegIndex the spatial index of the buffer ring segments
   * @param bufferPts the points of the buffer ring
   * @param rawCurvePos the raw curve positions of the buffer ring segments
   * @return the index of the minimum matched buffer segment
   */
  private int matchSegments(Coordinate raw0, Coordinate raw1, int rawCurveIndex,
      SegmentMCIndex bufferSegIndex, Coordinate[] bufferPts, 
      double[] rawCurvePos) {
    Envelope matchEnv = new Envelope(raw0, raw1);
    matchEnv.expandBy(matchDistance);
    MatchCurveSegmentAction matchAction = new MatchCurveSegmentAction(raw0, raw1, rawCurveIndex, matchDistance, bufferPts, rawCurvePos);
    bufferSegIndex.query(matchEnv, matchAction);
    return matchAction.getBufferMinIndex();
  }
  
  /**
   * An action to match a raw offset curve segment 
   * to segments in a buffer ring 
   * and record the matched segment locations(s) along the raw curve.
   * 
   * @author Martin Davis
   */
  private static class MatchCurveSegmentAction 
    extends MonotoneChainSelectAction
  {
    private Coordinate raw0;
    private Coordinate raw1;
    private double rawLen;
    private int rawCurveIndex;
    private Coordinate[] bufferRingPts;
    private double matchDistance;
    private double[] rawCurveLoc;
    private double minRawLocation = -1;
    private int bufferRingMinIndex = -1;
    
    public MatchCurveSegmentAction(Coordinate raw0, Coordinate raw1, 
        int rawCurveIndex,
        double matchDistance, Coordinate[] bufferRingPts, double[] rawCurveLoc) {
      this.raw0 = raw0;
      this.raw1 = raw1;
      rawLen = raw0.distance(raw1);
      this.rawCurveIndex = rawCurveIndex;
      this.bufferRingPts = bufferRingPts;
      this.matchDistance = matchDistance;
      this.rawCurveLoc = rawCurveLoc;
    }
    
    public int getBufferMinIndex() {
      return bufferRingMinIndex;
    }
    
    public void select(MonotoneChain mc, int segIndex)
    {
      /**
       * Generally buffer segments are no longer than raw curve segments, 
       * since the final buffer line likely has node points added.
       * So a buffer segment may match all or only a portion of a single raw segment.
       * There may be multiple buffer ring segs that match along the raw segment.
       * 
       * HOWEVER, in some cases the buffer construction may contain 
       * a matching buffer segment which is slightly longer than a raw curve segment.
       * Specifically, at the endpoint of a closed line with nearly parallel end segments
       * - the closing fillet line is very short so is heuristically removed in the buffer.
       * In this case, the buffer segment must still be matched.
       * This produces closed offset curves, which is technically
       * an anomaly, but only happens in rare cases.
       */
      double frac = segmentMatchFrac(bufferRingPts[segIndex], bufferRingPts[segIndex+1], 
          raw0, raw1, matchDistance);
      //-- no match
      if (frac < 0) return;
      
      //-- location is used to sort segments along raw curve
      double location = rawCurveIndex + frac;
      rawCurveLoc[segIndex] = location;
      //-- buffer seg index at lowest raw location is the curve start
      if (minRawLocation < 0 || location < minRawLocation) {
        minRawLocation = location;
        bufferRingMinIndex = segIndex;
      }    
    }
  
    private double segmentMatchFrac(Coordinate buf0, Coordinate buf1, 
        Coordinate raw0, Coordinate raw1, double matchDistance) {
      if (! isMatch(buf0, buf1, raw0, raw1, matchDistance))
      return -1;
      
      //-- matched - determine location as fraction along raw segment
      LineSegment seg = new LineSegment(raw0, raw1);
      return seg.segmentFraction(buf0);
  }
  
    private boolean isMatch(Coordinate buf0, Coordinate buf1, Coordinate raw0, Coordinate raw1, double matchDistance) {
      double bufSegLen = buf0.distance(buf1);
      if (rawLen <= bufSegLen) {
        if (matchDistance < Distance.pointToSegment(raw0, buf0, buf1))
          return false;
        if (matchDistance < Distance.pointToSegment(raw1, buf0, buf1))
          return false;
      }
      else {
        //TODO: only match longer buf segs at raw curve end segs?
        if (matchDistance < Distance.pointToSegment(buf0, raw0, raw1))
          return false;
        if (matchDistance < Distance.pointToSegment(buf1, raw0, raw1))
          return false;      
      }
      return true;
    }  
  }

  /**
   * This is only called when there is at least one ring segment matched
   * (so rawCurvePos has at least one entry != NOT_IN_CURVE).
   * The start index of the first section must be provided.
   * This is intended to be the section with lowest position
   * along the raw curve.
   * @param ringPts the points in a buffer ring
   * @param rawCurveLoc the position of buffer ring segments along the raw curve
   * @param startIndex the index of the start of a section
   * @param sections the list of extracted offset curve sections
   */
  private void extractSections(Coordinate[] ringPts, double[] rawCurveLoc, 
      int startIndex, List<OffsetCurveSection> sections) {
    int sectionStart = startIndex;
    int sectionCount = 0;
    int sectionEnd;
    do {
      sectionEnd = findSectionEnd(rawCurveLoc, sectionStart, startIndex);
      double location = rawCurveLoc[sectionStart];
      int lastIndex = prev(sectionEnd, rawCurveLoc.length);
      double lastLoc = rawCurveLoc[lastIndex];
      OffsetCurveSection section = OffsetCurveSection.create(ringPts, sectionStart, sectionEnd, location, lastLoc);
      sections.add(section);
      sectionStart = findSectionStart(rawCurveLoc, sectionEnd);
      
      //-- check for an abnormal state
      if (sectionCount++ > ringPts.length) {
        Assert.shouldNeverReachHere("Too many sections for ring - probable bug");
      }
    } while (sectionStart != startIndex && sectionEnd != startIndex);
  }
  
  private int findSectionStart(double[] loc, int end) {
    int start = end;
    do {
      int next = next(start, loc.length);
      //-- skip ahead if segment is not in raw curve
      if (loc[start] == NOT_IN_CURVE) {
        start = next;
        continue;
      }
      int prev = prev(start, loc.length);
      //-- if prev segment is not in raw curve then have found a start
      if (loc[prev] == NOT_IN_CURVE) {
        return start;
      }
      if (isJoined) {
        /**
         *  Start section at next gap in raw curve.
         *  Only needed for joined curve, since otherwise
         *  contiguous buffer segments can be in same curve section.
         */
        double locDelta = Math.abs(loc[start] - loc[prev]);
        if (locDelta > 1)
          return start;
        }
      start = next;
    } while (start != end);
    return start;
  }
  
  private int findSectionEnd(double[] loc, int start, int firstStartIndex) {
    // assert: pos[start] is IN CURVE
    int end = start;
    int next;
    do {
      next = next(end, loc.length);
      if (loc[next] == NOT_IN_CURVE)
        return next;
      if (isJoined) {
        /**
         *  End section at gap in raw curve.
         *  Only needed for joined curve, since otherwise
         *  contigous buffer segments can be in same section
         */
        double locDelta = Math.abs(loc[next] - loc[end]);
        if (locDelta > 1)
          return next;
      }
      end = next;
    } while (end != start && end != firstStartIndex);
    return end;
  }
  
  private static int next(int i, int size) {
    i += 1;
    return (i < size) ? i : 0; 
  }
  
  private static int prev(int i, int size) {
    i -= 1;
    return (i < 0) ? size - 1 : i; 
  }

}
