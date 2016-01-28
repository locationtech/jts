
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
package com.vividsolutions.jtstest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;

import com.vividsolutions.jtstest.testrunner.TestCase;
import com.vividsolutions.jtstest.testrunner.TestReader;
import com.vividsolutions.jtstest.testrunner.TestRun;
import com.vividsolutions.jtstest.util.FileUtil;
import com.vividsolutions.jtstest.util.StringUtil;


/**
 * @version 1.7
 */
public class TestFileGeometryExtractor {

  public TestFileGeometryExtractor() {
  }
  public static void main(String[] args) throws Exception {
    TestReader testReader = new TestReader();
    TestRun testRun = testReader.createTestRun(new File("c:\\blah\\isvalid.xml"), 0);
    ArrayList geometries = new ArrayList();
    for (Iterator i = testRun.getTestCases().iterator(); i.hasNext(); ) {
      TestCase testCase = (TestCase) i.next();
      add(testCase.getGeometryA(), geometries);
      add(testCase.getGeometryB(), geometries);
    }
    String run = "";
    int j = 0;
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry geometry = (Geometry) i.next();
      j++;
      run += "<case>" + StringUtil.newLine;
      run += "  <desc>Test " + j + "</desc>" + StringUtil.newLine;
      run += "  <a>" + StringUtil.newLine;
      run += "    " + geometry + StringUtil.newLine;
      run += "  </a>" + StringUtil.newLine;
      run += "  <test> <op name=\"isValid\" arg1=\"A\"> true </op> </test>" + StringUtil.newLine;
      run += "</case>" + StringUtil.newLine;
    }
    FileUtil.setContents("c:\\blah\\isvalid2.xml", run);
  }

  private static void add(Geometry geometry, ArrayList geometries) {
    if (geometry == null) { return; }
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry existingGeometry = (Geometry) i.next();
      if (geometry.equalsExact(existingGeometry)) { return; }
    }
    geometries.add(geometry);
  }
}
