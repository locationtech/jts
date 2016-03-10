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

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;



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

