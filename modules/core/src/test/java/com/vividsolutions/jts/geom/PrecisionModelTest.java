
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

package com.vividsolutions.jts.geom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @version 1.7
 */
public class PrecisionModelTest extends TestCase
{

  public PrecisionModelTest(String name) {
      super(name);
  }

  public static Test suite() {
    return new TestSuite(PrecisionModelTest.class);
  }

  public void testParameterlessConstructor() {
    PrecisionModel p = new PrecisionModel();
    //Implicit precision model has scale 0
    assertEquals(0, p.getScale(), 1E-10);
  }
  
  public void testGetMaximumSignificantDigits() {
   assertEquals(16, new PrecisionModel(PrecisionModel.FLOATING).getMaximumSignificantDigits());
   assertEquals(6, new PrecisionModel(PrecisionModel.FLOATING_SINGLE).getMaximumSignificantDigits());
   assertEquals(1, new PrecisionModel(PrecisionModel.FIXED).getMaximumSignificantDigits());
   assertEquals(4, new PrecisionModel(1000).getMaximumSignificantDigits());
  }

  public void testMakePrecise()
  {
  	PrecisionModel pm_10 = new PrecisionModel(0.1);
  	
  	preciseCoordinateTester(pm_10, 1200.4, 1240.4, 1200, 1240);
  	preciseCoordinateTester(pm_10, 1209.4, 1240.4, 1210, 1240);
  }
  
  private void preciseCoordinateTester(PrecisionModel pm, 
  		double x1, double y1, 
  		double x2, double y2)
  {
  	Coordinate p = new Coordinate(x1, y1);
  	
  	pm.makePrecise(p);
  	
  	Coordinate pPrecise = new Coordinate(x2, y2);
  	assertTrue(p.equals2D(pPrecise));
  }
  
}
