/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;

/**
 * Finds gaps in a polygonal coverage which are narrower than a given width.
 * <p>
 * The coverage should be valid according to {@link CoverageValidator}.
 * If this is not the case, some gaps may not be reported, or the invocation may fail.
 * This does not find gaps which fully separate two adjacent polygons.
 * 
 * 
 * @author mdavis
 *
 */
public class CoverageGapFinder {
  
  /**
   * Finds gaps in a polygonal coverage.
   * Returns lines indicating the boundary of the gaps.
   * 
   * @param coverage a set of polygons forming a polygonal coverage
   * @param gapWidth the maximum width of gap to detect
   * @return a geometry indicating the position of gaps, or null if the coverage was empty
   */
  public static Geometry findGaps(Geometry[] coverage, double gapWidth) {
    CoverageGapFinder finder = new CoverageGapFinder(coverage);
    return finder.findGaps(gapWidth);
  }
  
  private Geometry[] coverage;

  /**
   * Creates a new coverage gap finder.
   * 
   * @param coverage a set of polygons forming a polygonal coverage
   */
  public CoverageGapFinder(Geometry[] coverage) {
    this.coverage = coverage;
  }
  
  /**
   * Finds gaps in the coverage.
   * Returns lines indicating the boundary of the gaps.
   * 
   * @param gapWidth the maximum width of gap to detect
   * @return a geometry indicating the position of gaps, or null if the coverage was empty
   */
  public Geometry findGaps(double gapWidth) {
    Geometry union = CoverageUnion.union(coverage);
    List<Polygon> polygons = PolygonExtracter.getPolygons(union);
    
    List<LineString> gapLines = new ArrayList<LineString>();
    for (Polygon poly : polygons) {
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = poly.getInteriorRingN(i);
        if (isGap(hole, gapWidth)) {
          gapLines.add(copyLine(hole));
        }
      }
    }
    return union.getFactory().buildGeometry(gapLines);
  }

  private LineString copyLine(LinearRing hole) {
    Coordinate[] pts = hole.getCoordinates();
    return hole.getFactory().createLineString(pts);
  }

  private boolean isGap(LinearRing hole, double gapWidth) {
    Geometry holePoly = hole.getFactory().createPolygon(hole);
    double tolerance = gapWidth / 100;
    //TODO: improve MIC class to allow short-circuiting when radius is larger than a value
    LineString line = MaximumInscribedCircle.getRadiusLine(holePoly, tolerance);
    double width = line.getLength() * 2;
    return width <= gapWidth;
  }

}
