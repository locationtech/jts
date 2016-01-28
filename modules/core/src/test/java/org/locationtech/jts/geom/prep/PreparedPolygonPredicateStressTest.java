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
package org.locationtech.jts.geom.prep;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Stress tests {@link PreparedPolygon} for 
 * correctness of 
 * {@link PreparedPolygon#contains(Geometry)}
 * and {@link PreparedPolygon#intersects(Geometry)}
 * operations.
 * 
 * @author Owner
 *
 */
public class PreparedPolygonPredicateStressTest extends TestCase
{

  public static void main(String args[]) {
    TestRunner.run(PreparedPolygonPredicateStressTest.class);
  }

  boolean testFailed = false;

  public PreparedPolygonPredicateStressTest(String name) {
    super(name);
  }

  public void test()
  {
  	PredicateStressTester tester = new PredicateStressTester();
  	tester.run(1000);
  }
  
  class PredicateStressTester 
  extends StressTestHarness
  {
  	public boolean checkResult(Geometry target, Geometry test) {
  		if (! checkIntersects(target, test)) return false;
  		if (! checkContains(target, test)) return false;
  		return true;
  	}
  }
  
  public boolean checkContains(Geometry target, Geometry test) 
  {
	boolean expectedResult = target.contains(test);

    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
    PreparedGeometry prepGeom = pgFact.create(target);
    
	boolean prepResult = prepGeom.contains(test);

	if (prepResult != expectedResult) {
		return false;
	}
	return true;
  } 

  public boolean checkIntersects(Geometry target, Geometry test) 
  {
	boolean expectedResult = target.intersects(test);
	
	PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
	PreparedGeometry prepGeom = pgFact.create(target);
	
	boolean prepResult = prepGeom.intersects(test);
	
	if (prepResult != expectedResult) {
		return false;
	}
	return true;
  } 

}
