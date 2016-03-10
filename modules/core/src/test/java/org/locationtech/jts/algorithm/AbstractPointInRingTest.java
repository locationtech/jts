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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;

import junit.framework.TestCase;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public abstract class AbstractPointInRingTest extends TestCase {


  public AbstractPointInRingTest(String name) { super(name); }

  public void testBox() throws Exception
  {
    runPtInRing(Location.INTERIOR, new Coordinate(10, 10),
"POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))");
  }

  public void testComplexRing() throws Exception
  {
    runPtInRing(Location.INTERIOR, new Coordinate(0, 0),
"POLYGON ((-40 80, -40 -80, 20 0, 20 -100, 40 40, 80 -80, 100 80, 140 -20, 120 140, 40 180,     60 40, 0 120, -20 -20, -40 80))");
  }

  public static final String comb = 
  	"POLYGON ((0 0, 0 10, 4 5, 6 10, 7 5, 9 10, 10 5, 13 5, 15 10, 16 3, 17 10, 18 3, 25 10, 30 10, 30 0, 15 0, 14 5, 13 0, 9 0, 8 5, 6 0, 0 0))";
  
  public void testComb() throws Exception
  {
    runPtInRing(Location.BOUNDARY, new Coordinate(0, 0), comb);
    runPtInRing(Location.BOUNDARY, new Coordinate(0, 1), comb);
    // at vertex 
    runPtInRing(Location.BOUNDARY, new Coordinate(4, 5), comb);
    runPtInRing(Location.BOUNDARY, new Coordinate(8, 5), comb);
    
    // on horizontal segment
    runPtInRing(Location.BOUNDARY, new Coordinate(11, 5), comb);
    // on vertical segment
    runPtInRing(Location.BOUNDARY, new Coordinate(30, 5), comb);
    // on angled segment
    runPtInRing(Location.BOUNDARY, new Coordinate(22, 7), comb);
    
    
    
    runPtInRing(Location.INTERIOR, new Coordinate(1, 5), comb);
    runPtInRing(Location.INTERIOR, new Coordinate(5, 5), comb);
    runPtInRing(Location.INTERIOR, new Coordinate(1, 7), comb);
    
    
    
    runPtInRing(Location.EXTERIOR, new Coordinate(12, 10), comb);
    runPtInRing(Location.EXTERIOR, new Coordinate(16, 5), comb);
    runPtInRing(Location.EXTERIOR, new Coordinate(35, 5), comb);
  }

  public static final String repeatedPts = 
  	"POLYGON ((0 0, 0 10, 2 5, 2 5, 2 5, 2 5, 2 5, 3 10, 6 10, 8 5, 8 5, 8 5, 8 5, 10 10, 10 5, 10 5, 10 5, 10 5, 10 0, 0 0))";

  /**
   * Tests that repeated points are handled correctly
   * @throws Exception
   */
  public void testRepeatedPts() throws Exception
  {
    runPtInRing(Location.BOUNDARY, new Coordinate(0, 0), repeatedPts);
    runPtInRing(Location.BOUNDARY, new Coordinate(0, 1), repeatedPts);
    
    // at vertex 
    runPtInRing(Location.BOUNDARY, new Coordinate(2, 5), repeatedPts);
    runPtInRing(Location.BOUNDARY, new Coordinate(8, 5), repeatedPts);
    runPtInRing(Location.BOUNDARY, new Coordinate(10, 5), repeatedPts);

    runPtInRing(Location.INTERIOR, new Coordinate(1, 5), repeatedPts);
    runPtInRing(Location.INTERIOR, new Coordinate(3, 5), repeatedPts);

  }
  
  abstract protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt)
      throws Exception;

}