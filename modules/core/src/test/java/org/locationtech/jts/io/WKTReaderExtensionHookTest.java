/*
 * Copyright (c) 2026 grootstebozewolf
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
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.EnumSet;

import org.locationtech.jts.geom.Geometry;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Verifies the {@link WKTReader#readOtherGeometryText} and
 * {@link WKTWriter#appendOtherGeometryTaggedText} extension hooks added
 * for SFA / ISO 19125-2 extended geometry support, without taking any
 * dependency on jts-curved. A dummy subclass of {@link WKTReader}
 * recognises a made-up keyword and a dummy subclass of
 * {@link WKTWriter} emits it; this confirms the seam is wired and the
 * promoted helpers are accessible across packages.
 */
public class WKTReaderExtensionHookTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTReaderExtensionHookTest.class); }

  public WKTReaderExtensionHookTest(String name) { super(name); }

  /** A reader that recognises a made-up {@code DUMMYTYPE} keyword and
   *  returns an empty Point. Exercises the protected helpers. */
  private static class DummyReader extends WKTReader {
    boolean hookCalled = false;

    @Override
    protected Geometry readOtherGeometryText(StreamTokenizer t, String type, EnumSet<Ordinate> ord)
        throws IOException, ParseException {
      if ("DUMMYTYPE".equals(type)) {
        hookCalled = true;
        // Use the promoted-protected helpers from core.
        String tok = getNextEmptyOrOpener(t);
        if (!WKTConstants.EMPTY.equals(tok)) {
          // burn through the body to a balanced ')'
          int depth = 1;
          while (depth > 0) {
            int c = t.nextToken();
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == StreamTokenizer.TT_EOF)
              throw parseErrorWithLine(t, "Unexpected EOF in DUMMYTYPE body");
          }
        }
        return geometryFactory.createPoint();
      }
      return super.readOtherGeometryText(t, type, ord);
    }
  }

  /** A writer that intercepts a dummy custom geometry type. */
  private static class DummyWriter extends WKTWriter {
    boolean hookCalled = false;

    @Override
    protected boolean appendOtherGeometryTaggedText(Geometry geometry, EnumSet<Ordinate> outputOrdinates,
        boolean useFormatting, int level, Writer writer, OrdinateFormat formatter) throws IOException {
      // Pretend we have a custom type: any Geometry whose toString starts with "POINT"
      // (i.e. all Points) should be emitted with our marker. This confirms the hook
      // runs before the instanceof ladder.
      if ("Point".equals(geometry.getGeometryType()) && !geometry.isEmpty()) {
        writer.write("DUMMYTYPE EMPTY");
        return true;
      }
      return false;
    }
  }

  public void testReaderHookIsInvokedForUnknownType() throws Exception {
    DummyReader reader = new DummyReader();
    Geometry g = reader.read("DUMMYTYPE EMPTY");
    assertTrue("readOtherGeometryText should have been called", reader.hookCalled);
    assertEquals("Point", g.getGeometryType());
  }

  public void testReaderHookHandlesParenthesisedBody() throws Exception {
    DummyReader reader = new DummyReader();
    Geometry g = reader.read("DUMMYTYPE (1 2, 3 4)");
    assertTrue(reader.hookCalled);
    assertNotNull(g);
  }

  public void testReaderHookFallsThroughToCoreError() {
    try {
      new DummyReader().read("UNKNOWNTYPE EMPTY");
      fail("Expected ParseException for unknown type");
    } catch (Throwable e) {
      assertTrue("Expected ParseException, got: " + e, e instanceof ParseException);
    }
  }

  public void testCoreReaderStillThrowsForUnknownType() {
    try {
      new WKTReader().read("DUMMYTYPE EMPTY");
      fail("Expected ParseException from default WKTReader");
    } catch (Throwable e) {
      assertTrue("Expected ParseException, got: " + e, e instanceof ParseException);
    }
  }

  public void testWriterHookFiresBeforeInstanceofLadder() throws Exception {
    DummyWriter writer = new DummyWriter();
    Geometry pt = read("POINT (1 2)");
    String wkt = writer.write(pt);
    assertEquals("Hook should have intercepted before the Point branch",
        "DUMMYTYPE EMPTY", wkt);
  }

  public void testWriterDefaultHookReturnsFalse() throws Exception {
    // The default WKTWriter must keep writing "POINT (...)" — confirms the hook
    // returning false does not skip the standard path.
    Geometry pt = read("POINT (1 2)");
    String wkt = new WKTWriter().write(pt);
    assertTrue("Default writer should emit POINT, got: " + wkt, wkt.toUpperCase().startsWith("POINT"));
  }
}
