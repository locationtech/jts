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

package org.locationtech.jts.geom;

import java.util.Comparator;

import org.locationtech.jts.geom.CoordinateArrays.BidirectionalComparator;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests {@link BidirectionalComparator}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class BidirectionalComparatorTest extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(BidirectionalComparatorTest.class);
  }

  public BidirectionalComparatorTest(String name) { super(name); }


  public void testLineString1() throws Exception
  {
    assertTrue(0 == compareBiDir(
                                 "LINESTRING ( 1388155.775 794886.703, 1388170.712 794887.346, 1388185.425 794892.987, 1388195.167 794898.409, 1388210.091 794899.06, 1388235.117 794900.145, 1388250.276 794895.796, 1388270.174 794896.648, 1388280.138 794897.079, 1388295.063 794897.731, 1388310.348 794893.382, 1388330.479 794889.255, 1388345.617 794884.895, 1388360.778 794880.538, 1388366.184 794870.766, 1388366.62 794860.776, 1388362.086 794850.563, 1388357.761 794835.234, 1388343.474 794819.588, 1388339.151 794804.386, 1388320.114 794783.54, 1388310.597 794773.107, 1388301.155 794757.682, 1388286.452 794751.914, 1388282.129 794736.7, 1388273.037 794716.275, 1388278.444 794706.504, 1388293.603 794702.155, 1388303.994 794692.585, 1388319.278 794688.247, 1388339.4 794684.108, 1388369.486 794680.401, 1388394.513 794681.487, 1388409.429 794682.126, 1388433.884 794693.192, 1388454.204 794698.202 )",
                                 "LINESTRING ( 1388454.204 794698.202, 1388433.884 794693.192, 1388409.429 794682.126, 1388394.513 794681.487, 1388369.486 794680.401, 1388339.4 794684.108, 1388319.278 794688.247, 1388303.994 794692.585, 1388293.603 794702.155, 1388278.444 794706.504, 1388273.037 794716.275, 1388282.129 794736.7, 1388286.452 794751.914, 1388301.155 794757.682, 1388310.597 794773.107, 1388320.114 794783.54, 1388339.151 794804.386, 1388343.474 794819.588, 1388357.761 794835.234, 1388362.086 794850.563, 1388366.62 794860.776, 1388366.184 794870.766, 1388360.778 794880.538, 1388345.617 794884.895, 1388330.479 794889.255, 1388310.348 794893.382, 1388295.063 794897.731, 1388280.138 794897.079, 1388270.174 794896.648, 1388250.276 794895.796, 1388235.117 794900.145, 1388210.091 794899.06, 1388195.167 794898.409, 1388185.425 794892.987, 1388170.712 794887.346, 1388155.775 794886.703 )"
                                 ));
  }

  public void testLineString2() throws Exception
  {
    assertTrue(0 == compareBiDir(
                                 "LINESTRING (1389103.293 794193.755, 1389064.931 794188.991)",
                                 "LINESTRING (1389064.931 794188.991, 1389103.293 794193.755)"
                                 ));
  }



  public int compareBiDir(String wkt0, String wkt1)
      throws Exception
  {
    LineString g0 = (LineString) rdr.read(wkt0);
    LineString g1 = (LineString) rdr.read(wkt1);
    Coordinate[] pts0 = g0.getCoordinates();
    Coordinate[] pts1 = g1.getCoordinates();
    Comparator comp = new CoordinateArrays.BidirectionalComparator();
    return comp.compare(pts0, pts1);
  }
}