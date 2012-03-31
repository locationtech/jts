package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.CGAlgorithmsDD;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class OrientationFunctions
{
  public static int orientationIndex(Geometry segment, Geometry ptGeom) {
    if (segment.getNumPoints() != 2 || ptGeom.getNumPoints() != 1) {
      throw new IllegalArgumentException("A must have two points and B must have one");
    }
    Coordinate[] segPt = segment.getCoordinates();
    
    Coordinate p = ptGeom.getCoordinate();
    int index = CGAlgorithms.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

  public static int orientationIndexDD(Geometry segment, Geometry ptGeom) {
    if (segment.getNumPoints() != 2 || ptGeom.getNumPoints() != 1) {
      throw new IllegalArgumentException("A must have two points and B must have one");
    }
    Coordinate[] segPt = segment.getCoordinates();
    
    Coordinate p = ptGeom.getCoordinate();
    int index = CGAlgorithmsDD.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

}
