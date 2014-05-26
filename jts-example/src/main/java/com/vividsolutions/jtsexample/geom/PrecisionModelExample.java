
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;

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
