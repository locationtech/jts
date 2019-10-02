
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
package org.locationtech.jts.noding.snapround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.NodingValidator;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.util.Debug;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s.
 * <p>
 * Implements the Snap Rounding technique described in 
 * the papers by Hobby, Guibas &amp; Marimont, and Goodrich et al.
 * Snap Rounding enforces that all output vertices lie on a uniform grid,
 * which is determined by the provided {@link PrecisionModel}.
 * Input vertices do not have to be rounded to this grid; 
 * this will be done during the snap-rounding process.
 * <p>
 * This implementation uses simple iteration over the line segments.
 * This is not an efficient approach for large sets of segments.
 * <p>
 * This implementation appears to be fully robust using an integer precision model.
 * It will function with non-integer precision models, but the
 * results are not 100% guaranteed to be correctly noded.
 *
 * @version 1.7
 */
public class SimpleSnapRounder
    implements Noder
{
  private final PrecisionModel pm;
  private LineIntersector li;
  private final double scaleFactor;
  private Map<Coordinate, HotPixel> hotPixelMap = new HashMap<Coordinate, HotPixel>();
  private List<HotPixel> hotPixels;
  
  private List<NodedSegmentString> snappedResult;

  public SimpleSnapRounder(PrecisionModel pm) {
    this.pm = pm;
    li = new RobustLineIntersector();
    li.setPrecisionModel(pm);
    scaleFactor = pm.getScale();
  }

  /**
	 * @return a Collection of NodedSegmentStrings representing the substrings
	 * 
	 */
  public Collection getNodedSubstrings()
  {
    return NodedSegmentString.getNodedSubstrings(snappedResult);
  }

  /**
   * @param inputSegmentStrings a Collection of NodedSegmentStrings
   */
  public void computeNodes(Collection inputSegmentStrings)
  {
    /**
     * Determine intersections at full precision.  
     * Rounding happens during Hot Pixel creation.
     */
    snappedResult = snapRound(inputSegmentStrings);

    // testing purposes only - remove in final version
    //checkCorrectness(inputSegmentStrings);
    //if (Debug.isDebugging()) dumpNodedLines(inputSegmentStrings);
    //if (Debug.isDebugging()) dumpNodedLines(snappedResult);
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
  private List<NodedSegmentString> snapRound(Collection<SegmentString> segStrings)
  {
    List<NodedSegmentString> inputSS = createNodedStrings(segStrings);
    /**
     * Determine hot pixels for intersections and vertices.
     * This is done BEFORE the input lines are rounded,
     * to avoid distorting the line arrangement 
     * (rounding can cause vertices to move across edges).
     */
    List<Coordinate> intersections = findInteriorIntersections(inputSS);
    addHotPixels(intersections);
    addVertexPixels(segStrings);
    hotPixels = new ArrayList<HotPixel>(hotPixelMap.values());

    List<NodedSegmentString> snapped = computeSnaps(inputSS);
    return snapped;
  }

  private static List<NodedSegmentString> createNodedStrings(Collection<SegmentString> segStrings) {
    List<NodedSegmentString> nodedStrings = new ArrayList<NodedSegmentString>();
    for (SegmentString ss : segStrings) {
      nodedStrings.add( new NodedSegmentString(ss) );
    }
    return nodedStrings;
  }

  private void addVertexPixels(Collection<SegmentString> segStrings) {
    for (SegmentString nss : segStrings) {
      Coordinate[] pts = nss.getCoordinates();
      addHotPixels(pts);
    }
  }

  private void addHotPixels(Coordinate[] pts) {
    for (Coordinate pt : pts) {
      createHotPixel( round(pt) );
    }
  }
  
  private void addHotPixels(List<Coordinate> pts) {
    for (Coordinate pt : pts) {
      createHotPixel( round(pt) );
    }
  }

  private Coordinate round(Coordinate pt) {
    Coordinate p2 = pt.copy();
    pm.makePrecise(p2);
    return p2;
  }

  /**
   * Gets a list of the rounded coordinates.
   * Duplicate (collapsed) coordinates are removed.
   * 
   * @param pts the coordinates to round
   * @return array of rounded coordinates
   */
  private Coordinate[] round(Coordinate[] pts) {
    CoordinateList roundPts = new CoordinateList();
    
    for (int i = 0; i < pts.length; i++ ) {
      roundPts.add( round( pts[i] ), false);
    }
    return roundPts.toCoordinateArray();
  }
  
  private HotPixel createHotPixel(Coordinate p) {
    HotPixel hp = hotPixelMap.get(p);
    if (hp != null) return hp;
    hp = new HotPixel(p, scaleFactor, li);
    hotPixelMap.put(p,  hp);
    return hp;
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinate}s.
   *
   * Also adds the intersection nodes to the segments.
   *
   * @return a list of Coordinates for the intersections
   */
  private List<Coordinate> findInteriorIntersections(List<NodedSegmentString> inputSS)
  {
    SnapIntersectionAdder intAdder = new SnapIntersectionAdder(pm);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(intAdder);
    noder.computeNodes(inputSS);
    return intAdder.getIntersections();
  }

  /**
   * Computes nodes introduced as a result of snapping segments to snap points (hot pixels)
   * @param li
   * @return 
   */
  private List<NodedSegmentString> computeSnaps(Collection<NodedSegmentString> segStrings)
  {
    List<NodedSegmentString> snapped = new ArrayList<NodedSegmentString>();
    for (NodedSegmentString ss : segStrings ) {
      NodedSegmentString snappedSS = computeSnaps(ss);
      if (snappedSS != null)
        snapped.add(snappedSS);
    }
    return snapped;
  }

  private NodedSegmentString computeSnaps(NodedSegmentString ss)
  {
    //Coordinate[] pts = ss.getCoordinates();
    /**
     * Get edge coordinates, including added intersection nodes.
     * The coordinates are now rounded to the grid,
     * in preparation for snapping to the Hot Pixels
     */
    Coordinate[] pts = ss.getNodedCoordinates();
    Coordinate[] ptsRound = round(pts);
    
    // if complete collapse this edge can be eliminated
    if (ptsRound.length <= 1) 
      return null;
    
    // Create new nodedSS to allow adding any hot pixel nodes
    NodedSegmentString snapSS = new NodedSegmentString(ptsRound, ss.getData());
    
    int snapSSindex = 0;
    for (int i = 0; i < pts.length - 1; i++ ) {
      Coordinate currSnap = snapSS.getCoordinate(snapSSindex);

      /**
       * If the segment has collapsed completely, skip it
       */
      Coordinate p1 = pts[i+1];
      Coordinate p1Round = round(p1);
      if (p1Round.equals2D(currSnap))
        continue;
      
      Coordinate p0 = pts[i];
      
      /**
       * Add any Hot Pixel intersections with *original* segment to rounded segment.
       * (It is important to check original segment because rounding can
       * move it enough to intersect other hot pixels not intersecting original segment)
       */
      snapSegment( p0, p1, snapSS, snapSSindex);      
      snapSSindex++;
    }
    return snapSS;
  }


  /**
   * This is where all the work of snapping to hot pixels gets done
   * (in a very inefficient brute-force way).
   * 
   * @param coordinate
   * @param coordinate2
   * @param snapPts
   */
  private void snapSegment(Coordinate p0, Coordinate p1, NodedSegmentString ss, int segIndex) {
    for (HotPixel hp : hotPixels) {
      if (hp.intersects(p0, p1)) {
        ss.addIntersection( hp.getCoordinate(), segIndex );
      }
    }
  }

}
