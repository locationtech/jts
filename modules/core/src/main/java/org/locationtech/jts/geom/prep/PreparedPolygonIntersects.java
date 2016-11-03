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
package org.locationtech.jts.geom.prep;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.SegmentStringUtil;


/**
 * Computes the <tt>intersects</tt> spatial relationship predicate for
 * {@link PreparedPolygon}s relative to all other {@link Geometry} classes. Uses
 * short-circuit tests and indexing to improve performance.
 * 
 * @author Martin Davis
 * 
 */
class PreparedPolygonIntersects extends PreparedPolygonPredicate {
  /**
   * Computes the intersects predicate between a {@link PreparedPolygon} and a
   * {@link Geometry}.
   * 
   * @param prep
   *          the prepared polygon
   * @param geom
   *          a test geometry
   * @return true if the polygon intersects the geometry
   */
  public static boolean intersects(PreparedPolygon prep, Geometry geom) {
    PreparedPolygonIntersects polyInt = new PreparedPolygonIntersects(prep);
    return polyInt.intersects(geom);
  }

  /**
   * Creates an instance of this operation.
   * 
   * @param prepPoly
   *          the PreparedPolygon to evaluate
   */
  public PreparedPolygonIntersects(PreparedPolygon prepPoly) {
    super(prepPoly);
  }

  /**
   * Tests whether this PreparedPolygon intersects a given geometry.
   * 
   * @param geom
   *          the test geometry
   * @return true if the test geometry intersects
   */
  public boolean intersects(Geometry geom) {
    /**
     * Do point-in-poly tests first, since they are cheaper and may result in a
     * quick positive result.
     * 
     * If a point of any test components lie in target, result is true
     */
    boolean isInPrepGeomArea = isAnyTestComponentInTarget(geom);
    if (isInPrepGeomArea)
      return true;
    /**
     * If input contains only points, then at
     * this point it is known that none of them are contained in the target
     */
    if (geom.getDimension() == 0)
      return false;
    /**
     * If any segments intersect, result is true
     */
    List lineSegStr = SegmentStringUtil.extractSegmentStrings(geom);
    // only request intersection finder if there are segments 
    // (i.e. NOT for point inputs)
    if (lineSegStr.size() > 0) {
      boolean segsIntersect = prepPoly.getIntersectionFinder().intersects(
          lineSegStr);
      if (segsIntersect)
        return true;
    }

    /**
     * If the test has dimension = 2 as well, it is necessary to test for proper
     * inclusion of the target. Since no segments intersect, it is sufficient to
     * test representative points.
     */
    if (geom.getDimension() == 2) {
      // TODO: generalize this to handle GeometryCollections
      boolean isPrepGeomInArea = isAnyTargetComponentInAreaTest(geom,
          prepPoly.getRepresentativePoints());
      if (isPrepGeomInArea)
        return true;
    }

    return false;
  }

}
