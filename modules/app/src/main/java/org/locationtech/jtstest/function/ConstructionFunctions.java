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

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.algorithm.MinimumBoundingTriangle;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.algorithm.MinimumAreaRectangle;
import org.locationtech.jts.algorithm.construct.LargestEmptyCircle;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.OctagonalEnvelope;
import org.locationtech.jtstest.geomfunction.Metadata;

public class ConstructionFunctions {
  public static Geometry octagonalEnvelope(Geometry g) { return OctagonalEnvelope.octagonalEnvelope(g); }
  
  public static Geometry minimumDiameter(Geometry g) {      return (new MinimumDiameter(g)).getDiameter();  }
  public static double minimumDiameterLength(Geometry g) {      return (new MinimumDiameter(g)).getDiameter().getLength();  }
  public static Geometry minimumDiameterRectangle(Geometry g) { return MinimumDiameter.getMinimumRectangle(g);  }

  public static Geometry minimumAreaRectangle(Geometry g) { return MinimumAreaRectangle.getMinimumRectangle(g);  }
    
  public static Geometry minimumBoundingCircle(Geometry g) { return (new MinimumBoundingCircle(g)).getCircle();  }
  public static double minimumBoundingCircleDiameterLen(Geometry g) {      return 2 * (new MinimumBoundingCircle(g)).getRadius();  }

  public static Geometry minimumBoundingTriangle(Geometry g) { return (new MinimumBoundingTriangle(g)).getTriangle();  }

  public static Geometry maximumDiameter(Geometry g) {      return (new MinimumBoundingCircle(g)).getMaximumDiameter();  }
  public static double maximumDiameterLength(Geometry g) {  
    return (new MinimumBoundingCircle(g)).getMaximumDiameter().getLength();
  }
  
  public static Geometry boundary(Geometry g) {      return g.getBoundary();  }
  public static Geometry convexHull(Geometry g) {      return g.convexHull();  }
  public static Geometry centroid(Geometry g) {      return g.getCentroid();  }
  public static Geometry interiorPoint(Geometry g) {      return g.getInteriorPoint();  }

  public static Geometry densify(Geometry g, double distance) { return Densifier.densify(g, distance); }
  
  //--------------------------------------------
  
  @Metadata(description="Constructs the Maximum Inscribed Circle of a polygonal geometry")
  public static Geometry maxInscribedCircle(Geometry g,
      @Metadata(title="Distance tolerance")
      double tolerance) { 
    MaximumInscribedCircle mic = new MaximumInscribedCircle(g, tolerance); 
    Coordinate center = mic.getCenter().getCoordinate();
    Coordinate radiusPt = mic.getRadiusPoint().getCoordinate();
    LineString radiusLine = g.getFactory().createLineString(new Coordinate[] { center, radiusPt });
    return circleByRadiusLine(radiusLine, 60);
  }
  
  @Metadata(description="Constructs the center point of the Maximum Inscribed Circle of a polygonal geometry")
  public static Geometry maxInscribedCircleCenter(Geometry g,
      @Metadata(title="Distance tolerance")
      double tolerance) { 
    return MaximumInscribedCircle.getCenter(g, tolerance); 
  }
  
  @Metadata(description="Constructs a radius line of the Maximum Inscribed Circle of a polygonal geometry")
  public static Geometry maxInscribedCircleRadius(Geometry g,
      @Metadata(title="Distance tolerance")
      double tolerance) { 
    MaximumInscribedCircle mic = new MaximumInscribedCircle(g, tolerance); 
    return mic.getRadiusLine(); 
  }

  @Metadata(description="Computes the radius of the Maximum Inscribed Circle of a polygonal geometry")
  public static double maxInscribedCircleRadiusLen(Geometry g,
      @Metadata(title="Distance tolerance")
      double tolerance) { 
    MaximumInscribedCircle mic = new MaximumInscribedCircle(g, tolerance); 
    return mic.getRadiusLine().getLength(); 
  }

  //--------------------------------------------
  
  @Metadata(description="Constructs the Largest Empty Circle in a set of obstacles")
  public static Geometry largestEmptyCircle(Geometry obstacles, Geometry boundary,
      @Metadata(title="Accuracy distance tolerance")
      double tolerance) { 
    LineString radiusLine = LargestEmptyCircle.getRadiusLine(obstacles, boundary, tolerance);
    return circleByRadiusLine(radiusLine, 60);
  }
  
  @Metadata(description="Computes a radius line of the Largest Empty Circle in a set of obstacles")
  public static Geometry largestEmptyCircleCenter(Geometry obstacles, Geometry boundary,
      @Metadata(title="Accuracy distance tolerance")
      double tolerance) { 
    return LargestEmptyCircle.getCenter(obstacles, boundary, tolerance); 
  }
  
  @Metadata(description="Computes a radius line of the Largest Empty Circle in a set of obstacles")
  public static Geometry largestEmptyCircleRadius(Geometry obstacles, Geometry boundary, 
      @Metadata(title="Accuracy distance tolerance")
      double tolerance) { 
    return LargestEmptyCircle.getRadiusLine(obstacles, boundary, tolerance); 
  }
  
  //--------------------------------------------

  @Metadata(description="Constructs an n-point circle from a 2-point line giving the radius")
  public static Geometry circleByRadiusLine(Geometry radiusLine,
      @Metadata(title="Number of vertices")
      int nPts) {
    Coordinate[] radiusPts = radiusLine.getCoordinates();
    Coordinate center = radiusPts[0];
    Coordinate radiusPt = radiusPts[1];
    double dist = radiusPt.distance(center);
    
    double angInc = 2 * Math.PI / (nPts - 1);
    Coordinate[] circlePts = new Coordinate[nPts + 1];
    circlePts[0] = radiusPt.copy();
    circlePts[nPts] = radiusPt.copy();
    double angStart = Angle.angle(center, radiusPt);
    for (int i = 1; i < nPts; i++) {
      double x = center.getX() + dist * Math.cos(angStart + i * angInc);
      double y = center.getY() + dist * Math.sin(angStart + i * angInc);
      circlePts[i] =  new Coordinate(x,y);
    }
    return radiusLine.getFactory().createPolygon(circlePts);
  }
 
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
  
}
