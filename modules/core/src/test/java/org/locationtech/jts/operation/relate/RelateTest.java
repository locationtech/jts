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

package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests {@link Geometry#relate}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public RelateTest(String name)
  {
    super(name);
  }

  /**
   * From GEOS #572
   * 
   * The cause is that the longer line nodes the single-segment line.
   * The node then tests as not lying precisely on the original longer line.
   * 
   * @throws Exception
   */
  public void testContainsIncorrectIMMatrix()
      throws Exception
  {
    String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";

    // actual matrix is 001F001F2
    // true matrix should be 101F00FF2
    runRelateTest(a, b,  "001F001F2"    );
  }

  void runRelateTest(String wkt1, String wkt2, String expectedIM)
      throws ParseException
  {
    Geometry g1 = rdr.read(wkt1);
    Geometry g2 = rdr.read(wkt2);
    IntersectionMatrix im = RelateOp.relate(g1, g2);
    String imStr = im.toString();
    //System.out.println(imStr);
    assertTrue(im.matches(expectedIM));
  }
}