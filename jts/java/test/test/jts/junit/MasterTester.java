
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

package test.jts.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.jts.junit.geom.*;
import test.jts.junit.geom.impl.BasicCoordinateSequenceTest;
import test.jts.junit.algorithm.*;
import test.jts.junit.linearref.LengthIndexedLineTestCase;
import test.jts.junit.linearref.LocationIndexedLineTestCase;
import test.jts.junit.operation.buffer.*;
import test.jts.junit.operation.distance.DistanceTest;
import test.jts.junit.operation.linemerge.LineMergerTest;
import test.jts.junit.operation.polygonize.PolygonizeTest;
import test.jts.junit.operation.relate.*;
import test.jts.junit.operation.union.CascadedPolygonUnionTest;
import test.jts.junit.operation.union.UnaryUnionTest;
import test.jts.junit.operation.valid.*;
import test.jts.junit.precision.SimpleGeometryPrecisionReducerTest;
import test.jts.junit.index.IntervalTest;
import test.jts.junit.index.QuadtreeTestCase;
import test.jts.junit.index.SIRtreeTestCase;
import test.jts.junit.index.STRtreeTestCase;
import test.jts.junit.io.*;
/**
 * A collection of all the tests.
 *
 * @version 1.7
 */
public class MasterTester extends TestCase {

  public MasterTester(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTest(new TestSuite(AngleTest.class));
    result.addTest(new TestSuite(AreaLengthTest.class));
    result.addTest(new TestSuite(BasicCoordinateSequenceTest.class));
    result.addTest(new TestSuite(BidirectionalComparatorTest.class));
    result.addTest(new TestSuite(BufferTest.class));
    result.addTest(new TestSuite(ConvexHullTest.class));
    result.addTest(new TestSuite(CoordinateArraysTest.class));
    result.addTest(new TestSuite(DistanceTest.class));
    result.addTest(new TestSuite(EnvelopeTest.class));
    result.addTest(new TestSuite(GeometryCollectionImplTest.class));
    result.addTest(new TestSuite(GeometryImplTest.class));
    result.addTest(new TestSuite(IntersectionMatrixTest.class));
    result.addTest(new TestSuite(IntervalTest.class));
    result.addTest(new TestSuite(IsRectangleTest.class));
    result.addTest(new TestSuite(IsValidTest.class));
    result.addTest(new TestSuite(LineMergerTest.class));
    result.addTest(new TestSuite(LineStringImplTest.class));
    result.addTest(new TestSuite(MiscellaneousTest.class));
    result.addTest(new TestSuite(MiscellaneousTest2.class));
    result.addTest(new TestSuite(MultiPointImplTest.class));
    result.addTest(new TestSuite(NonRobustLineIntersectorTest.class));
    result.addTest(new TestSuite(NormalizeTest.class));
    result.addTest(new TestSuite(PointImplTest.class));
    result.addTest(new TestSuite(PolygonizeTest.class));
    result.addTest(new TestSuite(PredicateShortCircuitTest.class));
    result.addTest(new TestSuite(PrecisionModelTest.class));
    result.addTest(new TestSuite(QuadtreeTestCase.class));
    result.addTest(new TestSuite(RectanglePredicateSyntheticTest.class));
    result.addTest(new TestSuite(RectanglePredicateTest.class));
    result.addTest(new TestSuite(RelateBoundaryNodeRuleTest.class));
    result.addTest(new TestSuite(RobustLineIntersectorTest.class));
    result.addTest(new TestSuite(SimpleGeometryPrecisionReducerTest.class));
    result.addTest(new TestSuite(SimpleTest.class));
    result.addTest(new TestSuite(SIRtreeTestCase.class));
    result.addTest(new TestSuite(STRtreeTestCase.class));
    result.addTest(new TestSuite(WKTReaderTest.class));
    result.addTest(new TestSuite(WKTWriterTest.class));
    result.addTest(new TestSuite(WKBTest.class));
    result.addTest(new TestSuite(ValidClosedRingTest.class));
    result.addTest(new TestSuite(ValidSelfTouchingRingFormingHoleTest.class));

    result.addTest(new TestSuite(PredicateShortCircuitTest.class));
    result.addTest(new TestSuite(IsRectangleTest.class));

    result.addTest(new TestSuite(ComputeOrientationTest.class));
    result.addTest(new TestSuite(IsCCWTest.class));
    
    result.addTest(new TestSuite(UnaryUnionTest.class));
    result.addTest(new TestSuite(CascadedPolygonUnionTest.class));

    result.addTest(new TestSuite(LengthIndexedLineTestCase.class));
    result.addTest(new TestSuite(LocationIndexedLineTestCase.class));


    return result;
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
  }

}
