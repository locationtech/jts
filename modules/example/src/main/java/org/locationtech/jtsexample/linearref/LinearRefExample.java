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
package org.locationtech.jtsexample.linearref;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.linearref.*;

/**
 * Examples of Linear Referencing
 *
 * @version 1.7
 */

public class LinearRefExample {

  static GeometryFactory fact = new GeometryFactory();
  static WKTReader rdr = new WKTReader(fact);

  public static void main(String[] args)
      throws Exception
  {
    LinearRefExample example = new LinearRefExample();
    example.run();
  }


  public LinearRefExample() {
  }

  public void run()
      throws Exception
  {
    runExtractedLine("LINESTRING (0 0, 10 10, 20 20)", 1, 10);
    runExtractedLine("MULTILINESTRING ((0 0, 10 10), (20 20, 25 25, 30 40))", 1, 20);
  }

  public void runExtractedLine(String wkt, double start, double end)
    throws ParseException
  {
    System.out.println("=========================");
    Geometry g1 = rdr.read(wkt);
    System.out.println("Input Geometry: " + g1);
    System.out.println("Indices to extract: " + start + " " + end);

    LengthIndexedLine indexedLine = new LengthIndexedLine(g1);

    Geometry subLine = indexedLine.extractLine(start, end);
    System.out.println("Extracted Line: " + subLine);

    double[] index = indexedLine.indicesOf(subLine);
    System.out.println("Indices of extracted line: " + index[0] + " " + index[1]);

    Coordinate midpt = indexedLine.extractPoint((index[0] + index[1]) / 2);
    System.out.println("Midpoint of extracted line: " + midpt);
  }
}