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
package org.locationtech.jts.noding.snapround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdNodeVisitor;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s,
 * in a performant way.
 * <p>
 * Implements the Snap Rounding technique described in 
 * the papers by Hobby, Guibas &amp; Marimont, and Goodrich et al.
 * Snap Rounding enforces that all output vertices lie on a uniform grid,
 * which is determined by the provided {@link PrecisionModel}.
 * Input vertices do not have to be rounded to the grid; 
 * this is done during the snap-rounding process.
 * In fact, rounding cannot be done a priori,
 * since rounding vertices alone can distort the rounded topology
 * of the arrangement (by moving segments away from hot pixels
 * that would otherwise intersect them, or by moving vertices
 * across segments).
 * 
 * @version 1.7
 */
public class SnapRoundingNoder
    implements Noder
{
  private final PrecisionModel pm;
  private HotPixelIndex pixelIndex;
  
  private List<NodedSegmentString> snappedResult;

  public SnapRoundingNoder(PrecisionModel pm) {
    this.pm = pm;
    pixelIndex = new HotPixelIndex(pm);
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

  /*
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
  */
  
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
    pixelIndex.add(intersections);
    addVertexPixels(segStrings);

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
      pixelIndex.add(pts);
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
    SnapRoundingIntersectionAdder intAdder = new SnapRoundingIntersectionAdder(pm);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(intAdder);
    noder.computeNodes(inputSS);
    return intAdder.getIntersections();
  }

  /**
   * Computes new segment strings which are rounded and contain
   * any intersections added as a result of snapping segments to snap points (hot pixels).
   * 
   * @param segStrings segments to snap
   * @return the snapped segment strings
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

  /**
   * Add snapped vertices to a segemnt string.
   * If the segment string collapses completely due to rounding,
   * null is returned.
   * 
   * @param ss the segment string to snap
   * @return the snapped segment string, or null if it collapses completely
   */
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
   * Snaps a segment in a segmentString to HotPixels that it intersects.
   * 
   * @param p0 the segment start coordinate
   * @param p1 the segment end coordinate
   * @param ss the segment string to add intersections to
   * @param segIndex the index of the segment
   */
  private void snapSegment(Coordinate p0, Coordinate p1, NodedSegmentString ss, int segIndex) {
    pixelIndex.query(p0, p1, new KdNodeVisitor() {

      @Override
      public void visit(KdNode node) {
        // TODO Auto-generated method stub
        HotPixel hp = (HotPixel) node.getData();
        if (hp.intersects(p0, p1)) {
          //System.out.println("Added intersection: " + hp.getCoordinate());
          ss.addIntersection( hp.getCoordinate(), segIndex );
        }

      }
      
    });

  }

}
