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
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.geom.Coordinate;


/**
 * Finds non-noded intersections in a set of {@link SegmentString}s,
 * if any exist.
 * <p>
 * Non-noded intersections include:
 * <ul>
 * <li><b>Interior intersections</b> which lie in the interior of a segment
 * (with another segment interior or with a vertex or endpoint)
 * <li><b>Vertex intersections</b> which occur at vertices in the interior of {@link SegmentString}s
 * (with a segment string endpoint or with another interior vertex)
 * </ul>
 * The finder can be limited to finding only interior intersections 
 * by setting {@link #setInteriorIntersectionsOnly(boolean)}.
 * <p>
 * By default only the first intersection is found, 
 * but all can be found by setting {@link #setFindAllIntersections(boolean)}
 * 
 * @version 1.7
 */
public class NodingIntersectionFinder
    implements SegmentIntersector
{
	/**
	 * Creates a finder which tests if there is at least one intersection.
	 * Uses short-circuiting for efficient performance.
	 * The intersection found is recorded.
	 * 
	 * @param li a line intersector
	 * @return a finder which tests if there is at least one intersection.
	 */
	public static NodingIntersectionFinder createAnyIntersectionFinder(LineIntersector li)
	{
		return new NodingIntersectionFinder(li);
	}
	
	/**
	 * Creates a finder which finds all intersections.
	 * The intersections are recorded for later inspection.
	 * 
	 * @param li a line intersector
	 * @return a finder which finds all intersections.
	 */
  public static NodingIntersectionFinder createAllIntersectionsFinder(LineIntersector li)
  {
    NodingIntersectionFinder finder = new NodingIntersectionFinder(li);
    finder.setFindAllIntersections(true);
    return finder;
  }
  
  /**
   * Creates a finder which finds all interior intersections.
   * The intersections are recorded for later inspection.
   * 
   * @param li a line intersector
   * @return a finder which finds all interior intersections.
   */
  public static NodingIntersectionFinder createInteriorIntersectionsFinder(LineIntersector li)
  {
    NodingIntersectionFinder finder = new NodingIntersectionFinder(li);
    finder.setFindAllIntersections(true);
    finder.setInteriorIntersectionsOnly(true);
    return finder;
  }
  
  /**
   * Creates an finder which counts all intersections.
   * The intersections are note recorded to reduce memory usage.
   * 
   * @param li a line intersector
   * @return a finder which counts all intersections.
   */
  public static NodingIntersectionFinder createIntersectionCounter(LineIntersector li)
  {
    NodingIntersectionFinder finder = new NodingIntersectionFinder(li);
    finder.setFindAllIntersections(true);
    finder.setKeepIntersections(false);
    return finder;
  }
  
  /**
   * Creates an finder which counts all interior intersections.
   * The intersections are note recorded to reduce memory usage.
   * 
   * @param li a line intersector
   * @return a finder which counts all interior intersections.
   */
  public static NodingIntersectionFinder createInteriorIntersectionCounter(LineIntersector li)
  {
    NodingIntersectionFinder finder = new NodingIntersectionFinder(li);
    finder.setInteriorIntersectionsOnly(true);
    finder.setFindAllIntersections(true);
    finder.setKeepIntersections(false);
    return finder;
  }
  
  private boolean findAllIntersections = false;
  private boolean isCheckEndSegmentsOnly = false;
  private boolean keepIntersections = true;
  private boolean isInteriorIntersectionsOnly = false;
  
  private LineIntersector li;
  private Coordinate interiorIntersection = null;
  private Coordinate[] intSegments = null;
  private List intersections = new ArrayList();
  private int intersectionCount = 0;

  /**
   * Creates an intersection finder which finds an intersection
   * if one exists
   *
   * @param li the LineIntersector to use
   */
  public NodingIntersectionFinder(LineIntersector li)
  {
    this.li = li;
    interiorIntersection = null;
  }

  /**
   * Sets whether all intersections should be computed.
   * When this is <code>false</code> (the default value)
   * the value of {@link #isDone()} is <code>true</code> after the first intersection is found.
   * <p>
   * Default is <code>false</code>.
   * 
   * @param findAllIntersections whether all intersections should be computed
   */
  public void setFindAllIntersections(boolean findAllIntersections)
  {
    this.findAllIntersections = findAllIntersections;
  }
  
  /**
   * Sets whether only interior (proper) intersections will be found.
   * @param isInteriorIntersectionsOnly whether to find only interior intersections
   */
  public void setInteriorIntersectionsOnly(boolean isInteriorIntersectionsOnly)
  {
    this.isInteriorIntersectionsOnly  = isInteriorIntersectionsOnly;
  }
  
  /**
   * Sets whether only end segments should be tested for intersection.
   * This is a performance optimization that may be used if
   * the segments have been previously noded by an appropriate algorithm.
   * It may be known that any potential noding failures will occur only in
   * end segments.
   * 
   * @param isCheckEndSegmentsOnly whether to test only end segments
   */
  public void setCheckEndSegmentsOnly(boolean isCheckEndSegmentsOnly)
  {
    this.isCheckEndSegmentsOnly = isCheckEndSegmentsOnly;
  }
  
  /**
   * Sets whether intersection points are recorded.
   * If the only need is to count intersection points, this can be set to <code>false</code>.
   * <p>
   * Default is <code>true</code>.
   * 
   * @param keepIntersections indicates whether intersections should be recorded
   */
  public void setKeepIntersections(boolean keepIntersections)
  {
    this.keepIntersections = keepIntersections;
  }
  
  /**
   * Gets the intersections found.
   * 
   * @return a List of {@link Coordinate}
   */
  public List getIntersections()
  {
    return intersections;
  }
  
  /**
   * Gets the count of intersections found.
   * 
   * @return the intersection count
   */
  public int count()
  {
    return intersectionCount;
  }
  
  /**
   * Tests whether an intersection was found.
   * 
   * @return true if an intersection was found
   */
  public boolean hasIntersection() 
  { 
  	return interiorIntersection != null; 
  }
  
  /**
   * Gets the computed location of the intersection.
   * Due to round-off, the location may not be exact.
   * 
   * @return the coordinate for the intersection location
   */
  public Coordinate getIntersection()  
  {    
  	return interiorIntersection;  
  }

  /**
   * Gets the endpoints of the intersecting segments.
   * 
   * @return an array of the segment endpoints (p00, p01, p10, p11)
   */
  public Coordinate[] getIntersectionSegments()
  {
  	return intSegments;
  }
  
  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  public void processIntersections(
      SegmentString e0,  int segIndex0,
      SegmentString e1,  int segIndex1
      )
  {
  	// short-circuit if intersection already found
  	if (! findAllIntersections && hasIntersection())
  		return;
  	
    // don't bother intersecting a segment with itself
  	boolean isSameSegString = e0 == e1;
  	boolean isSameSegment = isSameSegString && segIndex0 == segIndex1;
    if (isSameSegment) return;

    /**
     * If enabled, only test end segments (on either segString).
     * 
     */
    if (isCheckEndSegmentsOnly) {
    	boolean isEndSegPresent = isEndSegment(e0, segIndex0) || isEndSegment(e1, segIndex1);
    	if (! isEndSegPresent)
    		return;
    }
    
    Coordinate p00 = e0.getCoordinate(segIndex0);
    Coordinate p01 = e0.getCoordinate(segIndex0 + 1);
    Coordinate p10 = e1.getCoordinate(segIndex1);
    Coordinate p11 = e1.getCoordinate(segIndex1 + 1);
    boolean isEnd00 = segIndex0 == 0;
    boolean isEnd01 = segIndex0 + 2 == e0.size();
    boolean isEnd10 = segIndex1 == 0;
    boolean isEnd11 = segIndex1 + 2 == e1.size();
    
    li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    /**
     * Check for an intersection in the interior of a segment
     */
    boolean isInteriorInt = li.hasIntersection() && li.isInteriorIntersection();
    /**
     * Check for an intersection between two vertices which are not both endpoints.
     */
    boolean isInteriorVertexInt = false;
    if (! isInteriorIntersectionsOnly) {
      boolean isAdjacentSegment = isSameSegString && Math.abs(segIndex1 - segIndex0) <= 1;
      isInteriorVertexInt = (! isAdjacentSegment) && isInteriorVertexIntersection(p00, p01, p10, p11,
          isEnd00, isEnd01, isEnd10, isEnd11);
    }
    
    if (isInteriorInt || isInteriorVertexInt) {
      // found an intersection!
    	intSegments = new Coordinate[4];
    	intSegments[0] = p00;
    	intSegments[1] = p01;
    	intSegments[2] = p10;
    	intSegments[3] = p11;
    	
    	//TODO: record endpoint intersection(s)
    	interiorIntersection = li.getIntersection(0);
    	if (keepIntersections) intersections.add(interiorIntersection);
    	intersectionCount++;
    }
  }
  
  /**
   * Tests if an intersection occurs between a segmentString interior vertex and another vertex.
   * Note that intersections between two endpoint vertices are valid noding, 
   * and are not flagged.
   * 
   * @param p00 a segment vertex
   * @param p01 a segment vertex
   * @param p10 a segment vertex
   * @param p11 a segment vertex
   * @param isEnd00 true if vertex is a segmentString endpoint
   * @param isEnd01 true if vertex is a segmentString endpoint
   * @param isEnd10 true if vertex is a segmentString endpoint
   * @param isEnd11 true if vertex is a segmentString endpoint
   * @return true if an intersection is found
   */
  private static boolean isInteriorVertexIntersection(
      Coordinate p00, Coordinate p01, 
      Coordinate p10, Coordinate p11,
      boolean isEnd00, boolean isEnd01,
      boolean isEnd10, boolean isEnd11) {
    if (isInteriorVertexIntersection(p00, p10, isEnd00, isEnd10)) return true;
    if (isInteriorVertexIntersection(p00, p11, isEnd00, isEnd11)) return true;
    if (isInteriorVertexIntersection(p01, p10, isEnd01, isEnd10)) return true;
    if (isInteriorVertexIntersection(p01, p11, isEnd01, isEnd11)) return true;
    return false;
  }
  
  /**
   * Tests if two vertices with at least one in a segmentString interior
   * are equal.
   * 
   * @param p0 a segment vertex
   * @param p1 a segment vertex
   * @param isEnd0 true if vertex is a segmentString endpoint
   * @param isEnd1 true if vertex is a segmentString endpoint
   * @return true if an intersection is found
   */
  private static boolean isInteriorVertexIntersection(
      Coordinate p0, Coordinate p1,
      boolean isEnd0, boolean isEnd1) {
    
    // Intersections between endpoints are valid nodes, so not reported
    if (isEnd0 && isEnd1) return false;
    
    if (p0.equals2D(p1)) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether a segment in a {@link SegmentString} is an end segment.
   * (either the first or last).
   * 
   * @param segStr a segment string
   * @param index the index of a segment in the segment string
   * @return true if the segment is an end segment
   */
  private static boolean isEndSegment(SegmentString segStr, int index)
  {
  	if (index == 0) return true;
  	if (index >= segStr.size() - 2) return true;
  	return false;
  }
  
  /**
   * 
   */
  public boolean isDone()
  { 
  	if (findAllIntersections) return false;
  	return interiorIntersection != null;
  }

}
