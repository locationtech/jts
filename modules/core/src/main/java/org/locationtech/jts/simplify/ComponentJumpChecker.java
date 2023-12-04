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

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

/**
 * Checks if simplifying (flattening) line sections or segments 
 * would cause them to "jump" over other components in the geometry.
 * 
 * @author mdavis
 *
 */
class ComponentJumpChecker {
  
  //TODO: use a spatial index?
  private Collection<TaggedLineString> components;

  public ComponentJumpChecker(Collection<TaggedLineString> taggedLines) {
    components = taggedLines;
  }

  /**
   * Checks if a line section jumps a component if flattened.
   * 
   * Assumes start <= end.
   * 
   * @param line the line containing the section being flattened
   * @param start start index of the section
   * @param end end index of the section
   * @param seg the flattening segment
   * @return true if the flattened section jumps a component
   */
  public boolean hasJump(TaggedLineString line, int start, int end, LineSegment seg) {
    Envelope sectionEnv = computeEnvelope(line, start, end);
    for (TaggedLineString comp : components) {
      //-- don't test component against itself
      if (comp == line)
        continue;
      
      Coordinate compPt = comp.getComponentPoint();
      if (sectionEnv.intersects(compPt)) {
        if (hasJumpAtComponent(compPt, line, start, end, seg)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if two consecutive segments jumps a component if flattened.
   * The segments are assumed to be consecutive.
   * (so the seg1.p1 = seg2.p0).
   * The flattening segment must be the segment between seg1.p0 and seg2.p1.
   * 
   * @param line the line containing the section being flattened
   * @param seg1 the first replaced segment
   * @param seg2 the next replaced segment
   * @param seg the flattening segment
   * @return true if the flattened segment jumps a component
   */
  public boolean hasJump(TaggedLineString line, LineSegment seg1, LineSegment seg2, LineSegment seg) {
    Envelope sectionEnv = computeEnvelope(seg1, seg2);
    for (TaggedLineString comp : components) {
      //-- don't test component against itself
      if (comp == line)
        continue;
      
      Coordinate compPt = comp.getComponentPoint();
      if (sectionEnv.intersects(compPt)) {
        if (hasJumpAtComponent(compPt, seg1, seg2, seg)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean hasJumpAtComponent(Coordinate compPt, TaggedLineString line, int start, int end, LineSegment seg) {
    int sectionCount = crossingCount(compPt, line, start, end);
    int segCount = crossingCount(compPt, seg);
    boolean hasJump = sectionCount % 2 != segCount % 2;
    return hasJump;
  }
  
  private static boolean hasJumpAtComponent(Coordinate compPt, LineSegment seg1, LineSegment seg2, LineSegment seg) {
    int sectionCount = crossingCount(compPt, seg1, seg2);
    int segCount = crossingCount(compPt, seg);
    boolean hasJump = sectionCount % 2 != segCount % 2;
    return hasJump;
  }

  private static int crossingCount(Coordinate compPt, LineSegment seg) {
    RayCrossingCounter rcc = new RayCrossingCounter(compPt);
    rcc.countSegment(seg.p0,  seg.p1);
    return rcc.getCount();
  }  
  
  private static int crossingCount(Coordinate compPt, LineSegment seg1, LineSegment seg2) {
    RayCrossingCounter rcc = new RayCrossingCounter(compPt);
    rcc.countSegment(seg1.p0,  seg1.p1);
    rcc.countSegment(seg2.p0,  seg2.p1);
    return rcc.getCount();
  }

  private static int crossingCount(Coordinate compPt, TaggedLineString line, int start, int end) {
    RayCrossingCounter rcc = new RayCrossingCounter(compPt);
    for (int i = start; i < end; i++) {
      rcc.countSegment(line.getCoordinate(i), line.getCoordinate(i + 1));
    }
    return rcc.getCount();
  }

  private static Envelope computeEnvelope(LineSegment seg1, LineSegment seg2) {
    Envelope env = new Envelope();
    env.expandToInclude(seg1.p0);
    env.expandToInclude(seg1.p1);
    env.expandToInclude(seg2.p0);
    env.expandToInclude(seg2.p1);
    return env;
  }  
  
  private static Envelope computeEnvelope(TaggedLineString line, int start, int end) {
    Envelope env = new Envelope();
    for (int i = start; i <= end; i++) {
      env.expandToInclude(line.getCoordinate(i)); 
    }
    return env;
  }
}
