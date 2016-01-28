

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
package com.vividsolutions.jtstest.testrunner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;


/**
 * @version 1.7
 */
public class GeometryResult implements Result {
  private Geometry geometry;

  public GeometryResult(Geometry geometry) {
    this.geometry = geometry;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public boolean equals(Result other, double tolerance) {
    if (!(other instanceof GeometryResult)) {
      return false;
    }
    GeometryResult otherGeometryResult = (GeometryResult) other;
    Geometry otherGeometry = otherGeometryResult.geometry;

    Geometry thisGeometryClone = (Geometry)geometry.clone();
    Geometry otherGeometryClone =(Geometry) otherGeometry.clone();
    thisGeometryClone.normalize();
    otherGeometryClone.normalize();
    return thisGeometryClone.equalsExact(otherGeometryClone, tolerance);
  }

  public String toLongString() {
    return geometry.toText();
  }

  public String toFormattedString() {
    WKTWriter writer = new WKTWriter();
    return writer.writeFormatted(geometry);
  }

  public String toShortString() {
    return geometry.getClass().getName();
  }
}

