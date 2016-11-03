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

package org.locationtech.jts.geom;

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