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

package org.locationtech.jtsexample.technique;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

/**
 * Shows a technique for identifying the location of self-intersections
 * in a non-simple LineString.
 *
 * @version 1.7
 */

public class LineStringSelfIntersections {

  public static void main(String[] args)
      throws Exception
  {
    WKTReader rdr = new WKTReader();

    LineString line1 = (LineString) (rdr.read("LINESTRING (0 0, 10 10, 20 20)"));
    showSelfIntersections(line1);
    LineString line2 = (LineString) (rdr.read("LINESTRING (0 40, 60 40, 60 0, 20 0, 20 60)"));
    showSelfIntersections(line2);

  }

  public static void showSelfIntersections(LineString line)
  {
    System.out.println("Line: " + line);
    System.out.println("Self Intersections: " + lineStringSelfIntersections(line));
  }

  public static Geometry lineStringSelfIntersections(LineString line)
  {
    Geometry lineEndPts = getEndPoints(line);
    Geometry nodedLine = line.union(lineEndPts);
    Geometry nodedEndPts = getEndPoints(nodedLine);
    Geometry selfIntersections = nodedEndPts.difference(lineEndPts);
    return selfIntersections;
  }

  public static Geometry getEndPoints(Geometry g)
  {
    List endPtList = new ArrayList();
    if (g instanceof LineString) {
      LineString line = (LineString) g;

      endPtList.add(line.getCoordinateN(0));
      endPtList.add(line.getCoordinateN(line.getNumPoints() - 1));
    }
    else if (g instanceof MultiLineString) {
      MultiLineString mls = (MultiLineString) g;
      for (int i = 0; i < mls.getNumGeometries(); i++) {
        LineString line = (LineString) mls.getGeometryN(i);
        endPtList.add(line.getCoordinateN(0));
        endPtList.add(line.getCoordinateN(line.getNumPoints() - 1));
      }
    }
    Coordinate[] endPts = CoordinateArrays.toCoordinateArray(endPtList);
    return (new GeometryFactory()).createMultiPoint(endPts);
  }


}