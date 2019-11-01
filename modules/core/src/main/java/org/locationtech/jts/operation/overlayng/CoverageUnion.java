/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.noding.Noder;

/**
 * Unions a valid, fully-noded homogeneous coverage of polygons or lines
 * in an efficient way.   
 * <p>
 * A valid coverage is determined by the following criteria:
 * <ul>
 * <li><b>Homogeneous</b> - all elements of the collection must have the same dimension
 * <li><b>Fully noded</b> - Linestrings within the collection must not cross
 * (in either a proper intersection or at interior segment string endpoints)
 * <li><b>No overlaps</b> - No two elements in the collection can overlap
 * </ul>
 * Currently no explicit checking is done to determine
 * whether the input coverage is valid.
 * If the input is not a valid coverage 
 * then an error <i>may</i> thrown, if this situation can be detected.
 * Otherwise the output may be invalid.
 * <p>
 * Unioning a valid coverage implies that no new vertices are created,
 * so a precision model does not need to be supplied,
 * and the precision of the output vertices is not changed.
 * 
 * @author Martin Davis
 * 
 * @see SegmentExtractingNoder
 *
 */
public class CoverageUnion 
{

  /**
   * Unions a valid polygonal or lineal coverage.
   * 
   * @param coverage a coverage of polygons or lines
   * @return the union of the coverage
   */
  public static Geometry union(Geometry coverage) {
    Noder noder = new SegmentExtractingNoder();
    Point emptyPoint = coverage.getFactory().createPoint();
    return OverlayNG.overlay(coverage, emptyPoint, UNION, null, noder );
  }

}
