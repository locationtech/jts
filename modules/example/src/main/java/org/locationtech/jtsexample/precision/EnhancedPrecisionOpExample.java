
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
package org.locationtech.jtsexample.precision;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.precision.EnhancedPrecisionOp;

/**
 * Example of using {@link EnhancedPrecisionOp} to avoid robustness problems
 *
 * @version 1.7
 */
public class EnhancedPrecisionOpExample
{
  public static void main(String[] args) throws Exception
  {
    EnhancedPrecisionOpExample example = new EnhancedPrecisionOpExample();
    try {
      example.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private WKTReader reader = new WKTReader();

  public EnhancedPrecisionOpExample() {
  }

  void run()
      throws Exception
  {
    String wkt1, wkt2;
    // two geometries which cause robustness problems
    wkt1 = "POLYGON ((708653.498611049 2402311.54647056, 708708.895756966 2402203.47250014, 708280.326454234 2402089.6337791, 708247.896591321 2402252.48269854, 708367.379593851 2402324.00761653, 708248.882609455 2402253.07294874, 708249.523621829 2402244.3124463, 708261.854734465 2402182.39086576, 708262.818392579 2402183.35452387, 708653.498611049 2402311.54647056))";
    wkt2 = "POLYGON ((708258.754920656 2402197.91172757, 708257.029447455 2402206.56901508, 708652.961095455 2402312.65463437, 708657.068786251 2402304.6356364, 708258.754920656 2402197.91172757))";
    Geometry g1 = reader.read(wkt1);
    Geometry g2 = reader.read(wkt2);

    System.out.println("This call to intersection will throw a topology exception due to robustness problems:");
    try {
      Geometry result = g1.intersection(g2);
    }
    catch (TopologyException ex) {
      ex.printStackTrace();
    }

    System.out.println("Using EnhancedPrecisionOp allows the intersection to be performed with no errors:");
    Geometry result = EnhancedPrecisionOp.intersection(g1, g2);
    System.out.println(result);
  }


}
