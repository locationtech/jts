/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding.snap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;

/**
 * Nodes a set of segment strings
 * snapping vertices and intersection points together if
 * they lie within the given snap tolerance distance.
 * Vertices take priority over intersection points for snapping.
 * Input segment strings are generally only split at true node points
 * (i.e. the output segment strings are of maximal length in the output arrangement).
 * <p>
 * The snap tolerance should be chosen to be as small as possible
 * while still producing a correct result.
 * It probably only needs to be small enough to eliminate 
 * "nearly-coincident" segments, for which intersection points cannot be computed accurately.
 * This implies a factor of about 10e-12
 * smaller than the magnitude of the segment coordinates. 
 * <p>
 * With an appropriate snap tolerance this algorithm appears to be very robust.
 * So far no failure cases have been found, 
 * given a small enough snap tolerance.
 * <p>
 * The correctness of the output is not verified by this noder. 
 * If required this can be done by {@link org.locationtech.jts.noding.ValidatingNoder}.
 * 
 * @version 1.17
 */
public class SnappingNoder
    implements Noder
{
  private SnappingPointIndex snapIndex;
  private double snapTolerance;
  private List<NodedSegmentString> nodedResult;

  /**
   * Creates a snapping noder using the given snap distance tolerance.
   * 
   * @param snapTolerance points are snapped if within this distance
   */
  public SnappingNoder(double snapTolerance) {
    this.snapTolerance = snapTolerance;
    snapIndex = new SnappingPointIndex(snapTolerance);
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
   * @param inputSegStrings a Collection of SegmentStrings
   */
  public void computeNodes(Collection inputSegStrings)
  {
    List<NodedSegmentString> snappedSS = snapVertices(inputSegStrings);
    nodedResult = (List<NodedSegmentString>) snapIntersections(snappedSS);
  }

  private List<NodedSegmentString> snapVertices(Collection<SegmentString> segStrings) {
    List<NodedSegmentString> nodedStrings = new ArrayList<NodedSegmentString>();
    for (SegmentString ss : segStrings) {
      nodedStrings.add( snapVertices(ss) );
    }
    return nodedStrings;
  }

  private NodedSegmentString snapVertices(SegmentString ss) {
    Coordinate[] snapCoords = snap(ss.getCoordinates());
    return new NodedSegmentString(snapCoords, ss.getData());
  }
  
  private Coordinate[] snap(Coordinate[] coords) {
    CoordinateList snapCoords = new CoordinateList();
    for (int i = 0 ; i < coords.length; i++) {
      Coordinate pt = snapIndex.snap(coords[i]);
      snapCoords.add(pt, false);
    }
    return snapCoords.toCoordinateArray();
  }
  
  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinate}s.
   *
   * Also adds the intersection nodes to the segments.
   *
   * @return a list of Coordinates for the intersections
   */
  private Collection snapIntersections(List<NodedSegmentString> inputSS)
  {
    SnappingIntersectionAdder intAdder = new SnappingIntersectionAdder(snapTolerance, snapIndex);
    /**
     * Use an overlap tolerance to ensure all 
     * possible snapped intersections are found
     */
    MCIndexNoder noder = new MCIndexNoder( intAdder, 2 * snapTolerance );
    noder.computeNodes(inputSS);
    return noder.getNodedSubstrings();
  }

}
