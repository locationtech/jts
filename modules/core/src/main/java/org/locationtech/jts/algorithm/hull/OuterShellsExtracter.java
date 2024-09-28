/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.hull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Extracts the rings of outer shells from a polygonal geometry.
 * Outer shells are the shells of polygon elements which
 * are not nested inside holes of other polygons.
 * 
 * @author mdavis
 *
 */
class OuterShellsExtracter {

  public static LinearRing[] extractShells(Geometry polygons) {
    OuterShellsExtracter extracter = new OuterShellsExtracter(polygons);
    return extracter.extractShells();
  }

  private Geometry polygons;

  public OuterShellsExtracter(Geometry polygons) {
    this.polygons = polygons;
  }

  private LinearRing[] extractShells() {
    LinearRing[] shells = extractShellRings(polygons);
    /**
     * sort shells in order of increasing envelope area
     * to ensure that shells are added before any of their inner shells
     */
    Arrays.sort(shells, new EnvelopeAreaComparator());
    List<LinearRing> outerShells = new ArrayList<LinearRing>();
    for (int i = shells.length - 1; i >= 0; i--) {
      LinearRing shell = shells[i];
      if (outerShells.size() == 0 
          || isOuter(shell, outerShells)) {
        outerShells.add(shell);
      }
    }
    return GeometryFactory.toLinearRingArray(outerShells);
  }
  
  private boolean isOuter(LinearRing shell, List<LinearRing> outerShells) {
    for (LinearRing outShell : outerShells) {
      if (covers(outShell, shell)) {
        return false;
      }
    }
    return true;
  }

  private boolean covers(LinearRing shellA, LinearRing shellB) {
    //-- if shellB envelope is not covered then shell is not covered
    if (! shellA.getEnvelopeInternal().covers(shellB.getEnvelopeInternal()))
      return false;
    //-- if a shellB point lies inside shellA, shell is covered (since shells do not overlap)
    if (isPointInRing(shellB, shellA))
      return true;
    return false;
  }

  private boolean isPointInRing(LinearRing shell, LinearRing shellRing) {
    //TODO: optimize this with cached index
    Coordinate pt = shell.getCoordinate();
    return PointLocation.isInRing(pt, shellRing.getCoordinates());
  }

  private static LinearRing[] extractShellRings(Geometry polygons) {
    LinearRing[] rings = new LinearRing[polygons.getNumGeometries()];
    for (int i = 0; i < polygons.getNumGeometries(); i++) {
      Polygon consPoly = (Polygon) polygons.getGeometryN(i);
      rings[i] = (LinearRing) consPoly.getExteriorRing().copy();
    }
    return rings;
  }
  
  private static class EnvelopeAreaComparator implements Comparator<Geometry> {

    @Override
    public int compare(Geometry o1, Geometry o2) {
      double a1 = o1.getEnvelopeInternal().getArea();
      double a2 = o2.getEnvelopeInternal().getArea();
      return Double.compare(a1, a2);
    }
    
  }
}
