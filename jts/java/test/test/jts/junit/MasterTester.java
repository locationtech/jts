
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

import com.vividsolutions.jts.algorithm.RobustLineIntersectionTest;
import com.vividsolutions.jts.geom.AreaLengthTest;
import com.vividsolutions.jts.geom.BidirectionalComparatorTest;
import com.vividsolutions.jts.geom.CoordinateArraysTest;
import com.vividsolutions.jts.geom.EnvelopeTest;
import com.vividsolutions.jts.geom.GeometryCollectionImplTest;
import com.vividsolutions.jts.geom.GeometryImplTest;
import com.vividsolutions.jts.geom.IntersectionMatrixTest;
import com.vividsolutions.jts.geom.IsRectangleTest;
import com.vividsolutions.jts.geom.LineStringImplTest;
import com.vividsolutions.jts.geom.MultiPointImplTest;
import com.vividsolutions.jts.geom.NormalizeTest;
import com.vividsolutions.jts.geom.PointImplTest;
import com.vividsolutions.jts.geom.PrecisionModelTest;
import com.vividsolutions.jts.geom.PredicateShortCircuitTest;
import com.vividsolutions.jts.geom.RectanglePredicateSyntheticTest;
import com.vividsolutions.jts.geom.RectanglePredicateTest;
import com.vividsolutions.jts.geom.impl.BasicCoordinateSequenceTest;
import com.vividsolutions.jts.index.IntervalTest;
import com.vividsolutions.jts.index.QuadtreeTest;
import com.vividsolutions.jts.index.SIRtreeTest;
import com.vividsolutions.jts.index.STRtreeTest;
import com.vividsolutions.jts.io.WKBTest;
import com.vividsolutions.jts.io.WKTReaderTest;
import com.vividsolutions.jts.io.WKTWriterTest;
import com.vividsolutions.jts.linearref.LengthIndexedLineTest;
import com.vividsolutions.jts.linearref.LocationIndexedLineTest;
import com.vividsolutions.jts.operation.buffer.BufferTest;
import com.vividsolutions.jts.operation.distance.DistanceTest;
import com.vividsolutions.jts.operation.linemerge.LineMergerTest;
import com.vividsolutions.jts.operation.polygonize.PolygonizeTest;
import com.vividsolutions.jts.operation.relate.RelateBoundaryNodeRuleTest;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnionTest;
import com.vividsolutions.jts.operation.union.UnaryUnionTest;
import com.vividsolutions.jts.operation.valid.IsValidTest;
import com.vividsolutions.jts.operation.valid.ValidClosedRingTest;
import com.vividsolutions.jts.operation.valid.ValidSelfTouchingRingFormingHoleTest;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducerTest;
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
    result.addTest(new TestSuite(com.vividsolutions.jts.algorithm.AngleTest.class));
    result.addTest(new TestSuite(AreaLengthTest.class));
    result.addTest(new TestSuite(BasicCoordinateSequenceTest.class));
    result.addTest(new TestSuite(BidirectionalComparatorTest.class));
    result.addTest(new TestSuite(BufferTest.class));
    result.addTest(new TestSuite(CascadedPolygonUnionTest.class));
    result.addTest(new TestSuite(com.vividsolutions.jts.algorithm.OrientationIndexTest.class));
    result.addTest(new TestSuite(ConformingDelaunayTest.class));
    result.addTest(new TestSuite(com.vividsolutions.jts.algorithm.ConvexHullTest.class));
    result.addTest(new TestSuite(CoordinateArraysTest.class));
    result.addTest(new TestSuite(DelaunayTest.class));
    result.addTest(new TestSuite(DistanceTest.class));
    result.addTest(new TestSuite(EnvelopeTest.class));
    result.addTest(new TestSuite(GeometryCollectionImplTest.class));
    result.addTest(new TestSuite(GeometryImplTest.class));
    result.addTest(new TestSuite(IntersectionMatrixTest.class));
    result.addTest(new TestSuite(IntervalTest.class));
    result.addTest(new TestSuite(com.vividsolutions.jts.algorithm.IsCCWTest.class));
    result.addTest(new TestSuite(IsRectangleTest.class));
    result.addTest(new TestSuite(IsValidTest.class));
    result.addTest(new TestSuite(LengthIndexedLineTest.class));
    result.addTest(new TestSuite(LineMergerTest.class));
    result.addTest(new TestSuite(LineStringImplTest.class));
    result.addTest(new TestSuite(LocationIndexedLineTest.class));
    result.addTest(new TestSuite(MiscellaneousTest.class));
    result.addTest(new TestSuite(MiscellaneousTest2.class));
    result.addTest(new TestSuite(MultiPointImplTest.class));
    result.addTest(new TestSuite(com.vividsolutions.jts.algorithm.NonRobustLineIntersectorTest.class));
    result.addTest(new TestSuite(NormalizeTest.class));
    result.addTest(new TestSuite(PointImplTest.class));
    result.addTest(new TestSuite(PolygonizeTest.class));
    result.addTest(new TestSuite(PredicateShortCircuitTest.class));
    result.addTest(new TestSuite(PrecisionModelTest.class));
    result.addTest(new TestSuite(QuadtreeTest.class));
    result.addTest(new TestSuite(RectanglePredicateSyntheticTest.class));
    result.addTest(new TestSuite(RectanglePredicateTest.class));
    result.addTest(new TestSuite(RelateBoundaryNodeRuleTest.class));
    result.addTest(new TestSuite(RobustLineIntersectionTest.class));
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
