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

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests Delaunay Triangulatin classes
 * 
 */
public class ConformingDelaunayTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(ConformingDelaunayTest.class);
  }

  public ConformingDelaunayTest(String name) { super(name); }

  public void testRandom()
  throws ParseException
  {
  	String wkt = "MULTIPOINT ((90 290), (120 250), (280 250), (200 200), (220 290), (170 320), (110 180), (70 140), (180 110), (210 80), (250 120))";
  	String lineWKT = "MULTILINESTRING ((130 160, 150 280, 200 250), (180 120, 240 230), (40 270, 90 220), (90 270, 130 290, 100 280, 140 310))";

  	String expected = "MULTILINESTRING ((220 290, 280 250), (170 320, 220 290), (170 320, 140 310), (90 290, 140 310), (90 290, 40 270), (70 140, 40 270), (70 140, 210 80), (210 80, 250 120), (280 250, 250 120), (250 120, 240 230), (280 250, 240 230), (240 230, 220 290), (200 250, 240 230), (220 290, 200 250), (150 280, 200 250), (220 290, 150 280), (170 320, 150 280), (140 310, 150 280), (130 290, 150 280), (130 290, 140 310), (130 290, 124 298), (140 310, 124 298), (90 290, 124 298), (100 280, 124 298), (90 290, 100 280), (90 270, 100 280), (90 290, 90 270), (90 270, 40 270), (90 220, 90 270), (40 270, 90 220), (90 220, 70 140), (110 180, 90 220), (70 140, 110 180), (110 180, 130 160), (70 140, 130 160), (180 110, 130 160), (70 140, 180 110), (180 110, 210 80), (180 110, 250 120), (180 110, 180 120), (250 120, 180 120), (180 120, 218.21656050955414 190.06369426751593), (250 120, 218.21656050955414 190.06369426751593), (240 230, 218.21656050955414 190.06369426751593), (200 200, 218.21656050955414 190.06369426751593), (200 200, 240 230), (200 200, 200 250), (200 200, 143.51351351351352 241.0810810810811), (200 250, 143.51351351351352 241.0810810810811), (150 280, 143.51351351351352 241.0810810810811), (120 250, 143.51351351351352 241.0810810810811), (120 250, 150 280), (120 250, 130 290), (120 250, 102 276), (130 290, 102 276), (100 280, 102 276), (100 280, 130 290), (90 270, 102 276), (120 250, 90 270), (120 250, 90 220), (120 250, 136.05405405405406 196.32432432432432), (136.05405405405406 196.32432432432432, 90 220), (136.05405405405406 196.32432432432432, 110 180), (130 160, 136.05405405405406 196.32432432432432), (200 200, 136.05405405405406 196.32432432432432), (130 160, 200 200), (200 200, 180 120), (130 160, 180 120), (143.51351351351352 241.0810810810811, 136.05405405405406 196.32432432432432))";
  	runDelaunay(wkt, lineWKT, false, expected);
  	
  	String expectedTri = "GEOMETRYCOLLECTION (POLYGON ((40 270, 70 140, 90 220, 40 270)), POLYGON ((40 270, 90 220, 90 270, 40 270)), POLYGON ((40 270, 90 270, 90 290, 40 270)), POLYGON ((90 290, 90 270, 100 280, 90 290)), POLYGON ((90 290, 100 280, 124 298, 90 290)), POLYGON ((90 290, 124 298, 140 310, 90 290)), POLYGON ((140 310, 124 298, 130 290, 140 310)), POLYGON ((140 310, 130 290, 150 280, 140 310)), POLYGON ((140 310, 150 280, 170 320, 140 310)), POLYGON ((170 320, 150 280, 220 290, 170 320)), POLYGON ((220 290, 150 280, 200 250, 220 290)), POLYGON ((220 290, 200 250, 240 230, 220 290)), POLYGON ((220 290, 240 230, 280 250, 220 290)), POLYGON ((280 250, 240 230, 250 120, 280 250)), POLYGON ((210 80, 250 120, 180 110, 210 80)), POLYGON ((210 80, 180 110, 70 140, 210 80)), POLYGON ((70 140, 180 110, 130 160, 70 140)), POLYGON ((70 140, 130 160, 110 180, 70 140)), POLYGON ((70 140, 110 180, 90 220, 70 140)), POLYGON ((90 220, 110 180, 136.05405405405406 196.32432432432432, 90 220)), POLYGON ((90 220, 136.05405405405406 196.32432432432432, 120 250, 90 220)), POLYGON ((90 220, 120 250, 90 270, 90 220)), POLYGON ((90 270, 120 250, 102 276, 90 270)), POLYGON ((90 270, 102 276, 100 280, 90 270)), POLYGON ((100 280, 102 276, 130 290, 100 280)), POLYGON ((100 280, 130 290, 124 298, 100 280)), POLYGON ((130 290, 102 276, 120 250, 130 290)), POLYGON ((130 290, 120 250, 150 280, 130 290)), POLYGON ((150 280, 120 250, 143.51351351351352 241.0810810810811, 150 280)), POLYGON ((150 280, 143.51351351351352 241.0810810810811, 200 250, 150 280)), POLYGON ((200 250, 143.51351351351352 241.0810810810811, 200 200, 200 250)), POLYGON ((200 250, 200 200, 240 230, 200 250)), POLYGON ((240 230, 200 200, 218.21656050955414 190.06369426751593, 240 230)), POLYGON ((240 230, 218.21656050955414 190.06369426751593, 250 120, 240 230)), POLYGON ((250 120, 218.21656050955414 190.06369426751593, 180 120, 250 120)), POLYGON ((250 120, 180 120, 180 110, 250 120)), POLYGON ((180 110, 180 120, 130 160, 180 110)), POLYGON ((130 160, 180 120, 200 200, 130 160)), POLYGON ((130 160, 200 200, 136.05405405405406 196.32432432432432, 130 160)), POLYGON ((130 160, 136.05405405405406 196.32432432432432, 110 180, 130 160)), POLYGON ((136.05405405405406 196.32432432432432, 200 200, 143.51351351351352 241.0810810810811, 136.05405405405406 196.32432432432432)), POLYGON ((136.05405405405406 196.32432432432432, 143.51351351351352 241.0810810810811, 120 250, 136.05405405405406 196.32432432432432)), POLYGON ((200 200, 180 120, 218.21656050955414 190.06369426751593, 200 200)))";
  	runDelaunay(wkt, lineWKT, true, expectedTri);
  }
  
	static final double COMPARISON_TOLERANCE = 1.0e-7;
	
  void runDelaunay(String sitesWKT, String constraintsWKT, boolean computeTriangles, String expectedWKT)
  throws ParseException
  {
  	Geometry sites = reader.read(sitesWKT);
  	Geometry constraints = reader.read(constraintsWKT);
  	
  	ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
  	builder.setSites(sites);
  	builder.setConstraints(constraints);
  	GeometryFactory geomFact = new GeometryFactory();
  	
  	Geometry result = null;
  	if (computeTriangles) {
  		result = builder.getTriangles(geomFact);  		
  	}
  	else {
  		result = builder.getEdges(geomFact);
  	}
  	//System.out.println(result);
  	
  	Geometry expectedEdges = reader.read(expectedWKT);
  	result.normalize();
  	expectedEdges.normalize();
  	assertTrue(expectedEdges.equalsExact(result, COMPARISON_TOLERANCE));
  }
}