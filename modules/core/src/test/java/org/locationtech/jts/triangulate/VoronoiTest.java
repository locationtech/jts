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
package org.locationtech.jts.triangulate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.math.DD;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests Voronoi diagram generation
 * 
 */
public class VoronoiTest extends GeometryTestCase {

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
  
  /** 
   * Test case taken from GEOS issue 976: https://trac.osgeo.org/geos/ticket/976
   * 
   * Running with original Triangle.circumcentre double-precision code caused
   * invalid polygons to be computed, due to different circumcentres being 
   * computed for adjacent triangles for sites in a square.
   * Switching to {@link DD} solved the problem by computing
   * identical circumcentres.
   */
  public void testSitesWithPointsOnSquareGrid() {
    String wkt = "0104000080170000000101000080EC51B81E11A20741EC51B81E85A51C415C8FC2F528DC354001010000801F85EB5114A207415C8FC2F585A51C417B14AE47E1BA3540010100008085EB51B818A20741A8C64B3786A51C413E0AD7A3709D35400101000080000000001BA20741FED478E984A51C413E0AD7A3709D3540010100008085EB51B818A20741FED478E984A51C413E0AD7A3709D354001010000800AD7A37016A20741FED478E984A51C413E0AD7A3709D35400101000080000000001BA2074154E3A59B83A51C413E0AD7A3709D3540010100008085EB51B818A2074154E3A59B83A51C413E0AD7A3709D354001010000800AD7A37016A2074154E3A59B83A51C413E0AD7A3709D35400101000080000000001BA20741AAF1D24D82A51C413E0AD7A3709D3540010100008085EB51B818A20741AAF1D24D82A51C413E0AD7A3709D35400101000080F6285C8F12A20741EC51B81E88A51C414160E5D022DB354001010000802222222210A2074152B81EC586A51C414160E5D022DB354001010000804F1BE8B40DA2074152B81EC586A51C414160E5D022DB354001010000807B14AE470BA2074152B81EC586A51C414160E5D022DB354001010000802222222210A20741B81E856B85A51C414160E5D022DB354001010000804F1BE8B40DA20741B81E856B85A51C414160E5D022DB354001010000807B14AE470BA20741B81E856B85A51C414160E5D022DB35400101000080A70D74DA08A20741B81E856B85A51C414160E5D022DB35400101000080D4063A6D06A20741B81E856B85A51C414160E5D022DB354001010000807B14AE470BA207411F85EB1184A51C414160E5D022DB35400101000080A70D74DA08A207411F85EB1184A51C414160E5D022DB35400101000080D4063A6D06A207411F85EB1184A51C414160E5D022DB3540";
    runVoronoi(wkt);    
  }
  
  static final double COMPARISON_TOLERANCE = 1.0e-7;
	
	private void runVoronoi(String sitesWKT) {
	  runVoronoi(sitesWKT, true, null);
  }

  void runVoronoi(String sitesWKT, boolean computePolys, String expectedWKT)
  {
  	Geometry sites = read(sitesWKT);
  	DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
  	builder.setSites(sites);
  	
    QuadEdgeSubdivision subdiv = builder.getSubdivision();
    
  	GeometryFactory geomFact = new GeometryFactory();
  	Geometry result = null;
  	if (computePolys) {
  		result = subdiv.getVoronoiDiagram(geomFact);	
  	}
  	else {
  		//result = builder.getEdges(geomFact);
  	}
 	
  	assertTrue("Found invalid geometry(s) in Voronoi result", result.isValid() );
  	
  	if (expectedWKT == null) 
  	  return;
  	
  	Geometry expected = read(expectedWKT);
  	result.normalize();
  	expected.normalize();
  	assertTrue(expected.equalsExact(result, COMPARISON_TOLERANCE));
  }
}
