/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */
package org.locationtech.jts.geom;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.locationtech.jts.algorithm.exactcurve.ExactCircularArc;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Shelf-a fixtures / golden round-trips for {@code CIRCULARSTRING (5 0, 3 4, 0 5)}.
 * κ=8; centre (0,0) r=5; exact length = 5·π/2 =
 * {@code 0x1.f6a7a2955385fp+2}.
 * <p>
 * WKB hex is vendored as golden bytes only. Year-1 does not implement
 * WKB type 8 (HOLD WKB 13–21).
 *
 * @author Jeroen Bloemscheer
 */
public class CircularStringShelfATest extends GeometryTestCase {

  /** ADR-0006 LENGTH_UNIFIED hex-float for the shelf-a quarter window. */
  public static final String ORACLE_LENGTH_HEX = "0x1.f6a7a2955385fp+2";

  private static final String SHELF_WKT = "CIRCULARSTRING (5 0, 3 4, 0 5)";

  private final WKTReader reader = new WKTReader();
  private final WKTWriter writer = new WKTWriter();

  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  public CircularStringShelfATest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CircularStringShelfATest.class);
  }

  public void testShelfAWktRoundTripAndControlsNotDensify() throws Exception {
    String vendored = readResource("testdata/oracle/circularstring-quarter.wkt").trim();
    assertEquals(SHELF_WKT, vendored);

    Geometry g = reader.read(vendored);
    assertTrue(g instanceof CircularString);
    CircularString cs = (CircularString) g;
    assertEquals(Geometry.TYPENAME_CIRCULARSTRING, cs.getGeometryType());
    assertEquals(SHELF_WKT, writer.write(cs));

    assertEquals("I/O must not silent-densify", 3, cs.getNumPoints());
    assertEquals(3, cs.getCoordinates().length);
    assertEquals(3, cs.getCoordinateSequence().size());
    assertTrue(cs.getCoordinateN(0).equals2D(new Coordinate(5, 0)));
    assertTrue(cs.getCoordinateN(1).equals2D(new Coordinate(3, 4)));
    assertTrue(cs.getCoordinateN(2).equals2D(new Coordinate(0, 5)));

    LineString linear = cs.toLinear(0.01);
    assertTrue("named toLinear is the densify path", linear.getNumPoints() > 3);
    assertEquals(3, cs.getNumPoints());
  }

  public void testShelfAExactLengthVsCertifiedHex() throws Exception {
    CircularString cs = (CircularString) reader.read(SHELF_WKT);
    ExactCircularArc arc = cs.getArcN(0);
    assertTrue(arc.isExact());
    assertFalse(arc.isCollinear());
    assertEquals(5.0, arc.getRadius(), 1e-10);
    assertTrue(arc.getCenter().equals2D(new Coordinate(0, 0)));

    double expected = Double.valueOf(ORACLE_LENGTH_HEX).doubleValue();
    assertEquals(5.0 * Math.PI / 2.0, expected, 1e-15);
    assertEquals(expected, cs.getLength(), 1e-12);
    assertEquals(expected, arc.length(), 1e-12);
  }

  public void testShelfAWkbHexVendoredNotDecoded() throws Exception {
    String ndr = readResource("testdata/oracle/circularstring-quarter.wkb-ndr.hex").trim();
    String xdr = readResource("testdata/oracle/circularstring-quarter.wkb-xdr.hex").trim();
    assertTrue("NDR type 8 CircularString", ndr.toLowerCase().startsWith("01080000"));
    assertTrue("XDR type 8 CircularString", xdr.toLowerCase().startsWith("0000000008"));
    // HOLD WKB 13–21: do not decode these into a JTS CircularString this Year-1.
  }

  public void testLengthUnifiedAgainstOracleBinIfPresent() throws Exception {
    File bin = findOracleBin();
    CircularString cs = (CircularString) reader.read(SHELF_WKT);
    double jtsLen = cs.getLength();
    if (bin == null) {
      // Fixtures + certified hex are the CI path; proofs oracle_bin is optional.
      assertEquals(Double.valueOf(ORACLE_LENGTH_HEX).doubleValue(), jtsLen, 1e-12);
      return;
    }

    ProcessBuilder pb = new ProcessBuilder(bin.getAbsolutePath());
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    OutputStream stdin = proc.getOutputStream();
    stdin.write("LENGTH_UNIFIED\n1\nA 5 0 3 4 0 5\n".getBytes(Charset.forName("UTF-8")));
    stdin.close();
    BufferedReader stdout = new BufferedReader(
        new InputStreamReader(proc.getInputStream(), Charset.forName("UTF-8")));
    String line = stdout.readLine();
    int code = proc.waitFor();
    assertEquals("oracle_bin exit", 0, code);
    assertNotNull(line);
    double oracle = Double.valueOf(line.trim()).doubleValue();
    assertEquals(ORACLE_LENGTH_HEX, line.trim());
    assertEquals(oracle, jtsLen, 1e-12);
  }

  /** Optional proofs LENGTH_UNIFIED checker (not Oracle Database). */
  private static File findOracleBin() {
    String[] candidates = new String[] {
        System.getProperty("jts.oracle.bin"),
        System.getenv("JTS_ORACLE_BIN"),
        "/tmp/oracle-bin/oracle_bin"
    };
    for (int i = 0; i < candidates.length; i++) {
      if (candidates[i] == null || candidates[i].length() == 0) {
        continue;
      }
      File f = new File(candidates[i]);
      if (f.canExecute()) {
        return f;
      }
    }
    return null;
  }

  private static String readResource(String path) throws Exception {
    InputStream in = CircularStringShelfATest.class.getClassLoader().getResourceAsStream(path);
    assertNotNull("missing resource " + path, in);
    BufferedReader r = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = r.readLine()) != null) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(line);
    }
    r.close();
    return sb.toString();
  }
}
