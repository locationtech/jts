

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
package org.locationtech.jtstest.testrunner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.WKBWriter;


/**
 * @version 1.7
 */
public class GeometryResult implements Result {
  private Geometry geometry;
  private boolean outputwkb = System.getenv("JTSTEST_WKB_OUTPUT") != null;

  public GeometryResult(Geometry geometry) {
    this.geometry = geometry;
  }

  public void setWKBOutput(boolean newval) {
    this.outputwkb = newval;
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
    if ( this.outputwkb ) {
      WKBWriter wkbwriter = new WKBWriter();
      return WKBWriter.toHex(wkbwriter.write(geometry));
    } else {
      return geometry.toText();
    }
  }

  public String toFormattedString() {
    if ( this.outputwkb ) {
      WKBWriter wkbwriter = new WKBWriter();
      return WKBWriter.toHex(wkbwriter.write(geometry));
    } else {
      WKTWriter writer = new WKTWriter();
      return writer.writeFormatted(geometry);
    }
  }

  public String toShortString() {
    return geometry.getClass().getName();
  }
}

