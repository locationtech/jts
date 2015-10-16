package com.vividsolutions.jtslab;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

public class SnapRoundOverlayFunctions {

  
  public static Geometry intersection(Geometry geomA, Geometry geomB, double scaleFactor) {
    Geometry[] geom = snapClean(geomA, geomB, scaleFactor);
    return geom[0].intersection(geom[1]);
  }

  public static Geometry difference(Geometry geomA, Geometry geomB, double scaleFactor) {
    Geometry[] geom = snapClean(geomA, geomB, scaleFactor);
    return geom[0].difference(geom[1]);
  }

  public static Geometry symDifference(Geometry geomA, Geometry geomB, double scaleFactor) {
    Geometry[] geom = snapClean(geomA, geomB, scaleFactor);
    return geom[0].symDifference(geom[1]);
  }

  public static Geometry union(Geometry geomA, Geometry geomB, double scaleFactor) {
    Geometry[] geom = snapClean(geomA, geomB, scaleFactor);
    return geom[0].union(geom[1]);
  }

  private static Geometry[] snapClean(
      Geometry geomA, Geometry geomB, 
      double scaleFactor) {
    Geometry snapped = SnapRoundFunctions.snapRound(geomA, geomB, scaleFactor);
    // TODO: don't need to clean once GeometrySnapRounder ensures all components are valid
    Geometry aSnap = clean(snapped.getGeometryN(0));
    Geometry bSnap = clean(snapped.getGeometryN(1));
    return new Geometry[] { aSnap, bSnap };
  }

  private static Geometry clean(Geometry geom) {
    // TODO: only buffer if it is a polygonal geometry
    if (! (geom instanceof Polygonal) ) return geom;
    return geom.buffer(0);
  }
  

}
