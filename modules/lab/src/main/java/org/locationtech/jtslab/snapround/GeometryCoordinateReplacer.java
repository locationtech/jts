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
package org.locationtech.jtslab.snapround;

import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jtslab.geom.util.GeometryEditorEx.CoordinateSequenceOperation;

class GeometryCoordinateReplacer extends CoordinateSequenceOperation {

  private Map geometryLinesMap;

  public GeometryCoordinateReplacer(Map linesMap) {
    this.geometryLinesMap = linesMap;
  }
  
  /**
   * Gets the snapped coordinate array for an atomic geometry,
   * or null if it has collapsed.
   * 
   * @return the snapped coordinate array for this geometry
   * @return null if the snapped coordinates have collapsed, or are missing
   */
  public CoordinateSequence edit(CoordinateSequence coordSeq, Geometry geometry, GeometryFactory targetFactory) {
    if (geometryLinesMap.containsKey(geometry)) {
      Coordinate[] pts = (Coordinate[]) geometryLinesMap.get(geometry);
      // Assert: pts should always have length > 0
      boolean isValidPts = isValidSize(pts, geometry);
      if (! isValidPts) return null;
      return targetFactory.getCoordinateSequenceFactory().create(pts);
    }
    //TODO: should this return null if no matching snapped line is found
    // probably should never reach here?
    return coordSeq;
  }

  /**
   * Tests if a coordinate array has a size which is 
   * valid for the containing geometry.
   * 
   * @param pts the point list to validate
   * @param geom the atomic geometry containing the point list
   * @return true if the coordinate array is a valid size
   */
  private static boolean isValidSize(Coordinate[] pts, Geometry geom) {
    if (pts.length == 0) return true;
    int minSize = minimumNonEmptyCoordinatesSize(geom);
    if (pts.length < minSize) {
      return false;
    }
    return true;
  }

  private static int minimumNonEmptyCoordinatesSize(Geometry geom) {
    if (geom instanceof LinearRing)
      return 4;
    if (geom instanceof LineString)
      return 2;
    return 0;
  }
}