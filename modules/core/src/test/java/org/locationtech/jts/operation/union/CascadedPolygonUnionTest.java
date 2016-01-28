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

import java.util.*;
import java.io.*;

import org.locationtech.jts.algorithm.match.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.operation.union.*;

import test.jts.TestFiles;
import test.jts.junit.*;

import junit.framework.TestCase;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using synthetic datasets.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTest extends TestCase 
{
	GeometryFactory geomFact = new GeometryFactory();
	
  public CascadedPolygonUnionTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionTest.class);
  }
  
  public void testBoxes()
  throws Exception
  {
  	runTest(GeometryUtils.readWKT(
  			new String[] {
  				"POLYGON ((80 260, 200 260, 200 30, 80 30, 80 260))",
  				"POLYGON ((30 180, 300 180, 300 110, 30 110, 30 180))",
  				"POLYGON ((30 280, 30 150, 140 150, 140 280, 30 280))"
  			}),
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs1()
  throws Exception
  {
  	Collection geoms = createDiscs(5, 0.7);
  	
  	System.out.println(geomFact.buildGeometry(geoms));
  	
  	runTest(geoms,
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  
  public void testDiscs2()
  throws Exception
  {
  	Collection geoms = createDiscs(5, 0.55);
  	
  	System.out.println(geomFact.buildGeometry(geoms));
  	
  	runTest(geoms,
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  
  // TODO: add some synthetic tests
  
  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();
  
  private void runTest(Collection geoms, double minimumMeasure) 
  {
  	assertTrue(tester.test(geoms, minimumMeasure));
  }
  
  private Collection createDiscs(int num, double radius)
  {
  	List geoms = new ArrayList();
  	for (int i = 0; i < num; i++) {
    	for (int j = 0; j < num; j++) {
    		Coordinate pt = new Coordinate(i, j);
    		Geometry ptGeom = geomFact.createPoint(pt);
    		Geometry disc = ptGeom.buffer(radius);
    		geoms.add(disc);
    	}
  	}
  	return geoms;
  }
}
