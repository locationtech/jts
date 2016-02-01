/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.operation.union;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import org.locationtech.jts.io.ParseException;

import junit.framework.TestCase;
import test.jts.junit.GeometryUtils;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using data from files.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionFileTest extends TestCase 
{
  public CascadedPolygonUnionFileTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionFileTest.class);
  }
  
  public void testAfrica2()
  throws Exception
  {
    runTestResource("../../../../../data/africa.wkt", 
        CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void XtestEurope()
  throws Exception
  {
    runTestResource("../../../../../data/europe.wkt", 
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();
  
  private void runTest(String filename, double minimumMeasure) 
  throws IOException, ParseException
  {
    Collection geoms = GeometryUtils.readWKTFile(filename);
    assertTrue(tester.test(geoms, minimumMeasure));
  }
  private void runTestResource(String resource, double minimumMeasure) 
  throws IOException, ParseException
  {
    InputStream is = this.getClass().getResourceAsStream(resource);
    // don't bother if file is missing
    if (is == null) return;
    Collection geoms = GeometryUtils.readWKTFile(new InputStreamReader(is));
    assertTrue(tester.test(geoms, minimumMeasure));
  }
  
}
