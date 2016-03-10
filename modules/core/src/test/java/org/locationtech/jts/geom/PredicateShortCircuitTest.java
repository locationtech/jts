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
package org.locationtech.jts.geom;

import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test named predicate short-circuits
 */
/**
 * @version 1.7
 */
public class PredicateShortCircuitTest extends TestCase {

    WKTReader rdr = new WKTReader();

    public static void main(String args[]) {
      TestRunner.run(PredicateShortCircuitTest.class);
    }

    public PredicateShortCircuitTest(String name) { super(name); }

    String[] polyInsidePoly =
    { "POLYGON (( 0 0, 100 0, 100 100, 0 100, 0 0 ))",
      "POLYGON (( 10 10, 90 10, 90 90, 10 90, 10 10 ))" } ;
    String[] polyPartiallyOverlapsPoly =
    { "POLYGON (( 10 10, 100 10, 100 100, 10 100, 10 10 ))",
      "POLYGON (( 0 0, 90 0, 90 90, 0 90, 0 0 ))" } ;
    String[] polyTouchesPolyAtPoint =
    { "POLYGON (( 10 10, 100 10, 100 100, 10 100, 10 10 ))",
      "POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0 ))" } ;
    String[] polyTouchesPolyAtLine =
    { "POLYGON (( 10 10, 100 10, 100 100, 10 100, 10 10 ))",
      "POLYGON (( 10 0, 10 10, 20 10, 20 0, 10 0 ))" } ;
    String[] polyInsideHoleInPoly =
    { "POLYGON (( 40 40, 40 60, 60 60, 60 40, 40 40 ))",
      "POLYGON (( 0 0, 100 0, 100 100, 0 100, 0 0), ( 10 10, 90 10, 90 90, 10 90, 10 10))" } ;


    public void testAll() throws Exception
    {
      doPredicates(polyInsidePoly);
      doPredicates(polyPartiallyOverlapsPoly);
      doPredicates(polyTouchesPolyAtPoint);
      doPredicates(polyTouchesPolyAtLine);
      doPredicates(polyInsideHoleInPoly);
    }

    public void doPredicates(String[] wkt)
                    throws Exception
    {
        Geometry a = rdr.read(wkt[0]);
        Geometry b = rdr.read(wkt[1]);
        doPredicates(a, b);
        doPredicates(b ,a);
    }
    public void doPredicates(Geometry a, Geometry b) throws Exception
    {
      assertTrue( a.contains(b) == a.relate(b).isContains() );
      assertTrue( a.crosses(b) == a.relate(b).isCrosses(a.getDimension(), b.getDimension()) );
      assertTrue( a.disjoint(b) == a.relate(b).isDisjoint() );
      assertTrue( a.equals(b) == a.relate(b).isEquals(a.getDimension(), b.getDimension()) );
      assertTrue( a.intersects(b) == a.relate(b).isIntersects() );
      assertTrue( a.overlaps(b) == a.relate(b).isOverlaps(a.getDimension(), b.getDimension()) );
      assertTrue( a.touches(b) == a.relate(b).isTouches(a.getDimension(), b.getDimension()) );
      assertTrue( a.within(b) == a.relate(b).isWithin() );
    }



}