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
package org.locationtech.jtslab;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;

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
