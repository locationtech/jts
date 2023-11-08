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

package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Tests {@link Geometry#relate}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateTest
    extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateTest.class);
  }

  public RelateTest(String name)
  {
    super(name);
  }

  /**
   * From https://github.com/locationtech/jts/issues/396
   * 
   * The original failure is caused by the intersection computed
   * during noding not lying exactly on each original line segment.
   * This is due to numerical error in the FP intersection algorithm.
   * This is fixed by using DD intersection calculation.
   */
  public void testContainsNoding()
  {
    String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";

    runRelateTest(a, b,  "101F00FF2"    );
  }

  /**
   * From GEOS https://github.com/libgeos/geos/issues/933
   * 
   * The original failure is caused by the intersection computed
   * during noding not lying exactly on each original line segment.
   * This is due to numerical error in the FP intersection algorithm.
   * This is fixed by using DD intersection calculation.
   */
  public void testContainsNoding2()
  {
    String a = "MULTILINESTRING ((0 0, 1 1), (0.5 0.5, 1 0.1, -1 0.1))";
    String b = "LINESTRING (0 0, 1 1)";

    runRelateTest(a, b,  "1F1000FF2"    );
  }
  
  /**
   * Tests case where segments intersect properly, but computed intersection point
   * snaps to a boundary endpoint due to roundoff.
   * Fixed by detecting that computed intersection snapped to a boundary node.
   * 
   * See https://lists.osgeo.org/pipermail/postgis-users/2022-February/045266.html
   */
  public void testIntersectsSnappedEndpoint1()
  {
    String a = "LINESTRING (-29796.696826656284 138522.76848210802, -29804.3911369969 138519.3504205817)";
    String b = "LINESTRING (-29802.795222153436 138520.05937757515, -29802.23305474065 138518.7938969792)";
    runRelateTest(a, b,  "F01FF0102"    );
  }
  
  /**
   * Tests case where segments intersect properly, but computed intersection point
   * snaps to a boundary endpoint due to roundoff.
   * Fixed by detecting that computed intersection snapped to a boundary node.
   * 
   * See https://lists.osgeo.org/pipermail/postgis-users/2022-February/045277.html
   */  
  public void testIntersectsSnappedEndpoint2()
  {
    String a = "LINESTRING (-57.2681216 49.4063466, -57.267725199999994 49.406617499999996, -57.26747895046037 49.406750916517765)";
    String b = "LINESTRING (-57.267475399999995 49.4067465, -57.2675701 49.406864299999995, -57.267989 49.407135399999994)";
    runRelateTest(a, b,  "FF10F0102"  );
  }
  
  public void testMultiPointWithEmpty()
  {
    String a = "MULTIPOINT(EMPTY,(0 0))";
    String b = "POLYGON ((1 0,0 1,-1 0,0 -1, 1 0))";
    runRelateTest(a, b,  "0FFFFF212"  );
  }
  
  void runRelateTest(String wkt1, String wkt2, String expectedIM)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    IntersectionMatrix im = RelateOp.relate(g1, g2);
    String imStr = im.toString();
    //System.out.println(imStr);
    assertEquals(expectedIM, imStr);
  }
}
