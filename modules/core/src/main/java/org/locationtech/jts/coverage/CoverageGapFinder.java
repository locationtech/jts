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
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;

/**
 * Finds gaps in a polygonal coverage.
 * Gaps are holes in the coverage which are narrower than a given width.
 * <p>
 * The coverage should be valid according to {@link CoverageValidator}.
 * If this is not the case, some gaps may not be reported, or the invocation may fail.
 * <p>
 * This is a more accurate way of identifying gaps 
 * than using {@link CoverageValidator#setGapWidth(double)}.
 * Gaps which separate the coverage into two disjoint regions are not detected.
 * Gores are not identified as gaps.
 * 
 * @author mdavis
 *
 */
public class CoverageGapFinder {
  
  /**
   * Finds gaps in a polygonal coverage.
   * Returns lines indicating the locations of the gaps.
   * 
   * @param coverage a set of polygons forming a polygonal coverage
   * @param gapWidth the maximum width of gap to detect
   * @return a MultiPolygon indicating the locations of gaps (empty if no gaps were found), or null if the coverage was empty
   */
  public static Geometry findGaps(Geometry[] coverage, double gapWidth) {
    CoverageGapFinder finder = new CoverageGapFinder(coverage);
    return finder.findGaps(gapWidth);
  }
  
  private Geometry[] coverage;

  /**
   * Creates a new polygonal coverage gap finder.
   * 
   * @param coverage a set of polygons forming a polygonal coverage
   */
  public CoverageGapFinder(Geometry[] coverage) {
    this.coverage = coverage;
  }
  
  /**
   * Finds gaps in the coverage.
   * Returns lines indicating the locations of the gaps.
   * 
   * @param gapWidth the maximum width of gap to detect
   * @return a geometry indicating the locations of gaps (which is empty if no gaps were found), or null if the coverage was empty
   */
  public Geometry findGaps(double gapWidth) {
    Geometry union = CoverageUnion.union(coverage);
    List<Polygon> polygons = PolygonExtracter.getPolygons(union);
    
    List<Polygon> gapLines = new ArrayList<Polygon>();
    for (Polygon poly : polygons) {
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = poly.getInteriorRingN(i);
        if (isGap(hole, gapWidth)) {
          gapLines.add(toPolygon(hole));
        }
      }
    }
    return union.getFactory().buildGeometry(gapLines);
  }

  private Polygon toPolygon(LinearRing hole) {
    Coordinate[] pts = CoordinateArrays.copyDeep(hole.getCoordinates());
    return hole.getFactory().createPolygon(pts);
  }

  private boolean isGap(LinearRing hole, double maxGapWidth) {
    Geometry holePoly = hole.getFactory().createPolygon(hole);
    //-- guard against bad input
    if (maxGapWidth <= 0.0)
      return false;
    
    return MaximumInscribedCircle.isRadiusWithin(holePoly, 0.5 * maxGapWidth);
  }

}
