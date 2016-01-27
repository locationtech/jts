
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
package test.jts;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.valid.IsValidOp;


/**
 * @version 1.7
 */
public class IsValidTester {

  public IsValidTester() {
  }
  public static void main(String[] args) throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory());
    Geometry g = reader.read("GEOMETRYCOLLECTION (POINT (110 300), POINT (100 110), POINT (130 210), POINT (150 210), POINT (150 180), POINT (130 170), POINT (140 190), POINT (130 200), LINESTRING (240 50, 210 120, 270 80, 250 140, 330 70, 300 160, 340 130, 340 130), POLYGON ((210 340, 220 260, 150 270, 230 220, 230 140, 270 210, 360 240, 260 250, 260 280, 240 270, 210 340), (230 270, 230 250, 200 250, 240 220, 240 190, 260 220, 290 230, 250 230, 230 270)))");
    IsValidOp op = new IsValidOp(g);
    if (!op.isValid()) {
      System.out.println(op.getValidationError().getMessage());
    }
    else {
      System.out.println("OK");
    }
  }
}
