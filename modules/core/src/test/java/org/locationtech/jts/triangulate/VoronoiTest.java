/*
 * Copyright (c) 2026 Martin Davis.
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.math.DD;

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
    String expected = "GEOMETRYCOLLECTION (POLYGON ((-82.19544457292888 56.1992407621548, -82.19544457292888 162.19544457292886, 50 162.19544457292886, 50 60, 27.857142857142858 37.857142857142854, -82.19544457292888 56.1992407621548)), POLYGON ((-82.19544457292888 -82.19544457292888, -82.19544457292888 56.1992407621548, 27.857142857142858 37.857142857142854, 75.87817782917156 -82.19544457292888, -82.19544457292888 -82.19544457292888)), POLYGON ((172.19544457292886 -1.0977222864644354, 172.19544457292886 -82.19544457292888, 75.87817782917156 -82.19544457292888, 27.857142857142858 37.857142857142854, 50 60, 172.19544457292886 -1.0977222864644354)), POLYGON ((50 162.19544457292886, 172.19544457292886 162.19544457292886, 172.19544457292886 -1.0977222864644354, 50 60, 50 162.19544457292886)))";
    checkVoronoi(wkt, expected);
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
    checkVoronoiValid(wkt);    
  }
  
  /**
   * This test fails if the frame is forced to be convex via {@link IncrementalDelaunayTriangulator#forceConvex(boolean)}.
   * It is also dependent on the frame size factor - a value of 10 causes failure, 
   * but larger values may not.
   */
  public void testFrameDisableForceConvex() {
    String wkt = "MULTIPOINT ((259 289), (46 194), (396 359), (243 349), (206 99), (470 40), (429 185), (54 9), (78 208), (457 406), (355 191), (346 497), (144 79), (35 459), (322 37), (181 371), (359 257), (57 331), (225 139), (475 245), (416 364), (155 477), (123 232), (102 141), (251 434))";
    checkVoronoiValid(wkt);    
  }
  
  //-- see https://github.com/libgeos/geos/issues/1040
  public void testCocircularSitesGEOS1040() {
    checkVoronoiValid("MULTIPOINT ((6.6584 53.583000000000006), (6.6576 53.583600000000004), (6.657 53.5848), (6.6572000000000005 53.5842))");
  }
  
  //-- see https://github.com/libgeos/geos/issues/955
  public void testCocircularSitesGEOS955() {
    checkVoronoiValid("MULTIPOINT ((18.68285714285716 100.105), (16.046428571428578 100.105), (13.41 100.105), (13.41 102.46300000000001), (13.41 104.82100000000001), (13.41 107.179), (13.41 109.537), (13.41 111.89500000000001), (16.04642857142857 111.89500000000001), (18.682857142857145 111.89500000000001))");
  }
  
  /**
   * Sets of nearly cocircular sites generally produce invalid raw Voronoi diagrams.
   * They are fixed by the vertex snapping heuristic.
   */
  public void testCircles() {
    checkVoronoiValid(createCircularPoints( 10 ));
    checkVoronoiValid(createCircularPoints( 20 ));
    checkVoronoiValid(createCircularPoints( 100 ));
  }
  
  //====================================================

  static final double COMPARISON_TOLERANCE = 1.0e-7;

	
	private void checkVoronoi(String sitesWKT) {
	  checkVoronoi(sitesWKT, null);
  }

  void checkVoronoi(String wktSites, String expectedWKT)
  {
  	Geometry sites = read(wktSites);
  	Geometry result = computeVoronoi(sites); 
 	
  	assertTrue("Found invalid geometry(s) in Voronoi result", result.isValid() );
  	
  	if (expectedWKT == null) 
  	  return;
  	
  	Geometry expected = read(expectedWKT);
  	result.normalize();
  	expected.normalize();
  	checkEqual(expected, result, COMPARISON_TOLERANCE);
  }
  
  void checkVoronoiValid(String wktSites) {
    Geometry sites = read(wktSites);
    checkVoronoiValid(sites);
  }
    
  void checkVoronoiValid(Geometry sites) {

    Geometry result = null;
    try {
      result = computeVoronoi(sites); 
    }
    catch (TopologyException e) {
      fail("Voronoi result has invalid topology");
    }

    assertTrue(VoronoiChecker.isValid(result));
  }

  private static Geometry computeVoronoi(Geometry sites) {
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sites);
    Geometry result = builder.getDiagram(sites.getFactory());
    return result;
  }
  
  private Geometry createCircularPoints(int nPts)
  {
    double radius = 100;
    double angFrac = 2 * Math.PI / nPts;
    Coordinate[] pts = new Coordinate[nPts];
    for (int i = 0; i < nPts; i++) {
      double ang = i * angFrac;
      double x = radius + radius * Math.cos(ang);
      double y = radius + radius * Math.sin(ang);
      pts[i] = new Coordinate(x, y);
    }
    return getGeometryFactory().createMultiPointFromCoords(pts);
  }

  private Geometry createRandomCircularPoints(int nPts)
  {
    double radius = 100;
    Coordinate[] pts = new Coordinate[nPts];
    for (int i = 0; i < nPts; i++) {
      double ang = 2 * Math.PI * Math.random();
      double x = radius + radius * Math.cos(ang);
      double y = radius + radius * Math.sin(ang);
      pts[i] = new Coordinate(x, y);
    }
    return getGeometryFactory().createMultiPointFromCoords(pts);
  }
}
