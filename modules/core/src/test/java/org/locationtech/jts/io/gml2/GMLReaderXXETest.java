/*
 * Copyright (c) 2026 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.gml2;

import java.io.File;
import java.nio.file.Files;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests that {@link GMLReader} does not resolve external XML entities (XXE).
 * GML is frequently read from untrusted sources (files, WFS responses, uploads),
 * so the underlying SAX parser must not fetch external entities or process DTDs.
 */
public class GMLReaderXXETest extends GeometryTestCase {

  public static void main(String[] args) {
    TestRunner.run(GMLReaderXXETest.class);
  }

  public GMLReaderXXETest(String name) {
    super(name);
  }

  /**
   * An external general entity referencing a local file must not be resolved.
   * Without the hardening the file content leaks into the parse (and, here, into
   * the exception raised while parsing it as a coordinate); with it, the DOCTYPE
   * is rejected before any entity is resolved.
   */
  public void testExternalEntityIsNotResolved() throws Exception {
    File secretFile = File.createTempFile("jts-xxe", ".txt");
    String secret = "JTS-XXE-CANARY-SECRET";
    Files.write(secretFile.toPath(), secret.getBytes("UTF-8"));
    try {
      String gml =
            "<?xml version=\"1.0\"?>\n"
          + "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"" + secretFile.toURI() + "\"> ]>\n"
          + "<gml:Point><gml:coordinates>&xxe;</gml:coordinates></gml:Point>";
      String observed;
      try {
        observed = String.valueOf(new GMLReader().read(gml, null));
      }
      catch (Exception e) {
        // The parser may legitimately reject the input. Either way the external
        // file's content must not appear, neither in the parsed geometry nor in
        // the message of the exception raised while parsing it.
        observed = String.valueOf(e.getMessage());
      }
      assertFalse("GMLReader resolved an external entity (XXE): " + observed,
          observed.contains(secret));
    }
    finally {
      secretFile.delete();
    }
  }

  /**
   * A DOCTYPE declaration must be rejected outright (billion-laughs / DTD surface).
   */
  public void testDoctypeIsRejected() throws Exception {
    String gml =
          "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE foo>\n"
        + "<gml:Point><gml:coordinates>5,10</gml:coordinates></gml:Point>";
    try {
      new GMLReader().read(gml, null);
      fail("expected a DOCTYPE to be rejected");
    }
    catch (Exception e) {
      // expected: parser refuses the DOCTYPE
    }
  }

  /**
   * Legitimate GML without a DOCTYPE must still parse unchanged.
   */
  public void testBenignGmlStillParses() throws Exception {
    Geometry g = new GMLReader().read(
        "<gml:Point><gml:coordinates>5,10</gml:coordinates></gml:Point>", null);
    assertNotNull(g);
    assertEquals("POINT (5 10)", g.toText());
  }
}
