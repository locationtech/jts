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
import org.locationtech.jts.algorithm.hull.ConstrainedConcaveHull;
import org.locationtech.jts.algorithm.hull.PolygonHull;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.geomfunction.Metadata;

public class HullFunctions {
  public static Geometry convexHull(Geometry g) {      return g.convexHull();  }
 
  public static Geometry concaveHullByLen(Geometry geom, 
      @Metadata(title="Length")
      double maxLen) {
    return ConcaveHull.concaveHullByLength(geom, maxLen);
  }
  
  public static Geometry concaveHullWithHolesByLen(Geometry geom, 
      @Metadata(title="Length")
      double maxLen) {
    return ConcaveHull.concaveHullByLength(geom, maxLen, true);
  }
  
  public static Geometry concaveHullByLenRatio(Geometry geom, 
      @Metadata(title="Length Ratio")
      double maxLen) {
    return ConcaveHull.concaveHullByLengthRatio(geom, maxLen);
  }
  
  public static Geometry concaveHullWithHolesByLenRatio(Geometry geom, 
      @Metadata(title="Length Ratio")
      double maxLen) {
    return ConcaveHull.concaveHullByLengthRatio(geom, maxLen, true);
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
  
  public static Geometry polygonHullByVertexFrac(Geometry geom, 
      @Metadata(title="Vertex Fraction")
      double vertexFrac) {
    return PolygonHull.hull(geom, vertexFrac);
  }
  
  public static Geometry polygonHullByAreaDelta(Geometry geom, 
      @Metadata(title="Area Delta Ratio")
      double areaFrac) {
    return PolygonHull.hullByAreaDelta(geom, areaFrac);
  }
  
  public static Geometry constrainedHull(Geometry geom, 
      @Metadata(title="Area Delta Ratio")
      double areaFrac) {
    return ConstrainedConcaveHull.hull(geom, areaFrac);
  }
  
  

}
