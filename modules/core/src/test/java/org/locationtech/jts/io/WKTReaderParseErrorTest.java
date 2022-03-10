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
package org.locationtech.jts.io;

import java.io.IOException;

import org.locationtech.jts.geom.GeometryFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests the {@link WKTReader} with various syntax errors
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

  public void testExtraLParen() throws IOException {
    readWithParseException("POINT (( 1e01 -1E02)");
  }

  public void testMissingOrdinate() throws IOException
  {
    readWithParseException("POINT ( 1e01 )");
  }

  public void testBadChar() throws IOException
  {
    readWithParseException("POINT ( # 1e-04 1E-05)");
  }

  public void testBadExpFormat() throws IOException
  {
    readWithParseException("POINT (1e0a1 1X02)");
  }

  public void testBadExpPlusSign() throws IOException
  {
    readWithParseException("POINT (1e+01 1X02)");
  }

  public void testBadPlusSign() throws IOException
  {
    readWithParseException("POINT ( +1e+01 1X02)");
  }

  public void testBadCharsInType() throws IOException
  {
    readWithParseException("POINTABC ( 0 0 )");
    readWithParseException("LINESTRINGABC ( 0 0 )");
    readWithParseException("LINEARRINGABC ( 0 0, 0 0, 0 0 )");
    readWithParseException("POLYGONABC (( 0 0, 0 0, 0 0, 0 0 ))");
    readWithParseException("MULTIPOINTABC (( 0 0 ), ( 0 0 ))");
    readWithParseException("MULTILINESTRINGABC (( 0 0, 1 1 ), ( 0 0, 1 1 ))");
    readWithParseException("MULTIPOLYGONABC ((( 0 0, 1 1, 2 2, 0 0 )), (( 0 0, 1 1, 2 2, 0 0 )))");
    readWithParseException("GEOMETRYCOLLECTIONABC (POINT( 0 0 ), LINESTRING( 0 0, 1 1))");
  }

  public void testBadCharsInTypeZ() throws IOException
  {
    readWithParseException("POINTABCZ ( 0 0 )");
    readWithParseException("LINESTRINGABCZ ( 0 0 )");
    readWithParseException("LINEARRINGABCZ ( 0 0, 0 0, 0 0 )");
    readWithParseException("POLYGONABCZ (( 0 0, 0 0, 0 0, 0 0 ))");
    readWithParseException("MULTIPOINTABCZ (( 0 0 ), ( 0 0 ))");
    readWithParseException("MULTILINESTRINGABCZ (( 0 0, 1 1 ), ( 0 0, 1 1 ))");
    readWithParseException("MULTIPOLYGONABCZ ((( 0 0, 1 1, 2 2, 0 0 )), (( 0 0, 1 1, 2 2, 0 0 )))");
    readWithParseException("GEOMETRYCOLLECTIONABCZ (POINT( 0 0 ), LINESTRING( 0 0, 1 1))");
  }

  public void testBadCharsInTypeM() throws IOException
  {
    readWithParseException("LINESTRINGABCM ( 0 0 0, 1 1 1 )");
  }

  public void testBadCharsInTypeZM() throws IOException
  {
    readWithParseException("LINESTRINGABCZM ( 0 0 0 0, 1 1 1 1 )");
  }

  private void readWithParseException(String wkt)
      throws IOException
  {
    boolean threwParseEx = false;
    try {
      rdr.read(wkt);
    }
    catch (ParseException ex) {
      //System.out.println(ex.getMessage());
      threwParseEx = true;
    }
    assertTrue(threwParseEx);
  }
}

