
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

package test.jts.junit;

import org.locationtech.jts.algorithm.RobustLineIntersectionTest;
import org.locationtech.jts.geom.AreaLengthTest;
import org.locationtech.jts.geom.BidirectionalComparatorTest;
import org.locationtech.jts.geom.CoordinateArraysTest;
import org.locationtech.jts.geom.EnvelopeTest;
import org.locationtech.jts.geom.GeometryCollectionImplTest;
import org.locationtech.jts.geom.GeometryImplTest;
import org.locationtech.jts.geom.IntersectionMatrixTest;
import org.locationtech.jts.geom.IsRectangleTest;
import org.locationtech.jts.geom.LineStringImplTest;
import org.locationtech.jts.geom.MultiPointImplTest;
import org.locationtech.jts.geom.NormalizeTest;
import org.locationtech.jts.geom.PointImplTest;
import org.locationtech.jts.geom.PrecisionModelTest;
import org.locationtech.jts.geom.PredicateShortCircuitTest;
import org.locationtech.jts.geom.RectanglePredicateSyntheticTest;
import org.locationtech.jts.geom.RectanglePredicateTest;
import org.locationtech.jts.geom.impl.BasicCoordinateSequenceTest;
import org.locationtech.jts.index.IntervalTest;
import org.locationtech.jts.index.QuadtreeTest;
import org.locationtech.jts.index.SIRtreeTest;
import org.locationtech.jts.index.STRtreeTest;
import org.locationtech.jts.io.WKBTest;
import org.locationtech.jts.io.WKTReaderTest;
import org.locationtech.jts.io.WKTWriterTest;
import org.locationtech.jts.linearref.LengthIndexedLineTest;
import org.locationtech.jts.linearref.LocationIndexedLineTest;
import org.locationtech.jts.operation.buffer.BufferTest;
import org.locationtech.jts.operation.distance.DistanceTest;
import org.locationtech.jts.operation.linemerge.LineMergerTest;
import org.locationtech.jts.operation.polygonize.PolygonizeTest;
import org.locationtech.jts.operation.relate.RelateBoundaryNodeRuleTest;
import org.locationtech.jts.operation.union.CascadedPolygonUnionTest;
import org.locationtech.jts.operation.union.UnaryUnionTest;
import org.locationtech.jts.operation.valid.IsValidTest;
import org.locationtech.jts.operation.valid.ValidClosedRingTest;
import org.locationtech.jts.operation.valid.ValidSelfTouchingRingFormingHoleTest;
import org.locationtech.jts.precision.SimpleGeometryPrecisionReducerTest;
import org.locationtech.jts.triangulate.ConformingDelaunayTest;
import org.locationtech.jts.triangulate.DelaunayTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    result.addTest(new TestSuite(org.locationtech.jts.algorithm.AngleTest.class));
    result.addTest(new TestSuite(AreaLengthTest.class));
    result.addTest(new TestSuite(BasicCoordinateSequenceTest.class));
    result.addTest(new TestSuite(BidirectionalComparatorTest.class));
    result.addTest(new TestSuite(BufferTest.class));
    result.addTest(new TestSuite(CascadedPolygonUnionTest.class));
    result.addTest(new TestSuite(org.locationtech.jts.algorithm.OrientationIndexTest.class));
    result.addTest(new TestSuite(ConformingDelaunayTest.class));
    result.addTest(new TestSuite(org.locationtech.jts.algorithm.ConvexHullTest.class));
    result.addTest(new TestSuite(CoordinateArraysTest.class));
    result.addTest(new TestSuite(DelaunayTest.class));
    result.addTest(new TestSuite(DistanceTest.class));
    result.addTest(new TestSuite(EnvelopeTest.class));
    result.addTest(new TestSuite(GeometryCollectionImplTest.class));
    result.addTest(new TestSuite(GeometryImplTest.class));
    result.addTest(new TestSuite(IntersectionMatrixTest.class));
    result.addTest(new TestSuite(IntervalTest.class));
    result.addTest(new TestSuite(org.locationtech.jts.algorithm.IsCCWTest.class));
    result.addTest(new TestSuite(IsRectangleTest.class));
    result.addTest(new TestSuite(IsValidTest.class));
    result.addTest(new TestSuite(LengthIndexedLineTest.class));
    result.addTest(new TestSuite(LineMergerTest.class));
    result.addTest(new TestSuite(LineStringImplTest.class));
    result.addTest(new TestSuite(LocationIndexedLineTest.class));
    result.addTest(new TestSuite(MiscellaneousTest.class));
    result.addTest(new TestSuite(MiscellaneousTest2.class));
    result.addTest(new TestSuite(MultiPointImplTest.class));
    result.addTest(new TestSuite(org.locationtech.jts.algorithm.NonRobustLineIntersectorTest.class));
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
