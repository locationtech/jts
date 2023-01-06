/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.triangulate.polygon;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Adds node vertices to the rings of a polygon 
 * where holes touch the shell or each other.
 * The structure of the polygon is preserved.
 * <p>
 * This does not fix invalid polygon topology
 * (such as self-touching or crossing rings). 
 * Invalid input remains invalid after noding,
 * and does not trigger an error.
 */
class PolygonNoder {

  private boolean[] isHoleTouching;
  private List<NodedSegmentString> nodedRings;

  public PolygonNoder(Coordinate[] shellRing, Coordinate[][] holeRings) {
    nodedRings = createNodedSegmentStrings(shellRing, holeRings);
    isHoleTouching = new boolean[holeRings.length];
  }

  public void node() {
    SegmentIntersector nodeAdder = new NodeAdder(isHoleTouching);
    MCIndexNoder noder = new MCIndexNoder(nodeAdder);
    noder.computeNodes(nodedRings);
  }

  public boolean isShellNoded() {
    return nodedRings.get(0).hasNodes();
  }
  
  public boolean isHoleNoded(int i) {
    return nodedRings.get(i + 1).hasNodes();
  }
  
  public Coordinate[] getNodedShell() {
    return nodedRings.get(0).getNodedCoordinates();
  }
  
  public Coordinate[] getNodedHole(int i) {
    return nodedRings.get(i + 1).getNodedCoordinates();
  }
  
  public boolean[] getHolesTouching() {
    return isHoleTouching;
  }
  
  public static List<NodedSegmentString> createNodedSegmentStrings(Coordinate[] shellRing, Coordinate[][] holeRings)
  {
    List<NodedSegmentString> segStr = new ArrayList<NodedSegmentString>();
    segStr.add(createNodedSegString(shellRing, -1));
    for (int i = 0; i < holeRings.length; i++) {
      segStr.add(createNodedSegString(holeRings[i], i));
    }
    return segStr;
  }
  
  private static NodedSegmentString createNodedSegString(Coordinate[] ringPts, int i) {
    return new NodedSegmentString(ringPts, i);
  }
  
  /**
   * A {@link SegmentIntersector} that added node vertices
   * to {@link NodedSegmentStrings} where a segment touches another
   * segment in its interior.
   * 
   * @author mdavis
   *
   */
  private static class NodeAdder implements SegmentIntersector {

    private LineIntersector li = new RobustLineIntersector();
    private boolean[] isHoleTouching;

    public NodeAdder(boolean[] isHoleTouching) {
      this.isHoleTouching = isHoleTouching;
    }

    @Override
    public void processIntersections(SegmentString ss0, int segIndex0, SegmentString ss1, int segIndex1) {
      //-- input is assumed valid, so rings do not self-intersect
      if (ss0 == ss1)
        return;
      
      Coordinate p00 = ss0.getCoordinate(segIndex0);
      Coordinate p01 = ss0.getCoordinate(segIndex0 + 1);
      Coordinate p10 = ss1.getCoordinate(segIndex1);
      Coordinate p11 = ss1.getCoordinate(segIndex1 + 1);
      
      li.computeIntersection(p00, p01, p10, p11);
      /**
       * There should never be 2 intersection points, since
       * that would imply collinear segments, and an invalid polygon
       */
      if (li.getIntersectionNum() == 1) {
        addTouch(ss0);
        addTouch(ss1);
        Coordinate intPt = li.getIntersection(0);
        if (li.isInteriorIntersection(0)) {
          ((NodedSegmentString) ss0).addIntersectionNode(intPt, segIndex0);          
        }
        else if (li.isInteriorIntersection(1)) {
          ((NodedSegmentString) ss1).addIntersectionNode(intPt, segIndex1);          
        }
      }     
    }
    
    private void addTouch(SegmentString ss) {
      int holeIndex = (int) ss.getData();
      if (holeIndex >= 0) {
        isHoleTouching[holeIndex] = true;
      }
    }

    @Override
    public boolean isDone() {
      return false;
    }
  }
}
