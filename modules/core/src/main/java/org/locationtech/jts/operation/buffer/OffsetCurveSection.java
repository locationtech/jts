/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Models a section of a raw offset curve,
 * starting at a given location along the raw curve.
 * The location is a decimal number, with the integer part 
 * containing the segment index and the fractional part
 * giving the fractional distance along the segment.
 * The location of the last section segment 
 * is also kept, to allow optimizing joining sections together.
 * 
 * @author mdavis
 */
class OffsetCurveSection 
implements Comparable<OffsetCurveSection> {
  
  public static Geometry toGeometry(List<OffsetCurveSection> sections, GeometryFactory geomFactory) {
    if (sections.size() == 0)
      return geomFactory.createLineString();
    if (sections.size() == 1)
      return geomFactory.createLineString(sections.get(0).getCoordinates());
    
    //-- sort sections in order along the offset curve
    Collections.sort(sections);
    LineString[] lines = new LineString[sections.size()];
    
    for (int i = 0; i < sections.size(); i++) {
      lines[i] = geomFactory.createLineString(sections.get(i).getCoordinates());
    }
    return geomFactory.createMultiLineString(lines);
  }

  /**
   * Joins section coordinates into a LineString.
   * Join vertices which lie in the same raw curve segment
   * are removed, to simplify the result linework.
   * 
   * @param sections the sections to join
   * @param geomFactory the geometry factory to use
   * @return the simplified linestring for the joined sections
   */
  public static Geometry toLine(List<OffsetCurveSection> sections, GeometryFactory geomFactory) {
    if (sections.size() == 0)
      return geomFactory.createLineString();
    if (sections.size() == 1)
      return geomFactory.createLineString(sections.get(0).getCoordinates());
    
    //-- sort sections in order along the offset curve
    Collections.sort(sections);
    CoordinateList pts = new CoordinateList();
    
    boolean removeStartPt = false;
    for (int i = 0; i < sections.size(); i++) {
      OffsetCurveSection section = sections.get(i);
      
      boolean removeEndPt = false;
      if (i < sections.size() - 1) {
        double nextStartLoc = sections.get(i+1).location;
        removeEndPt = section.isEndInSameSegment(nextStartLoc);
      }
      Coordinate[] sectionPts = section.getCoordinates();
      for (int j = 0; j < sectionPts.length; j++) {
        if ((removeStartPt && j == 0) || (removeEndPt && j == sectionPts.length-1))
          continue;
        pts.add(sectionPts[j], false);        
      }
      removeStartPt = removeEndPt;
    }
    return geomFactory.createLineString(pts.toCoordinateArray());
  }

  public static OffsetCurveSection create(Coordinate[] srcPts, int start, int end, double loc, double locLast) {
    int len = end - start + 1;
    if (end <= start) 
      len = srcPts.length - start + end;
      
    Coordinate[] sectionPts = new Coordinate[len];
    for (int i = 0; i < len; i++) {
      int index = (start + i) % (srcPts.length - 1);
      sectionPts[i] = srcPts[index].copy();
    }
    return new OffsetCurveSection(sectionPts, loc, locLast);
  }
  
  private Coordinate[] sectionPts;
  private double location;
  private double locLast;

  OffsetCurveSection(Coordinate[] pts, double loc, double locLast) {
    this.sectionPts = pts;
    this.location = loc;
    this.locLast = locLast;
  }
  
  private Coordinate[] getCoordinates() {
    return sectionPts;
  }

  private boolean isEndInSameSegment(double nextLoc) {
    int segIndex = (int) locLast;
    int nextIndex = (int) nextLoc;
    return segIndex == nextIndex;
  }
  
  /**
   * Orders sections by their location along the raw offset curve.
   */
  @Override
  public int compareTo(OffsetCurveSection section) {
    return Double.compare(location, section.location);
  }

}
