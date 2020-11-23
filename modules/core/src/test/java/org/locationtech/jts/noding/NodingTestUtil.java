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
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.util.LineStringExtracter;

public class NodingTestUtil {
  
  public static Geometry toLines(Collection<NodedSegmentString> nodedList, 
      GeometryFactory geomFact) {
    LineString[] lines = new LineString[ nodedList.size() ];
    int i = 0;
    for (NodedSegmentString nss : nodedList) {
      Coordinate[] pts = nss.getCoordinates();
      LineString line = geomFact.createLineString(pts);
      lines[i++] = line;
    }
    if (lines.length == 1) return lines[0];
    return geomFact.createMultiLineString(lines);
  }

  public static List<NodedSegmentString> toSegmentStrings(List<LineString> lines) {
    List<NodedSegmentString> nssList = new ArrayList<NodedSegmentString>();
    for (LineString line : lines) {
      NodedSegmentString nss = new NodedSegmentString(line.getCoordinates(), line);
      nssList.add(nss);
    }
    return nssList;
  }
  
  public static List<NodedSegmentString> getNodedSubstrings(NodedSegmentString nss) {
    List<NodedSegmentString> resultEdgelist = new ArrayList<NodedSegmentString>();
    nss.getNodeList().addSplitEdges(resultEdgelist);
    return resultEdgelist;
  }
  
  /**
   * Runs a noder on one or two sets of input geometries
   * and validates that the result is fully noded.
   * 
   * @param geom1 a geometry
   * @param geom2 a geometry, which may be null
   * @param noder the noder to use
   * @return the fully noded linework
   * 
   * @throws TopologyException
   */
  public static Geometry nodeValidated(Geometry geom1, Geometry geom2, Noder noder) {
    List<LineString> lines = LineStringExtracter.getLines(geom1);
    if (geom2 != null) {
      lines.addAll( LineStringExtracter.getLines(geom2) );
    }
    List ssList = toSegmentStrings(lines);
    
    Noder noderValid = new ValidatingNoder(noder);
    noderValid.computeNodes(ssList);
    Collection<NodedSegmentString> nodedList = noder.getNodedSubstrings();
    
    Geometry result = toLines(nodedList, geom1.getFactory());
    return result;
  }
  
  public NodedSegmentString createNSS(double... ords) {
    if (ords.length % 2 != 0) {
      throw new IllegalArgumentException("Must provide pairs of ordinates");
    }
    Coordinate[] pts = new Coordinate[ ords.length / 2 ];
    for (int i = 0; i <= ords.length; i += 2) {
      Coordinate p = new Coordinate(ords[i],ords[i+1]);
      pts[i / 2] = p;
    }
    NodedSegmentString nss = new NodedSegmentString(pts, null);
    return nss;
  }
}
