/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Debug;

/**
 * Uses regular noding but with snapping vertices
 * to nearby segments.
 * 
 * EXPERIMENTAL
 * 
 * @version 1.7
 */
public class SnappingNoder
    implements Noder
{
  private double snapTolerance;
  private List<NodedSegmentString> nodedResult;

  public SnappingNoder(double snapTolerance) {
    this.snapTolerance = snapTolerance;
  }

  /**
	 * @return a Collection of NodedSegmentStrings representing the substrings
	 * 
	 */
  public Collection getNodedSubstrings()
  {
    return nodedResult;
  }

  /**
   * @param inputSegmentStrings a Collection of NodedSegmentStrings
   */
  public void computeNodes(Collection inputSegmentStrings)
  {
    List<NodedSegmentString> inputSS = createNodedStrings(inputSegmentStrings);
    /**
     * Determine hot pixels for intersections and vertices.
     * This is done BEFORE the input lines are rounded,
     * to avoid distorting the line arrangement 
     * (rounding can cause vertices to move across edges).
     */
    nodedResult = (List<NodedSegmentString>) computeIntersections(inputSS);

    // testing purposes only - remove in final version
    //checkCorrectness(inputSegmentStrings);
    //if (Debug.isDebugging()) dumpNodedLines(inputSegmentStrings);
    //if (Debug.isDebugging()) dumpNodedLines(snappedResult);
  }

  private static List<NodedSegmentString> createNodedStrings(Collection<SegmentString> segStrings) {
    List<NodedSegmentString> nodedStrings = new ArrayList<NodedSegmentString>();
    for (SegmentString ss : segStrings) {
      nodedStrings.add( new NodedSegmentString(ss) );
    }
    return nodedStrings;
  }
  private void dumpNodedLines(Collection<NodedSegmentString> segStrings) {
    for (NodedSegmentString nss : segStrings) {
      Debug.println( WKTWriter.toLineString(nss.getNodeList().getSplitCoordinates()));
    }
  }

  private void checkValidNoding(Collection inputSegmentStrings)
  {
    Collection resultSegStrings = NodedSegmentString.getNodedSubstrings(inputSegmentStrings);
    NodingValidator nv = new NodingValidator(resultSegStrings);
    try {
      nv.checkValid();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinate}s.
   *
   * Also adds the intersection nodes to the segments.
   *
   * @return a list of Coordinates for the intersections
   */
  private Collection computeIntersections(List<NodedSegmentString> inputSS)
  {
    SnappingIntersectionAdder intAdder = new SnappingIntersectionAdder(snapTolerance);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(intAdder);
    noder.computeNodes(inputSS);
    return noder.getNodedSubstrings();
  }

}
