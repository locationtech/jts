/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testrunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBHexFileReader;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;

public class TestRunBuilder 
{
  private static GeometryFactory geomFact = new GeometryFactory();
  
  private Geometry a = null;
  private Geometry b = null;
  private String description = "";
  private String operation  = "no-op";
  private List<String> args = new ArrayList<String>();
  private File aFile = null;
  private File bFile = null;
  
  public TestRunBuilder() {
    
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public void setOperation(String operation) {
    this.operation  = operation;
  }
  
  public void setArguments(List<String> arguments) {
    args = arguments;
  }  
  public void readGeometryAFromFile(String filename) throws IOException, ParseException {
    aFile = new File(filename);
    a = readFile(filename);
  }
  
  public void readGeometryBFromFile(String filename) throws IOException, ParseException {
    bFile = new File(filename);
    b = readFile(filename);
  }
  
  public TestRun build() {
    TestRun testRun = new TestRun(description, 0, null, null, null, null);
    
    TestCase testCase = new TestCase(description, a, b, aFile, bFile, testRun, 0, 0);
    
    //String description = "Cmd-line test";
    //String operation = "op";
    String geomIndex = "A";
    
    Test test = new Test(testCase, 0, description, operation, geomIndex, args, null, 0);
    testCase.add(test);
    testRun.addTestCase(testCase);
    return testRun;
  }

  
  private Geometry readFile(String filename) throws IOException, ParseException {
    if (filename.toLowerCase().endsWith(".wkt")) {
      return readWKTFile(filename);
    }
    if (filename.toLowerCase().endsWith(".wkb")) {
      return readWKBFile(filename);
    }
    throw new IllegalArgumentException("unrecognized file type: " + filename);
  }

  private Geometry readWKTFile(String filename) throws IOException, ParseException {
    WKTReader wktReader = new WKTReader();
    WKTFileReader wktFileReader = new WKTFileReader(filename, wktReader);
    List<Geometry> geoms = wktFileReader.read();
    return createGeometry(geoms);
  }

  private Geometry readWKBFile(String filename) throws IOException, ParseException {
    WKBReader wkbReader = new WKBReader();
    WKBHexFileReader wkbFileReader = new WKBHexFileReader(filename, wkbReader);
    List geoms = wkbFileReader.read();
    return createGeometry(geoms);
  }
  
  private Geometry createGeometry(List<Geometry> geoms) {
    if (geoms.size() == 0) {
      return null;
    }
    else if (geoms.size() == 1) {
      return geoms.get(0);
    }
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
  }





}
