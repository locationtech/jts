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
package org.locationtech.jts.io;

import org.locationtech.jts.geom.CoordinateSequenceComparator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests for reading WKB.
 * 
 * @author Martin Davis
 *
 */
public class WKBReaderTest  extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(WKBReaderTest.class);
  }

  private GeometryFactory geomFactory = new GeometryFactory();
  private WKTReader rdr = new WKTReader(geomFactory);

  public void testShortPolygons() throws ParseException
  {
    // one point
    checkWKBGeometry("0000000003000000010000000140590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200))");
    // two point
    checkWKBGeometry("000000000300000001000000024059000000000000406900000000000040590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200))");
  }

  public WKBReaderTest(String name) { super(name); }

  public void testSinglePointLineString() throws ParseException
  {
    checkWKBGeometry("00000000020000000140590000000000004069000000000000", "LINESTRING (100 200, 100 200)");
  }

   /**
    * After removing the 39 bytes of MBR info at the front, and the
    * end-of-geometry byte, * Spatialite native BLOB is very similar
    * to WKB, except instead of a endian marker at the start of each
    * geometry in a multi-geometry, it has a start marker of 0x69.
    * Endianness is determined by the endian value of the multigeometry.
    *
    * @throws ParseException
    */
  public void testSpatialiteMultiGeometry() throws ParseException
  {
     //multipolygon
     checkWKBGeometry("01060000000200000069030000000100000004000000000000000000444000000000000044400000000000003440000000000080464000000000008046400000000000003E4000000000000044400000000000004440690300000001000000040000000000000000003E40000000000000344000000000000034400000000000002E40000000000000344000000000000039400000000000003E400000000000003440",
           "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((30 20, 20 15, 20 25, 30 20)))'");

     //multipoint
     checkWKBGeometry("0104000000020000006901000000000000000000F03F000000000000F03F690100000000000000000000400000000000000040",
           "MULTIPOINT(1 1,2 2)'");

     //multiline
     checkWKBGeometry("010500000002000000690200000003000000000000000000244000000000000024400000000000003440000000000000344000000000000024400000000000004440690200000004000000000000000000444000000000000044400000000000003E400000000000003E40000000000000444000000000000034400000000000003E400000000000002440",
           "MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10))");

     //geometrycollection
     checkWKBGeometry(
           "010700000002000000690100000000000000000010400000000000001840690200000002000000000000000000104000000000000018400000000000001C400000000000002440",
           "GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))"
     );
  }

  /**
   * Not yet implemented satisfactorily.
   * 
   * @throws ParseException
   */
  public void XXtestIllFormedWKB() throws ParseException
  {
    // WKB is missing LinearRing entry
    checkWKBGeometry("00000000030000000140590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200)");
  }


  private static CoordinateSequenceComparator comp2 = new CoordinateSequenceComparator(2);

  private void checkWKBGeometry(String wkbHex, String expectedWKT) throws ParseException
  {
    WKBReader wkbReader = new WKBReader(geomFactory);
    byte[] wkb = WKBReader.hexToBytes(wkbHex);
    Geometry g2 = wkbReader.read(wkb);
    
    Geometry expected = rdr.read(expectedWKT);
    
   boolean isEqual = (expected.compareTo(g2, comp2) == 0);
    assertTrue(isEqual);

 }
}
