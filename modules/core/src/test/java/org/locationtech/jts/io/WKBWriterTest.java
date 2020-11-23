/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for WKB which test output explicitly.
 * 
 * @author Martin Davis
 *
 */
public class WKBWriterTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(WKBWriterTest.class);
  }
  
  public WKBWriterTest(String name) {
      super(name);
  }
  
  public void testSRID() throws Exception {
      GeometryFactory gf = new GeometryFactory();
      Point p1 = gf.createPoint(new Coordinate(1,2));
      p1.setSRID(1234);
      
      //first write out without srid set
      WKBWriter w = new WKBWriter();
      byte[] wkb = w.write(p1);
      
      //check the 3rd bit of the second byte, should be unset
      byte b = (byte) (wkb[1] & 0x20);
      assertEquals(0, b);
      
      //read geometry back in
      WKBReader r = new WKBReader(gf);
      Point p2 = (Point) r.read(wkb);
      
      assertTrue(p1.equalsExact(p2));
      assertEquals(0, p2.getSRID());
      
      //not write out with srid set
      w = new WKBWriter(2, true);
      wkb = w.write(p1);
      
      //check the 3rd bit of the second byte, should be set
      b = (byte) (wkb[1] & 0x20);
      assertEquals(0x20, b);
      
      int srid = ((int) (wkb[5] & 0xff) << 24) | ( (int) (wkb[6] & 0xff) << 16)
          | ( (int) (wkb[7] & 0xff) << 8) | (( int) (wkb[8] & 0xff) );
     
      assertEquals(1234, srid);
      
      r = new WKBReader(gf);
      p2 = (Point) r.read(wkb);
      
      //read the geometry back in
      assertTrue(p1.equalsExact(p2));
      assertEquals(1234, p2.getSRID());
  }
    
  public void testPointEmpty2D() {
    checkWKB("POINT EMPTY", 2, "0101000000000000000000F87F000000000000F87F" );    
  }
  
  public void testPointEmpty3D() {
    checkWKB("POINT EMPTY", 3, "0101000080000000000000F87F000000000000F87F000000000000F87F" );    
  }
  
  public void testPolygonEmpty2DSRID() {
    checkWKB("POLYGON EMPTY", 2, ByteOrderValues.LITTLE_ENDIAN, 4326, "0103000020E610000000000000" );    
  }
  
  public void testPolygonEmpty2D() {
    checkWKB("POLYGON EMPTY", 2, "010300000000000000" );    
  }
  
  public void testPolygonEmpty3D() {
    checkWKB("POLYGON EMPTY", 3, "010300008000000000" );    
  }
  
  void checkWKB(String wkt, int dimension, String expectedWKBHex) {
    checkWKB(wkt, dimension, ByteOrderValues.LITTLE_ENDIAN, -1, expectedWKBHex);
  }
    
  void checkWKB(String wkt, int dimension, int byteOrder, int srid, String expectedWKBHex) {
    Geometry geom = read(wkt);
    
    // set SRID if not -1
    boolean includeSRID = false;
    if (srid >= 0) {
      includeSRID = true;
      geom.setSRID(srid);
    }
    
    WKBWriter wkbWriter = new WKBWriter(dimension, byteOrder, includeSRID);
    byte[] wkb = wkbWriter.write(geom);
    String wkbHex = WKBWriter.toHex(wkb);
    
    assertEquals(expectedWKBHex, wkbHex);
  }
}
