package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

  public static GeometryCollection diffVerticeBoths(Geometry a, Geometry b) {
    MultiPoint diffAB = diffVertices(a, b);
    MultiPoint diffBA = diffVertices(b, a);
    
    return a.getFactory().createGeometryCollection(
          new Geometry[] { diffAB, diffBA });
  }
  
  private static MultiPoint diffVertices(Geometry a, Geometry b) {
    
    Coordinate[] ptsA = a.getCoordinates();
    Set<Coordinate> pts = new HashSet<Coordinate>();
    for (int i = 0; i < ptsA.length; i++) {
      pts.add(ptsA[i]);
    }

    CoordinateList diffPts = new CoordinateList();
    Coordinate[] ptsB = b.getCoordinates();
    for (int j = 0; j < ptsB.length; j++) {
      Coordinate p = ptsB[j];
      if (! pts.contains(p)) {
        diffPts.add(p);
      }
    }
    return a.getFactory().createMultiPointFromCoords(diffPts.toCoordinateArray());
  }
  
  public static GeometryCollection diffSegmentsBoth(Geometry a, Geometry b) {
    List<LineSegment> segsA = extractSegments(a);
    List<LineSegment> segsB = extractSegments(b);
    
    MultiLineString diffAB = diffSegments( segsA, segsB, a.getFactory() );
    MultiLineString diffBA = diffSegments( segsB, segsA, a.getFactory() );
    
    
    return a.getFactory().createGeometryCollection(
        new Geometry[] { diffAB, diffBA });
  }

  private static MultiLineString diffSegments(List<LineSegment> segsA, List<LineSegment> segsB, GeometryFactory factory) {
    
    Set<LineSegment> segs = new HashSet<LineSegment>();
    segs.addAll(segsA);

    List<LineSegment> segsDiff = new ArrayList<LineSegment>();
    for (LineSegment seg : segsB) {
      if (! segs.contains(seg)) {
        segsDiff.add(seg);
      }
    }
    LineString[] diffLines = toLineStrings( segsDiff,  factory);
    return factory.createMultiLineString( diffLines );
  }

  private static LineString[] toLineStrings(List<LineSegment> segs, GeometryFactory factory) {
    LineString[] lines = new LineString[ segs.size() ];
    int i = 0;
    for (LineSegment seg : segs) {
      lines[i++] = seg.toGeometry(factory);
    }
    return lines;
  }

  private static List<LineSegment> extractSegments(Geometry geom) {
    List<LineSegment> segs = new ArrayList<LineSegment>();
    List lines = LinearComponentExtracter.getLines(geom);
    for (Iterator lineIt = lines.iterator(); lineIt.hasNext(); ) {
      LineString line = (LineString) lineIt.next();
      
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