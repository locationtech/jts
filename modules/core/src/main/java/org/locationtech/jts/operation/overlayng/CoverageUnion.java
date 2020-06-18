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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentExtractingNoder;

/**
 * Unions a valid coverage of polygons or lines
 * in a robust, efficient way.   
 * <p>
 * A valid coverage is determined by the following conditions:
 * <ul>
 * <li><b>Homogeneous</b> - all elements of the collection must have the same dimension.
 * <li><b>Fully noded</b> - Line segments within the collection 
 * must either be identical or intersect only at endpoints.
 * <li><b>Non-overlapping</b> - (Polygonal coverage only) No two polygons
 * may overlap. Equivalently, polygons must be interior-disjoint.
 * </ul>
 * <p>
 * Currently no checking is done to determine whether the input is a valid coverage.
 * This is because coverage validation involves segment intersection detection,
 * which is much more expensive than the union phase.
 * If the input is not a valid coverage 
 * then in some cases this will detected during processing 
 * and a error will be thrown.
 * Otherwise, the computation will produce output, but it will be invalid.
 * <p>
 * Unioning a valid coverage implies that no new vertices are created.
 * This means that a precision model does not need to be specified.
 * The precision of the vertices in the output geometry is not changed.
 * Because of this no precision reduction is performed.
 * <p>
 * Unioning a linear network is a way of performing 
 * line merging and line dissolving.
 * 
 * @author Martin Davis
 * 
 * @see SegmentExtractingNoder
 *
 */
public class CoverageUnion 
{
  /**
   * Unions a valid polygonal coverage or linear network.
   * 
   * @param coverage a coverage of polygons or lines
   * @return the union of the coverage
   */
  public static Geometry union(Geometry coverage) {
    Noder noder = new SegmentExtractingNoder();
    // a precision model is not needed since no noding is done
    return OverlayNG.union(coverage, null, noder );
  }

  private CoverageUnion() {
    // No instantiation for now
  }
}
