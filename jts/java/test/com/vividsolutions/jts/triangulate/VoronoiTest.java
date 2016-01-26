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
package com.vividsolutions.jts.triangulate;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.quadedge.*;

/**
 * Tests Voronoi diagram generation
 * 
 */
public class VoronoiTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(VoronoiTest.class);
  }

  public VoronoiTest(String name) { super(name); }

  public void testSimple()
  throws ParseException
  {
    String wkt = "MULTIPOINT ((10 10), (20 70), (60 30), (80 70))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((-1162.076359832636 462.66344142259413, 50 419.375, 50 60, 27.857142857142854 37.857142857142854, -867 187, -1162.076359832636 462.66344142259413)), POLYGON ((-867 187, 27.857142857142854 37.857142857142854, 245 -505, 45 -725, -867 187)), POLYGON ((27.857142857142854 37.857142857142854, 50 60, 556.6666666666666 -193.33333333333331, 245 -505, 27.857142857142854 37.857142857142854)), POLYGON ((50 60, 50 419.375, 1289.1616314199396 481.3330815709969, 556.6666666666666 -193.33333333333331, 50 60)))";
    runVoronoi(wkt, true, expected);
  }
    
	static final double COMPARISON_TOLERANCE = 1.0e-7;
	
  void runVoronoi(String sitesWKT, boolean computeTriangles, String expectedWKT)
  throws ParseException
  {
  	Geometry sites = reader.read(sitesWKT);
  	DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
  	builder.setSites(sites);
  	
    QuadEdgeSubdivision subdiv = builder.getSubdivision();
    
  	GeometryFactory geomFact = new GeometryFactory();
  	Geometry result = null;
  	if (computeTriangles) {
  		result = subdiv.getVoronoiDiagram(geomFact);	
  	}
  	else {
  		//result = builder.getEdges(geomFact);
  	}
  	System.out.println(result);
  	
  	Geometry expectedEdges = reader.read(expectedWKT);
  	result.normalize();
  	expectedEdges.normalize();
  	assertTrue(expectedEdges.equalsExact(result, COMPARISON_TOLERANCE));
  }
}