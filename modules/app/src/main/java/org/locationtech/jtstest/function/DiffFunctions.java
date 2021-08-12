/*
 * Copyright (c) 2019 Martin Davis, and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

public class DiffFunctions {
  
  public static GeometryCollection diffVerticesBoth(Geometry a, Geometry b) {
    MultiPoint diffAB = diffVertices(a, b);
    MultiPoint diffBA = diffVertices(b, a);
    
    return a.getFactory().createGeometryCollection(
          new Geometry[] { diffAB, diffBA });
  }
  
  /**
   * Diff the vertices in A against B to
   * find vertices in A which are not in B.
   * 
   * @param a a Geometry
   * @param b a Geometry
   * @return the vertices in A which are not in B
   */
  public static MultiPoint diffVertices(Geometry a, Geometry b) {
    
    Coordinate[] ptsB = b.getCoordinates();
    Set<Coordinate> pts = new HashSet<Coordinate>();
    for (int i = 0; i < ptsB.length; i++) {
      pts.add(ptsB[i]);
    }

    CoordinateList diffPts = new CoordinateList();
    Coordinate[] ptsA = a.getCoordinates();
    for (int j = 0; j < ptsA.length; j++) {
      Coordinate pa = ptsA[j];
      if (! pts.contains(pa)) {
        diffPts.add(pa);
      }
    }
    return a.getFactory().createMultiPointFromCoords(diffPts.toCoordinateArray());
  }
  
  public static GeometryCollection diffSegments(Geometry a, Geometry b) {
    List<LineSegment> segsA = extractSegmentsNorm(a);
    List<LineSegment> segsB = extractSegmentsNorm(b);
    
    MultiLineString diffAB = diffSegments( segsA, segsB, a.getFactory() );
     
    return diffAB;
  }

  public static GeometryCollection diffSegmentsBoth(Geometry a, Geometry b) {
    List<LineSegment> segsA = extractSegmentsNorm(a);
    List<LineSegment> segsB = extractSegmentsNorm(b);
    
    MultiLineString diffAB = diffSegments( segsA, segsB, a.getFactory() );
    MultiLineString diffBA = diffSegments( segsB, segsA, a.getFactory() );
    
    
    return a.getFactory().createGeometryCollection(
        new Geometry[] { diffAB, diffBA });
  }

  public static GeometryCollection duplicateSegments(Geometry a) {
    List<LineSegment> segsA = extractSegmentsNorm(a);    
    MultiLineString dupA = dupSegments( segsA, a.getFactory() );
    return dupA;
  }

  public static GeometryCollection singleSegments(Geometry a) {
    List<LineSegment> segsA = extractSegmentsNorm(a);    
    Map<LineSegment, Integer> segCounts = countSegments( segsA, a.getFactory() );
    List<LineSegment> singleSegs = new ArrayList<LineSegment>();
    for (LineSegment seg : segCounts.keySet()) {
      int count = segCounts.get(seg);
      if (count == 1) {
        singleSegs.add(seg);
      }
    }
    return toMultiLineString( singleSegs,  a.getFactory());
  }

  private static MultiLineString dupSegments(List<LineSegment> segs, GeometryFactory factory) {
    Set<LineSegment> segsAll = new HashSet<LineSegment>();
    List<LineSegment> segsDup = new ArrayList<LineSegment>();
    for (LineSegment seg : segs) {
      if (segsAll.contains(seg)) {
        segsDup.add(seg);
      }
      else {
        segsAll.add(seg);
      }
    }
    return toMultiLineString( segsDup,  factory);
  }

  private static Map<LineSegment, Integer> countSegments(List<LineSegment> segs, GeometryFactory factory) {
    Map<LineSegment, Integer> segsAll = new HashMap<LineSegment, Integer>();
    for (LineSegment seg : segs) {
      int count = 1;
      if (segsAll.containsKey(seg)) {
        count = 1 + segsAll.get(seg);
      }
      segsAll.put(seg, count);
    }
    return segsAll;
  }

  private static MultiLineString diffSegments(List<LineSegment> segsA, List<LineSegment> segsB, GeometryFactory factory) {
    
    Set<LineSegment> segs = new HashSet<LineSegment>();
    segs.addAll(segsB);

    List<LineSegment> segsDiffA = new ArrayList<LineSegment>();
    for (LineSegment seg : segsA) {
      if (! segs.contains(seg)) {
        segsDiffA.add(seg);
      }
    }
    return toMultiLineString( segsDiffA,  factory);
  }

  private static MultiLineString toMultiLineString(List<LineSegment> segs, GeometryFactory factory) {
    LineString[] lines = new LineString[ segs.size() ];
    int i = 0;
    for (LineSegment seg : segs) {
      lines[i++] = seg.toGeometry(factory);
    }
    return factory.createMultiLineString( lines );
  }

  private static List<LineSegment> extractSegmentsNorm(Geometry geom) {
    List<LineSegment> segs = new ArrayList<LineSegment>();
    List<LineString> lines = LinearComponentExtracter.getLines(geom);
    for (LineString line : lines ) {
      Coordinate[] pts = line.getCoordinates();
      for (int i = 0; i < pts.length - 1; i++) {
        LineSegment seg = new LineSegment(pts[i], pts[i + 1]);
        seg.normalize();
        segs.add(seg);
      }
    }
    return segs;
  }
  

  
}
;
