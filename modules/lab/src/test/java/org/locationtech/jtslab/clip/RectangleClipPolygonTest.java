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
package org.locationtech.jtslab.clip;

import junit.textui.TestRunner;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class RectangleClipPolygonTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(RectangleClipPolygonTest.class);
  }
  
  public RectangleClipPolygonTest(String name) {
    super(name);
  }

  public void testSimple() {
    checkClip(
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))",
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((150 200, 200 200, 200 150, 150 150, 150 200))"
        );
  }
  
  public void testOutside() {
    checkClip(
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))",
        "POLYGON ((50 100, 100 100, 100 50, 50 50, 50 100))",
        "POLYGON EMPTY"
        );
  }
  
  public void testMultiOneOutside() {
    checkClip(
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))",
        "MULTIPOLYGON (((50 100, 100 100, 100 50, 50 50, 50 100)), ((200 300, 300 300, 300 200, 200 200, 200 300)))",
        "POLYGON ((200 200, 200 250, 250 250, 250 200, 200 200))"
        );
  }
  
  private void checkClip(String rectWKT, String inputWKT, String expectedWKT) {
    Geometry rect = read(rectWKT);
    Geometry input = read(inputWKT);
    Geometry expected = read(expectedWKT);
    
    Geometry result = RectangleClipPolygon.clip(input, rect);
    
    checkEqual(expected, result);
  }
}
