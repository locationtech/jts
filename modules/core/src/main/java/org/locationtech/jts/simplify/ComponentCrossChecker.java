/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.simplify;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.algorithm.RayCrossingCounter;

public class ComponentCrossChecker {
  
  private List<Coordinate> compPts = new ArrayList<Coordinate>();

  public ComponentCrossChecker(Collection<TaggedLineString> taggedLines) {
    init(taggedLines);
  }

  private void init(Collection<TaggedLineString> taggedLines) {
    for (TaggedLineString line : taggedLines) {
      Coordinate pt = line.getParentCoordinates()[1];
      compPts.add(pt);
    }
  }

  public boolean isCross(TaggedLineString line, int start, int end, LineSegment seg) {
    Envelope sectionEnv = computeEnvelope(line, start, end);
    for (Coordinate compPt : compPts) {
      
      //TODO: don't test component against itself
      
      if (sectionEnv.intersects(compPt)) {
        if (isCross(compPt, line, start, end, seg)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isCross(Coordinate compPt, TaggedLineString line, int start, int end, LineSegment seg) {
    int sectionCount = crossingCount(compPt, line, start, end);
    int segCount = crossingCount(compPt, seg);
    boolean hasCrossing = sectionCount % 2 != segCount % 2;
    
    System.out.println(compPt);
    System.out.println("section count = " + sectionCount + " - seg count = " + segCount + "\n");
    return hasCrossing;
  }

  private int crossingCount(Coordinate compPt, LineSegment seg) {
    RayCrossingCounter rcc = new RayCrossingCounter(compPt);
    rcc.countSegment(seg.p0,  seg.p1);
    return rcc.getCount();
  }

  private int crossingCount(Coordinate compPt, TaggedLineString line, int start, int end) {
    RayCrossingCounter rcc = new RayCrossingCounter(compPt);
    int i = start;
    rcc.countSegment(line.getCoordinate(i), line.getCoordinate(i + 1));
    do {
      // increment segment index, with wrap-around
      i = nextSegmentIndex(line, i);
      rcc.countSegment(line.getCoordinate(i), line.getCoordinate(i + 1));
    } while (i != end - 1);
    return rcc.getCount();
  }

  private static int nextSegmentIndex(TaggedLineString line, int i) {
    return (i >= line.size() - 2) ? 0 : i + 1;
  }

  private static Envelope computeEnvelope(TaggedLineString line, int start, int end) {
    Envelope env = new Envelope();
    int i = start;
    env.expandToInclude(line.getCoordinate(i));
    do {
      i = nextIndex(line, i);
      env.expandToInclude(line.getCoordinate(i));
      // increment vertex index, with wrap-around
    } while (i != end);
    return env;
  }

  private static int nextIndex(TaggedLineString line, int i) {
    return (i >= line.size() - 1) ? 0 : i + 1;
  }
}
