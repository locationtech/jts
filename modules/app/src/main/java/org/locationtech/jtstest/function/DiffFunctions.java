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
    List<LineSegment> segsA = extractSegments(a);
    List<LineSegment> segsB = extractSegments(b);
    
    MultiLineString diffAB = diffSegments( segsA, segsB, a.getFactory() );
     
    return diffAB;
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
    segs.addAll(segsB);

    List<LineSegment> segsDiffA = new ArrayList<LineSegment>();
    for (LineSegment seg : segsA) {
      if (! segs.contains(seg)) {
        segsDiffA.add(seg);
      }
    }
    LineString[] diffLines = toLineStrings( segsDiffA,  factory);
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