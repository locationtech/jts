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

package org.locationtech.jts.simplify;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.LineSegment;

/**
 * Simplifies a TaggedLineString, preserving topology
 * (in the sense that no new intersections are introduced).
 * Uses the recursive Douglas-Peucker algorithm.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class TaggedLineStringSimplifier
{
  private LineIntersector li = new RobustLineIntersector();
  private LineSegmentIndex inputIndex;
  private LineSegmentIndex outputIndex;
  private ComponentCrossChecker crossChecker;
  private TaggedLineString line;
  private Coordinate[] linePts;
  private double distanceTolerance = 0.0;

  public TaggedLineStringSimplifier(LineSegmentIndex inputIndex,
                                     LineSegmentIndex outputIndex, 
                                     ComponentCrossChecker crossChecker)
  {
    this.inputIndex = inputIndex;
    this.outputIndex = outputIndex;
    this.crossChecker = crossChecker;
  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified geometry will be within this
   * distance of the original geometry.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Simplifies the given {@link TaggedLineString}
   * using the distance tolerance specified.
   * 
   * @param line the linestring to simplify
   */
  void simplify(TaggedLineString line)
  {
    this.line = line;
    linePts = line.getParentCoordinates();
    simplifySection(0, linePts.length - 1, 0);
    
    if (! line.isPreserveEndpoint() && CoordinateArrays.isRing(linePts)) {
      simplifyRingEndpoint();
    }
  }

  private void simplifySection(int i, int j, int depth)
  {
    depth += 1;
    if ((i+1) == j) {
      LineSegment newSeg = line.getSegment(i);
      line.addToResult(newSeg);
      // leave this segment in the input index, for efficiency
      return;
    }

    boolean isValidToSimplify = true;

    /**
     * Following logic ensures that there is enough points in the output line.
     * If there is already more points than the minimum, there's nothing to check.
     * Otherwise, if in the worst case there wouldn't be enough points,
     * don't flatten this segment (which avoids the worst case scenario)
     */
    if (line.getResultSize() < line.getMinimumSize()) {
      int worstCaseSize = depth + 1;
      if (worstCaseSize < line.getMinimumSize())
        isValidToSimplify = false;
    }

    double[] distance = new double[1];
    int furthestPtIndex = findFurthestPoint(linePts, i, j, distance);
    
    // flattening must be less than distanceTolerance
    if (distance[0] > distanceTolerance) {
      isValidToSimplify = false;
    }
    
    if (isValidToSimplify) {
      // test if flattened section would cause intersection
      LineSegment candidateSeg = new LineSegment();
      candidateSeg.p0 = linePts[i];
      candidateSeg.p1 = linePts[j];
      isValidToSimplify = isTopologyValid(line, i, j, candidateSeg);
    }
    
    if (isValidToSimplify) {
      LineSegment newSeg = flatten(i, j);
      line.addToResult(newSeg);
      return;
    }
    simplifySection(i, furthestPtIndex, depth);
    simplifySection(furthestPtIndex, j, depth);
  }

  private void simplifyRingEndpoint()
  {
    if (line.getResultSize() > line.getMinimumSize()) {
      LineSegment firstSeg = line.getResultSegment(0);
      LineSegment lastSeg = line.getResultSegment(-1);

      LineSegment simpSeg = new LineSegment(lastSeg.p0, firstSeg.p1);
      //-- the excluded segments are the ones containing the endpoint
      Coordinate endPt = firstSeg.p0;
      if (simpSeg.distance(endPt) <= distanceTolerance
          && isTopologyValid(line, line.getSegments().length - 2, 1, simpSeg)) {
        line.removeRingEndpoint();
      }
    }
  }
  
  private int findFurthestPoint(Coordinate[] pts, int i, int j, double[] maxDistance)
  {
    LineSegment seg = new LineSegment();
    seg.p0 = pts[i];
    seg.p1 = pts[j];
    double maxDist = -1.0;
    int maxIndex = i;
    for (int k = i + 1; k < j; k++) {
      Coordinate midPt = pts[k];
      double distance = seg.distance(midPt);
      if (distance > maxDist) {
        maxDist = distance;
        maxIndex = k;
      }
    }
    maxDistance[0] = maxDist;
    return maxIndex;
  }

  /**
   * Flattens a section of the line between
   * indexes <code>start</code> and <code>end</code>,
   * replacing them with a line between the endpoints.
   * The input and output indexes are updated
   * to reflect this.
   * 
   * @param start the start index of the flattened section
   * @param end the end index of the flattened section
   * @return the new segment created
   */
  private LineSegment flatten(int start, int end)
  {
    // make a new segment for the simplified geometry
    Coordinate p0 = linePts[start];
    Coordinate p1 = linePts[end];
    LineSegment newSeg = new LineSegment(p0, p1);
    // update the indexes
    remove(line, start, end);
    outputIndex.add(newSeg);
    return newSeg;
  }

  /**
   * Tests if a flattening segment intersects a line 
   * (excluding a given section of segments).
   * The excluded section is being replaced by the flattening segment, 
   * so there is no need to test it 
   * (and it may well intersect the segment).
   * 
   * @param line
   * @param excludeStart
   * @param excludeEnd
   * @param candidateSeg
   * @return
   */
  private boolean isTopologyValid(TaggedLineString line,
                       int excludeStart, int excludeEnd,
                       LineSegment candidateSeg)
  {
    System.out.println("Flattening candidate: " + candidateSeg);
    if (hasOutputIntersection(candidateSeg)) return false;
    if (hasInputIntersection(line, excludeStart, excludeEnd, candidateSeg)) return false;
    if (crossChecker.isCross(line, excludeStart, excludeEnd, candidateSeg)) return false;
System.out.println("OK TO FLATTEN\n\n");
    return true;
  }

  private boolean hasOutputIntersection(LineSegment candidateSeg)
  {
    List querySegs = outputIndex.query(candidateSeg);
    for (Iterator i = querySegs.iterator(); i.hasNext(); ) {
      LineSegment querySeg = (LineSegment) i.next();
      if (hasInvalidIntersection(querySeg, candidateSeg)) {
          return true;
      }
    }
    return false;
  }

  private boolean hasInputIntersection(TaggedLineString line,
                        int excludeStart, int excludeEnd,
                       LineSegment candidateSeg)
  {
    List querySegs = inputIndex.query(candidateSeg);
    for (Iterator i = querySegs.iterator(); i.hasNext(); ) {
      TaggedLineSegment querySeg = (TaggedLineSegment) i.next();
      if (hasInvalidIntersection(querySeg, candidateSeg)) {
          /**
           * Ignore the intersection if the intersecting segment is part of the section being collapsed
           * to the candidate segment
           */
          if (isInLineSection(line, excludeStart, excludeEnd, querySeg))
            continue;
          return true;
      }
    }
    return false;
  }

  /**
   * Tests whether a segment is in a section of a TaggedLineString.
   * Sections may wrap around the endpoint of the line, 
   * to support ring endpoint simplification.
   * This is indicated by excludedStart > excludedEnd
   * 
   * @param line the TaggedLineString containing the section segments
   * @param excludeStart  the index of the first segment in the excluded section  
   * @param excludeEnd the index of the last segment in the excluded section
   * @param seg the segment to test
   * @return true if the test segment intersects some segment in the line not in the excluded section
   */
  private static boolean isInLineSection(
      TaggedLineString line,
      int excludeStart, int excludeEnd,
      TaggedLineSegment seg)
  {
    //-- test segment is not in this line
    if (seg.getParent() != line.getParent())
      return false;
    int segIndex = seg.getIndex();
    if (excludeStart <= excludeEnd) {
      //-- section is contiguous
      if (segIndex >= excludeStart && segIndex < excludeEnd)
        return true;
    }
    else {
      //-- section wraps around the end of a ring
      if (segIndex >= excludeStart || segIndex <= excludeEnd)
      return true;
    }
    return false;
  }

  private boolean hasInvalidIntersection(LineSegment seg0, LineSegment seg1)
  {
    //-- segments must not be equal
    if (seg0.equalsTopo(seg1))
      return true;
    li.computeIntersection(seg0.p0, seg0.p1, seg1.p0, seg1.p1);
    return li.isInteriorIntersection();
  }

  /**
   * Remove the segs in the section of the line
   * @param line
   * @param pts
   * @param sectionStartIndex
   * @param sectionEndIndex
   */
  private void remove(TaggedLineString line,
                       int start, int end)
  {
    for (int i = start; i < end; i++) {
      TaggedLineSegment seg = line.getSegment(i);
      inputIndex.remove(seg);
    }
  }
}
