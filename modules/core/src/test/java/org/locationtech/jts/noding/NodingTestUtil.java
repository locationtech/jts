/*
 * Copyright (c) 2020 Martin Davis.
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.util.LineStringExtracter;

public class NodingTestUtil {
  public static MultiLineString toLines(Collection<NodedSegmentString> nodedList, 
      GeometryFactory geomFact) {
    LineString[] lines = new LineString[ nodedList.size() ];
    int i = 0;
    for (NodedSegmentString nss : nodedList) {
      Coordinate[] pts = nss.getCoordinates();
      LineString line = geomFact.createLineString(pts);
      lines[i++] = line;
    }
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
    
    MultiLineString result = toLines(nodedList, geom1.getFactory());
    return result;
  }
}
