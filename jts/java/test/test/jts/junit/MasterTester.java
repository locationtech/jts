
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
import test.jts.junit.algorithm.AngleTest;
import test.jts.junit.algorithm.ConvexHullTest;
import test.jts.junit.algorithm.IsCCWTest;
import test.jts.junit.algorithm.NonRobustLineIntersectorTest;
import test.jts.junit.algorithm.OrientationIndexTest;
import test.jts.junit.algorithm.RobustLineIntersectorTest;
import test.jts.junit.geom.AreaLengthTest;
import test.jts.junit.geom.BidirectionalComparatorTest;
import test.jts.junit.geom.CoordinateArraysTest;
import test.jts.junit.geom.EnvelopeTest;
import test.jts.junit.geom.GeometryCollectionImplTest;
import test.jts.junit.geom.GeometryImplTest;
import test.jts.junit.geom.IntersectionMatrixTest;
import test.jts.junit.geom.IsRectangleTest;
import test.jts.junit.geom.LineStringImplTest;
import test.jts.junit.geom.MultiPointImplTest;
import test.jts.junit.geom.NormalizeTest;
import test.jts.junit.geom.PointImplTest;
import test.jts.junit.geom.PrecisionModelTest;
import test.jts.junit.geom.PredicateShortCircuitTest;
import test.jts.junit.geom.RectanglePredicateSyntheticTest;
import test.jts.junit.geom.RectanglePredicateTest;
import test.jts.junit.geom.impl.BasicCoordinateSequenceTest;
import test.jts.junit.index.IntervalTest;
import test.jts.junit.index.QuadtreeTest;
import test.jts.junit.index.SIRtreeTest;
import test.jts.junit.index.STRtreeTest;
import test.jts.junit.io.WKBTest;
import test.jts.junit.io.WKTReaderTest;
import test.jts.junit.io.WKTWriterTest;
import test.jts.junit.linearref.LengthIndexedLineTest;
import test.jts.junit.linearref.LocationIndexedLineTest;
import test.jts.junit.operation.buffer.BufferTest;
import test.jts.junit.operation.distance.DistanceTest;
import test.jts.junit.operation.linemerge.LineMergerTest;
import test.jts.junit.operation.polygonize.PolygonizeTest;
import test.jts.junit.operation.relate.RelateBoundaryNodeRuleTest;
import test.jts.junit.operation.union.CascadedPolygonUnionTest;
import test.jts.junit.operation.union.UnaryUnionTest;
import test.jts.junit.operation.valid.IsValidTest;
import test.jts.junit.operation.valid.ValidClosedRingTest;
import test.jts.junit.operation.valid.ValidSelfTouchingRingFormingHoleTest;
import test.jts.junit.precision.SimpleGeometryPrecisionReducerTest;

import com.vividsolutions.jts.triangulate.ConformingDelaunayTest;
import com.vividsolutions.jts.triangulate.DelaunayTest;
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
    result.addTest(new TestSuite(CascadedPolygonUnionTest.class));
    result.addTest(new TestSuite(OrientationIndexTest.class));
    result.addTest(new TestSuite(ConformingDelaunayTest.class));
    result.addTest(new TestSuite(ConvexHullTest.class));
    result.addTest(new TestSuite(CoordinateArraysTest.class));
    result.addTest(new TestSuite(DelaunayTest.class));
    result.addTest(new TestSuite(DistanceTest.class));
    result.addTest(new TestSuite(EnvelopeTest.class));
    result.addTest(new TestSuite(GeometryCollectionImplTest.class));
    result.addTest(new TestSuite(GeometryImplTest.class));
    result.addTest(new TestSuite(IntersectionMatrixTest.class));
    result.addTest(new TestSuite(IntervalTest.class));
    result.addTest(new TestSuite(IsCCWTest.class));
    result.addTest(new TestSuite(IsRectangleTest.class));
    result.addTest(new TestSuite(IsValidTest.class));
    result.addTest(new TestSuite(LengthIndexedLineTest.class));
    result.addTest(new TestSuite(LineMergerTest.class));
    result.addTest(new TestSuite(LineStringImplTest.class));
    result.addTest(new TestSuite(LocationIndexedLineTest.class));
    result.addTest(new TestSuite(MiscellaneousTest.class));
    result.addTest(new TestSuite(MiscellaneousTest2.class));
    result.addTest(new TestSuite(MultiPointImplTest.class));
    result.addTest(new TestSuite(NonRobustLineIntersectorTest.class));
    result.addTest(new TestSuite(NormalizeTest.class));
    result.addTest(new TestSuite(PointImplTest.class));
    result.addTest(new TestSuite(PolygonizeTest.class));
    result.addTest(new TestSuite(PredicateShortCircuitTest.class));
    result.addTest(new TestSuite(PrecisionModelTest.class));
    result.addTest(new TestSuite(QuadtreeTest.class));
    result.addTest(new TestSuite(RectanglePredicateSyntheticTest.class));
    result.addTest(new TestSuite(RectanglePredicateTest.class));
    result.addTest(new TestSuite(RelateBoundaryNodeRuleTest.class));
    result.addTest(new TestSuite(RobustLineIntersectorTest.class));
    result.addTest(new TestSuite(SimpleGeometryPrecisionReducerTest.class));
    result.addTest(new TestSuite(SimpleTest.class));
    result.addTest(new TestSuite(SIRtreeTest.class));
    result.addTest(new TestSuite(STRtreeTest.class));
    result.addTest(new TestSuite(WKTReaderTest.class));
    result.addTest(new TestSuite(WKTWriterTest.class));
    result.addTest(new TestSuite(WKBTest.class));
    result.addTest(new TestSuite(UnaryUnionTest.class));
    result.addTest(new TestSuite(ValidClosedRingTest.class));
    result.addTest(new TestSuite(ValidSelfTouchingRingFormingHoleTest.class));
    //result.addTest(new TestSuite(VoronoiTest.class));
    
    return result;
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
  }

}
