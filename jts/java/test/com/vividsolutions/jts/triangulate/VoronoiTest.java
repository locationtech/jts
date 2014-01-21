/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
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