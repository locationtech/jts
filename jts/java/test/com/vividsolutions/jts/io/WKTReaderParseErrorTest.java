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
package com.vividsolutions.jts.io;

import java.io.IOException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Tests the {@link WKTReader} with various errors
 */
public class WKTReaderParseErrorTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(WKTReaderParseErrorTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public WKTReaderParseErrorTest(String name)
  {
    super(name);
  }

  public void testExtraLParen() throws IOException, ParseException
  {
    readBad("POINT (( 1e01 -1E02)");
  }

  public void testMissingOrdinate() throws IOException, ParseException
  {
    readBad("POINT ( 1e01 )");
  }

  public void testBadChar() throws IOException, ParseException
  {
    readBad("POINT ( # 1e-04 1E-05)");
  }

  public void testBadExpFormat() throws IOException, ParseException
  {
    readBad("POINT (1e0a1 1X02)");
  }

  public void testBadExpPlusSign() throws IOException, ParseException
  {
    readBad("POINT (1e+01 1X02)");
  }

  public void testBadPlusSign() throws IOException, ParseException
  {
    readBad("POINT ( +1e+01 1X02)");
  }

  private void readBad(String wkt)
      throws IOException
  {
    boolean threwParseEx = false;
    try {
      Geometry g = rdr.read(wkt);
    }
    catch (ParseException ex) {
      System.out.println(ex.getMessage());
      threwParseEx = true;
    }
    assertTrue(threwParseEx);
  }
}

