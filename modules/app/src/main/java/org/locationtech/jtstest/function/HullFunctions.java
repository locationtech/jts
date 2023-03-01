/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.algorithm.hull.ConcaveHullOfPolygons;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.geomfunction.Metadata;

public class HullFunctions {
  public static Geometry convexHull(Geometry g) {      return g.convexHull();  }
 
  public static Geometry concaveHullPoints(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxLen) {
    return ConcaveHull.concaveHullByLength(geom, maxLen);
  }
  
  public static Geometry concaveHullPointsWithHoles(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxLen) {
    return ConcaveHull.concaveHullByLength(geom, maxLen, true);
  }
  
  public static Geometry concaveHullPointsByLenRatio(Geometry geom, 
      @Metadata(title="Length Ratio")
      double maxLenRatio) {
    return ConcaveHull.concaveHullByLengthRatio(geom, maxLenRatio);
  }
  
  public static Geometry concaveHullPointsWithHolesByLenRatio(Geometry geom, 
      @Metadata(title="Length Ratio")
      double maxLenRatio) {
    return ConcaveHull.concaveHullByLengthRatio(geom, maxLenRatio, true);
  }
  
  public static Geometry alphaShape(Geometry geom, 
      @Metadata(title="Alpha (Radius)")
      double alpha) {
    return ConcaveHull.alphaShape(geom, alpha, false);
  }
  
  public static Geometry alphaShapeWithHoles(Geometry geom, 
      @Metadata(title="Alpha (Radius)")
      double alpha) {
    return ConcaveHull.alphaShape(geom, alpha, true);
  }
  
  public static double concaveHullLenGuess(Geometry geom) {
    return ConcaveHull.uniformGridEdgeLength(geom);
  }
  
  /**
   * A concaveness measure defined in terms of the perimeter length
   * relative to the convex hull perimeter.
   * <pre>
   * C = ( P(geom) - P(CH) ) / P(CH)
   * </pre>
   * Concaveness values are >= 0.  
   * A convex polygon has C = 0. 
   * A higher concaveness indicates a more concave polygon.
   * <p>
   * Originally defined by Park & Oh, 2012.
   * 
   * @param geom a polygonal geometry
   * @return the concaveness measure of the geometry
   */
  public static double concaveness(Geometry geom) {
    double convexLen = geom.convexHull().getLength();
    return (geom.getLength() - convexLen) / convexLen;
  }
  
  public static Geometry concaveHullPolygons(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxEdgeLen) {
    return ConcaveHullOfPolygons.concaveHullByLength(geom, maxEdgeLen);
  }
  
  public static Geometry concaveHullPolygonsWithHoles(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxEdgeLen) {
    return ConcaveHullOfPolygons.concaveHullByLength(geom, maxEdgeLen, false, true);
  }
  
  public static Geometry concaveHullPolygonsTight(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxEdgeLen) {
    return ConcaveHullOfPolygons.concaveHullByLength(geom, maxEdgeLen, true, false);
  }
  
  public static Geometry concaveHullPolygonsByLenRatio(Geometry geom, 
      @Metadata(title="Edge Length Ratio")
      double maxEdgeLenRatio) {
    return ConcaveHullOfPolygons.concaveHullByLengthRatio(geom, maxEdgeLenRatio);
  }
  
  public static Geometry concaveHullPolygonsTightByLenRatio(Geometry geom, 
      @Metadata(title="Edge Length Ratio")
      double maxEdgeLenRatio) {
    return ConcaveHullOfPolygons.concaveHullByLengthRatio(geom, maxEdgeLenRatio, true, false);
  }
  
  public static Geometry concaveFill(Geometry geom, 
      @Metadata(title="Max Edge Length")
      double maxEdgeLen) {
    return ConcaveHullOfPolygons.concaveFillByLength(geom, maxEdgeLen);
  }
  
  public static Geometry concaveFillByLenRatio(Geometry geom, 
      @Metadata(title="Edge Length Ratio")
      double maxEdgeLenRatio) {
    return ConcaveHullOfPolygons.concaveFillByLengthRatio(geom, maxEdgeLenRatio);
  }
  
}
