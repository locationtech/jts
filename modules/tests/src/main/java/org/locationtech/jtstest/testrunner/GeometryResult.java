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
package org.locationtech.jtstest.testrunner;
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
    boolean isEqual = thisGeometryClone.equalsExact(otherGeometryClone, tolerance);
    return isEqual;
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

