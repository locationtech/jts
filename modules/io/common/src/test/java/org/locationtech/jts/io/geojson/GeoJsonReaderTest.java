/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.geojson;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import test.jts.GeometryTestCase;

public class GeoJsonReaderTest extends GeometryTestCase {

  public GeoJsonReader geoJsonRdr;

  public GeoJsonReaderTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    this.geoJsonRdr = new GeoJsonReader();
  }

  public void testEmptyArray() throws ParseException {
    runParseEx("[]");
  }
 
  public void testEmptyObject() throws ParseException {
    runParseEx("{}");
  }
 
  private void runParseEx(String json) {
    try {
      Geometry geom = geoJsonRdr.read(json);
    }
    catch (ParseException ex) {
      assertTrue(true);
    }
  }

}
