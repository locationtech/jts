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

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.WKTWriter;


/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Indexing is used to improve performance.
 * By default validation stops after a single 
 * non-noded intersection is detected. 
 * Alternatively, it can be requested to detect all intersections
 * by using {@link #setFindAllIntersections(boolean)}.
 * <p>
 * The validator does not check for topology collapse situations
 * (e.g. where two segment strings are fully co-incident).
 * <p> 
 * The validator checks for the following situations which indicated incorrect noding:
 * <ul>
 * <li>Proper intersections between segments (i.e. the intersection is interior to both segments)
 * <li>Intersections at an interior vertex (i.e. with an endpoint or another interior vertex)
 * </ul>
 * <p>
 * The client may either test the {@link #isValid()} condition, 
 * or request that a suitable {@link TopologyException} be thrown.
 *
 * @version 1.7
 * 
 * @see NodingIntersectionFinder
 */
public class FastNodingValidator 
{
  /**
   * Gets a list of all intersections found.
   * Intersections are represented as {@link Coordinate}s.
   * List is empty if none were found.
   * 
   * @param segStrings a collection of SegmentStrings
   * @return a list of Coordinate
   */
  public static List computeIntersections(Collection segStrings)
  {
    FastNodingValidator nv = new FastNodingValidator(segStrings);
    nv.setFindAllIntersections(true);
    nv.isValid();
    return nv.getIntersections();
  }
  
  private LineIntersector li = new RobustLineIntersector();

  private Collection segStrings;
  private boolean findAllIntersections = false;
  private NodingIntersectionFinder segInt = null;
  private boolean isValid = true;
  
  /**
   * Creates a new noding validator for a given set of linework.
   * 
   * @param segStrings a collection of {@link SegmentString}s
   */
  public FastNodingValidator(Collection segStrings)
  {
    this.segStrings = segStrings;
  }

  public void setFindAllIntersections(boolean findAllIntersections)
  {
    this.findAllIntersections = findAllIntersections;
  }
  
  /**
   * Gets a list of all intersections found.
   * Intersections are represented as {@link Coordinate}s.
   * List is empty if none were found.
   * 
   * @return a list of Coordinate
   */
  public List getIntersections()
  {
    return segInt.getIntersections();
  }

  /**
   * Checks for an intersection and 
   * reports if one is found.
   * 
   * @return true if the arrangement contains an interior intersection
   */
  public boolean isValid()
  {
  	execute();
  	return isValid;
  }
  
  /**
   * Returns an error message indicating the segments containing
   * the intersection.
   * 
   * @return an error message documenting the intersection location
   */
  public String getErrorMessage()
  {
  	if (isValid) return "no intersections found";
  	
		Coordinate[] intSegs = segInt.getIntersectionSegments();
    return "found non-noded intersection between "
        + WKTWriter.toLineString(intSegs[0], intSegs[1])
        + " and "
        + WKTWriter.toLineString(intSegs[2], intSegs[3]);
  }
  
  /**
   * Checks for an intersection and throws
   * a TopologyException if one is found.
   *
   * @throws TopologyException if an intersection is found
   */
  public void checkValid()
  {
  	execute();
  	if (! isValid)
  		throw new TopologyException(getErrorMessage(), segInt.getIntersection());
  }

  private void execute()
  {
  	if (segInt != null) 
  		return;
    checkInteriorIntersections();
  }

  private void checkInteriorIntersections()
  {
  	/**
  	 * MD - It may even be reliable to simply check whether 
  	 * end segments (of SegmentStrings) have an interior intersection,
  	 * since noding should have split any true interior intersections already.
  	 */
  	isValid = true;
  	segInt = new NodingIntersectionFinder(li);
    segInt.setFindAllIntersections(findAllIntersections);
  	MCIndexNoder noder = new MCIndexNoder();
  	noder.setSegmentIntersector(segInt);
  	noder.computeNodes(segStrings);
  	if (segInt.hasIntersection()) {
  		isValid = false;
  		return;
  	}
  }
  
}
