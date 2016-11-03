
/*
 * Copyright (c) 2016 Vivid Solutions.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.InteriorIntersectionFinderAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.NodingValidator;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.SinglePassNoder;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s.
 * Implements the Snap Rounding technique described in 
 * the papers by Hobby, Guibas &amp; Marimont, and Goodrich et al.
 * Snap Rounding assumes that all vertices lie on a uniform grid;
 * hence the precision model of the input must be fixed precision,
 * and all the input vertices must be rounded to that precision.
 * <p>
 * This implementation uses simple iteration over the line segments.
 * This is not the most efficient approach for large sets of segments.
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
  private Collection nodedSegStrings;

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
    return  NodedSegmentString.getNodedSubstrings(nodedSegStrings);
  }

  /**
   * @param inputSegmentStrings a Collection of NodedSegmentStrings
   */
  public void computeNodes(Collection inputSegmentStrings)
  {
    this.nodedSegStrings = inputSegmentStrings;
    snapRound(inputSegmentStrings, li);

    // testing purposes only - remove in final version
    //checkCorrectness(inputSegmentStrings);
  }

  private void checkCorrectness(Collection inputSegmentStrings)
  {
    Collection resultSegStrings = NodedSegmentString.getNodedSubstrings(inputSegmentStrings);
    NodingValidator nv = new NodingValidator(resultSegStrings);
    try {
      nv.checkValid();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private void snapRound(Collection segStrings, LineIntersector li)
  {
    List intersections = findInteriorIntersections(segStrings, li);
    computeSnaps(segStrings, intersections);
    computeVertexSnaps(segStrings);
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinate}s.
   *
   * Does NOT node the segStrings.
   *
   * @return a list of Coordinates for the intersections
   */
  private List findInteriorIntersections(Collection segStrings, LineIntersector li)
  {
    InteriorIntersectionFinderAdder intFinderAdder = new InteriorIntersectionFinderAdder(li);
    SinglePassNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(intFinderAdder);
    noder.computeNodes(segStrings);
    return intFinderAdder.getInteriorIntersections();
  }


  /**
   * Computes nodes introduced as a result of snapping segments to snap points (hot pixels)
   * @param li
   */
  private void computeSnaps(Collection segStrings, Collection snapPts)
  {
    for (Iterator i0 = segStrings.iterator(); i0.hasNext(); ) {
      NodedSegmentString ss = (NodedSegmentString) i0.next();
      computeSnaps(ss, snapPts);
    }
  }

  private void computeSnaps(NodedSegmentString ss, Collection snapPts)
  {
    for (Iterator it = snapPts.iterator(); it.hasNext(); ) {
      Coordinate snapPt = (Coordinate) it.next();
      HotPixel hotPixel = new HotPixel(snapPt, scaleFactor, li);
      for (int i = 0; i < ss.size() - 1; i++) {
      	hotPixel.addSnappedNode(ss, i);
      }
    }
  }

  /**
   * Computes nodes introduced as a result of
   * snapping segments to vertices of other segments
   *
   * @param edges the list of segment strings to snap together
   */
  public void computeVertexSnaps(Collection edges)
  {
    for (Iterator i0 = edges.iterator(); i0.hasNext(); ) {
      NodedSegmentString edge0 = (NodedSegmentString) i0.next();
      for (Iterator i1 = edges.iterator(); i1.hasNext(); ) {
        NodedSegmentString edge1 = (NodedSegmentString) i1.next();
        computeVertexSnaps(edge0, edge1);
      }
    }
  }

  /**
   * Performs a brute-force comparison of every segment in each {@link SegmentString}.
   * This has n^2 performance.
   */
  private void computeVertexSnaps(NodedSegmentString e0, NodedSegmentString e1)
  {
    Coordinate[] pts0 = e0.getCoordinates();
    Coordinate[] pts1 = e1.getCoordinates();
    for (int i0 = 0; i0 < pts0.length - 1; i0++) {
      HotPixel hotPixel = new HotPixel(pts0[i0], scaleFactor, li);
      for (int i1 = 0; i1 < pts1.length - 1; i1++) {
        // don't snap a vertex to itself
        if (e0 == e1) {
          if (i0 == i1) continue;
        }
        //System.out.println("trying " + pts0[i0] + " against " + pts1[i1] + pts1[i1 + 1]);
        boolean isNodeAdded = hotPixel.addSnappedNode(e1, i1);
        // if a node is created for a vertex, that vertex must be noded too
        if (isNodeAdded) {
          e0.addIntersection(pts0[i0], i0);
        }
      }
    }
  }

}
