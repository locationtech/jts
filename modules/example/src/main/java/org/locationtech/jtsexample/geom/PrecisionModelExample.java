
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
 * An example showing the results of using different precision models
 * in computations involving geometric constructions.
 * A simple intersection computation is carried out in three different
 * precision models (Floating, FloatingSingle and Fixed with 0 decimal places).
 * The input is the same in all cases (since it is precise in all three models),
 * The output shows the effects of rounding in the single-precision and fixed-precision
 * models.
 *
 * @version 1.7
 */
public class PrecisionModelExample
{
  public static void main(String[] args) {
    PrecisionModelExample example = new PrecisionModelExample();
    try {
      example.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public PrecisionModelExample() {
  }

  public void run()
    throws ParseException
  {
    example1();
    example2();
  }

  public void example1()
    throws ParseException
  {
    System.out.println("-------------------------------------------");
    System.out.println("Example 1 shows roundoff from computing in different precision models");
    String wktA = "POLYGON ((60 180, 160 260, 240 80, 60 180))";
    String wktB = "POLYGON ((200 260, 280 160, 80 100, 200 260))";
    System.out.println("A = " + wktA);
    System.out.println("B = " + wktB);

    intersection(wktA, wktB, new PrecisionModel());
    intersection(wktA, wktB, new PrecisionModel(PrecisionModel.FLOATING_SINGLE));
    intersection(wktA, wktB, new PrecisionModel(1));
  }

  public void example2()
    throws ParseException
  {
    System.out.println("-------------------------------------------");
    System.out.println("Example 2 shows that roundoff can change the topology of geometry computed in different precision models");
    String wktA = "POLYGON ((0 0, 160 0, 160 1, 0 0))";
    String wktB = "POLYGON ((40 60, 40 -20, 140 -20, 140 60, 40 60))";
    System.out.println("A = " + wktA);
    System.out.println("B = " + wktB);

    difference(wktA, wktB, new PrecisionModel());
    difference(wktA, wktB, new PrecisionModel(1));
  }


  public void intersection(String wktA, String wktB, PrecisionModel pm)
      throws ParseException
  {
    System.out.println("Running example using Precision Model = " + pm);
    GeometryFactory fact = new GeometryFactory(pm);
    WKTReader wktRdr = new WKTReader(fact);

    Geometry A = wktRdr.read(wktA);
    Geometry B = wktRdr.read(wktB);
    Geometry C = A.intersection(B);

    System.out.println("A intersection B = " + C);
  }

  public void difference(String wktA, String wktB, PrecisionModel pm)
      throws ParseException
  {
    System.out.println("-------------------------------------------");
    System.out.println("Running example using Precision Model = " + pm);
    GeometryFactory fact = new GeometryFactory(pm);
    WKTReader wktRdr = new WKTReader(fact);

    Geometry A = wktRdr.read(wktA);
    Geometry B = wktRdr.read(wktB);
    Geometry C = A.difference(B);

    System.out.println("A intersection B = " + C);
  }
}
