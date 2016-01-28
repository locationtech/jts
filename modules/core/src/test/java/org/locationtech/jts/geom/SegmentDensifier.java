/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;


/**
 * Densifies a LineString
 *
 * @version 1.7
 */
public class SegmentDensifier
{
  private LineString inputLine;
  private CoordinateList newCoords;

  public SegmentDensifier(LineString line) {
    this.inputLine = line;
  }

  public Geometry densify(double segLength)
  {
    newCoords = new CoordinateList();

    CoordinateSequence seq = inputLine.getCoordinateSequence();

    Coordinate p0 = new Coordinate();
    Coordinate p1 = new Coordinate();
    seq.getCoordinate(0, p0);
    newCoords.add(new Coordinate(p0));

    for (int i = 0; i < seq.size() - 1; i++) {
      seq.getCoordinate(i, p0);
      seq.getCoordinate(i + 1, p1);
      densify(p0, p1, segLength);
    }
    Coordinate[] newPts = newCoords.toCoordinateArray();
    return inputLine.getFactory().createLineString(newPts);
  }

  private void densify(Coordinate p0, Coordinate p1, double segLength)
  {
    double origLen = p1.distance(p0);
    int nPtsToAdd = (int) Math.floor(origLen / segLength);

    double delx = p1.x - p0.x;
    double dely = p1.y - p0.y;

    double segLenFrac = segLength / origLen;
    for (int i = 0; i <= nPtsToAdd; i++) {
      double addedPtFrac = i * segLenFrac;
      Coordinate pt = new Coordinate(p0.x + addedPtFrac * delx,
                                     p0.y + addedPtFrac * dely);
      newCoords.add(pt, false);
    }
    newCoords.add(new Coordinate(p1), false);
  }
}