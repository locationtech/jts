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
package org.locationtech.jts.triangulate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;

import junit.framework.TestCase;
import junit.textui.TestRunner;

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
  	//System.out.println(result);
  	
  	Geometry expectedEdges = reader.read(expectedWKT);
  	result.normalize();
  	expectedEdges.normalize();
  	assertTrue(expectedEdges.equalsExact(result, COMPARISON_TOLERANCE));
  }
}