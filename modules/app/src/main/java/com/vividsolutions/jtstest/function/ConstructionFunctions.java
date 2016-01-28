/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.function;

import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.OctagonalEnvelope;

public class ConstructionFunctions {
  public static Geometry octagonalEnvelope(Geometry g) { return OctagonalEnvelope.octagonalEnvelope(g); }
  
  public static Geometry minimumDiameter(Geometry g) {      return (new MinimumDiameter(g)).getDiameter();  }
  public static double minimumDiameterLength(Geometry g) {      return (new MinimumDiameter(g)).getDiameter().getLength();  }

  public static Geometry minimumRectangle(Geometry g) { return (new MinimumDiameter(g)).getMinimumRectangle();  }
  public static Geometry minimumBoundingCircle(Geometry g) { return (new MinimumBoundingCircle(g)).getCircle();  }
  public static Geometry maximumDiameter(Geometry g) {      return 
      g.getFactory().createLineString((new MinimumBoundingCircle(g)).getExtremalPoints());  }
  public static Geometry farthestPoints(Geometry g) { 
      return ((new MinimumBoundingCircle(g)).getFarthestPoints());  }
  public static double maximumDiameterLength(Geometry g) {      return 2 * (new MinimumBoundingCircle(g)).getRadius();  }
  
  public static Geometry boundary(Geometry g) {      return g.getBoundary();  }
  public static Geometry convexHull(Geometry g) {      return g.convexHull();  }
  public static Geometry centroid(Geometry g) {      return g.getCentroid();  }
  public static Geometry interiorPoint(Geometry g) {      return g.getInteriorPoint();  }

  public static Geometry densify(Geometry g, double distance) { return Densifier.densify(g, distance); }
  
}
