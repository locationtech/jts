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
package test.jts.junit.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.*;

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