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
package org.locationtech.jtsexample.geom;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;

/**
 * An example showing a simple use of JTS methods for:
 * <ul>
 * <li>WKT reading
 * <li>intersection
 * <li>relate
 * <li>WKT output
 * </ul>
 * <p>
 * The expected output from this program is:
 * <pre>
 * ----------------------------------------------------------
 * A = POLYGON ((40 100, 40 20, 120 20, 120 100, 40 100))
 * B = LINESTRING (20 80, 80 60, 100 140)
 * A intersection B = LINESTRING (40 73.33333333333334, 80 60, 90 100)
 * A relate C = 1F20F1102
 * ----------------------------------------------------------
 * </pre>
 *
 * @version 1.7
 */
public class SimpleMethodsExample
{
  public static void main(String[] args) {
    SimpleMethodsExample example = new SimpleMethodsExample();
    try {
      example.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public SimpleMethodsExample() {
  }

  public void run()
    throws ParseException
  {
    GeometryFactory fact = new GeometryFactory();
    WKTReader wktRdr = new WKTReader(fact);

    String wktA = "POLYGON((40 100, 40 20, 120 20, 120 100, 40 100))";
    String wktB = "LINESTRING(20 80, 80 60, 100 140)";
    Geometry A = wktRdr.read(wktA);
    Geometry B = wktRdr.read(wktB);
    Geometry C = A.intersection(B);
    System.out.println("A = " + A);
    System.out.println("B = " + B);
    System.out.println("A intersection B = " + C);
    System.out.println("A relate C = " + A.relate(B));
  }

}
