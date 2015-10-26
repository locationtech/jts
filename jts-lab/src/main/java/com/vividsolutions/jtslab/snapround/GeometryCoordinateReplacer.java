package com.vividsolutions.jtslab.snapround;

import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jtslab.geom.util.GeometryEditorEx.CoordinateSequenceOperation;

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