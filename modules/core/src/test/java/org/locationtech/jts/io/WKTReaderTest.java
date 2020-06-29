/*
 * Copyright (c) 2018 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io;

import org.locationtech.jts.geom.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

import java.util.EnumSet;
import java.util.Locale;


/**
 * Test for {@link WKTReader}
 *
 * @version 1.7
 */
public class WKTReaderTest extends TestCase {

  // WKT readers used throughout this test
  private final WKTReader reader2D;
  private final WKTReader reader2DOld;
  private final WKTReader reader3D;
  private final WKTReader reader2DM;
  private final WKTReader reader3DM;

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public WKTReaderTest(String name) {
    super(name);

    reader2D = GeometryTestCase.getWKTReader(Ordinate.createXY(), 1d);
    reader2D.setIsOldJtsCoordinateSyntaxAllowed(false);
    reader2DOld = GeometryTestCase.getWKTReader(Ordinate.createXY(), 1d);
    reader2DOld.setIsOldJtsCoordinateSyntaxAllowed(true);
    reader3D = GeometryTestCase.getWKTReader(Ordinate.createXYZ(), 1d);
    reader2DM = GeometryTestCase.getWKTReader(Ordinate.createXYM(), 1d);
    reader3DM = GeometryTestCase.getWKTReader(Ordinate.createXYZM(), 1d);
  }

  public static Test suite() { return new TestSuite(WKTReaderTest.class); }

  public void testReadNaN() throws Exception {

    // arrange
    CoordinateSequence seq = createSequence(Ordinate.createXYZ(), new double[] {10, 10});
    seq.setOrdinate(0, CoordinateSequence.Z, Double.NaN);

    // act
    Point pt1 = (Point)reader2DOld.read("POINT (10 10 NaN)");
    Point pt2 = (Point)reader2DOld.read("POINT (10 10 nan)");
    Point pt3 = (Point)reader2DOld.read("POINT (10 10 NAN)");

    // assert
    assertTrue(GeometryTestCase.checkEqual(seq, pt1.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seq, pt2.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seq, pt3.getCoordinateSequence()));
  }

  public void testReadPoint() throws Exception {

    // arrange
    double[] coordinates = new double[] {10, 10};
    CoordinateSequence seqPt2D = createSequence(Ordinate.createXY(), coordinates);
    CoordinateSequence seqPt2DE = createSequence(Ordinate.createXY(), new double[0]);
    CoordinateSequence seqPt3D = createSequence(Ordinate.createXYZ(), coordinates);
    CoordinateSequence seqPt2DM = createSequence(Ordinate.createXYM(), coordinates);
    CoordinateSequence seqPt3DM = createSequence(Ordinate.createXYZM(), coordinates);

    // act
    Point pt2D = (Point) reader2D.read("POINT (10 10)");
    Point pt2DE = (Point) reader2D.read("POINT EMPTY");
    Point pt3D = (Point) reader3D.read("POINT Z(10 10 10)");
    Point pt2DM = (Point) reader2DM.read("POINT M(10 10 11)");
    Point pt3DM = (Point) reader3DM.read("POINT ZM(10 10 10 11)");

    // assert
    assertTrue(GeometryTestCase.checkEqual(seqPt2D, pt2D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqPt2DE, pt2DE.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqPt3D, pt3D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqPt2DM, pt2DM.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqPt3DM, pt3DM.getCoordinateSequence()));
  }

  public void testReadLineString() throws Exception {

    // arrange
    double[] coordinates = new double[] {10, 10, 20, 20, 30, 40};
    CoordinateSequence seqLs2D = createSequence(Ordinate.createXY(), coordinates);
    CoordinateSequence seqLs2DE = createSequence(Ordinate.createXY(), new double[0]);
    CoordinateSequence seqLs3D = createSequence(Ordinate.createXYZ(), coordinates);
    CoordinateSequence seqLs2DM = createSequence(Ordinate.createXYM(), coordinates);
    CoordinateSequence seqLs3DM = createSequence(Ordinate.createXYZM(), coordinates);

    // act
    LineString ls2D = (LineString) reader2D
            .read("LINESTRING (10 10, 20 20, 30 40)");
    LineString ls2DE = (LineString) reader2D
            .read("LINESTRING EMPTY");
    LineString ls3D = (LineString) reader3D
            .read("LINESTRING Z(10 10 10, 20 20 10, 30 40 10)");
    LineString ls2DM = (LineString) reader2DM
            .read("LINESTRING M(10 10 11, 20 20 11, 30 40 11)");
    LineString ls3DM = (LineString) reader3DM
            .read("LINESTRING ZM(10 10 10 11, 20 20 10 11, 30 40 10 11)");

    // assert
    assertTrue(GeometryTestCase.checkEqual(seqLs2D, ls2D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs2DE, ls2DE.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs3D, ls3D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs2DM, ls2DM.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs3DM, ls3DM.getCoordinateSequence()));
  }

  public void testReadLinearRing() throws Exception {

    double[] coordinates = new double[] {10, 10, 20, 20, 30, 40, 10, 10};
    CoordinateSequence seqLs2D = createSequence(Ordinate.createXY(), coordinates);
    CoordinateSequence seqLs2DE = createSequence(Ordinate.createXY(), new double[0]);
    CoordinateSequence seqLs3D = createSequence(Ordinate.createXYZ(), coordinates);
    CoordinateSequence seqLs2DM = createSequence(Ordinate.createXYM(), coordinates);
    CoordinateSequence seqLs3DM = createSequence(Ordinate.createXYZM(), coordinates);

    // act
    LineString ls2D = (LineString) reader2D
            .read("LINEARRING (10 10, 20 20, 30 40, 10 10)");
    LineString ls2DE = (LineString) reader2D
            .read("LINEARRING EMPTY");
    LineString ls3D = (LineString) reader3D
            .read("LINEARRING Z(10 10 10, 20 20 10, 30 40 10, 10 10 10)");
    LineString ls2DM = (LineString) reader2DM
            .read("LINEARRING M(10 10 11, 20 20 11, 30 40 11, 10 10 11)");
    LineString ls3DM = (LineString) reader3DM
            .read("LINEARRING ZM(10 10 10 11, 20 20 10 11, 30 40 10 11, 10 10 10 11)");

    // assert
    assertTrue(GeometryTestCase.checkEqual(seqLs2D, ls2D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs2DE, ls2DE.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs3D, ls3D.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs2DM, ls2DM.getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(seqLs3DM, ls3DM.getCoordinateSequence()));

    try {
      reader2D.read("LINEARRING (10 10, 20 20, 30 40, 10 99)");
      fail();
    }
    catch (Throwable e) {
      assertTrue(e instanceof IllegalArgumentException);
      assertTrue(e.getMessage().contains("not form a closed linestring"));
    }
  }

  public void testReadPolygon() throws Exception {
    double[] shell = new double[] {10, 10, 10, 20, 20, 20, 20, 15, 10, 10};
    double[] ring1 = new double[] {11, 11, 12, 11, 12, 12, 12, 11, 11, 11};
    double[] ring2 = new double[] {11, 19, 11, 18, 12, 18, 12, 19, 11, 19};
    
    CoordinateSequence[] csPoly2D = new CoordinateSequence[] {
            createSequence(Ordinate.createXY(), shell),
            createSequence(Ordinate.createXY(), ring1),
            createSequence(Ordinate.createXY(), ring2)};
    CoordinateSequence csPoly2DE = createSequence(Ordinate.createXY(), new double[0]);
    CoordinateSequence[] csPoly3D = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZ(), shell),
            createSequence(Ordinate.createXYZ(), ring1),
            createSequence(Ordinate.createXYZ(), ring2)};
    CoordinateSequence[] csPoly2DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYM(), shell),
            createSequence(Ordinate.createXYM(), ring1),
            createSequence(Ordinate.createXYM(), ring2)};
    CoordinateSequence[] csPoly3DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZM(), shell),
            createSequence(Ordinate.createXYZM(), ring1),
            createSequence(Ordinate.createXYZM(), ring2)};
    
    WKTReader rdr = reader2D;
    Polygon[] poly2D = new Polygon[]{
            (Polygon) rdr.read("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))"),
            (Polygon) rdr.read("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10), (11 11, 12 11, 12 12, 12 11, 11 11))"),
            (Polygon) rdr.read("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10), (11 11, 12 11, 12 12, 12 11, 11 11), (11 19, 11 18, 12 18, 12 19, 11 19))")
    };
    Polygon poly2DE = (Polygon) rdr.read("POLYGON EMPTY");
    rdr =  reader3D;
    Polygon[] poly3D = new Polygon[]{
            (Polygon) rdr.read("POLYGON Z((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10))"),
            (Polygon) rdr.read("POLYGON Z((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10), (11 11 10, 12 11 10, 12 12 10, 12 11 10, 11 11 10))"),
            (Polygon) rdr.read("POLYGON Z((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10), (11 11 10, 12 11 10, 12 12 10, 12 11 10, 11 11 10), (11 19 10, 11 18 10, 12 18 10, 12 19 10, 11 19 10))")
    };
    rdr =  reader2DM;
    Polygon[] poly2DM = new Polygon[]{
            (Polygon) rdr.read("POLYGON M((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11))"),
            (Polygon) rdr.read("POLYGON M((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11), (11 11 11, 12 11 11, 12 12 11, 12 11 11, 11 11 11))"),
            (Polygon) rdr.read("POLYGON M((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11), (11 11 11, 12 11 11, 12 12 11, 12 11 11, 11 11 11), (11 19 11, 11 18 11, 12 18 11, 12 19 11, 11 19 11))")
    };
    rdr =  reader3DM;
    Polygon[] poly3DM = new Polygon[]{
            (Polygon) rdr.read("POLYGON ZM((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11))"),
            (Polygon) rdr.read("POLYGON ZM((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11), (11 11 10 11, 12 11 10 11, 12 12 10 11, 12 11 10 11, 11 11 10 11))"),
            (Polygon) rdr.read("POLYGON ZM((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11), (11 11 10 11, 12 11 10 11, 12 12 10 11, 12 11 10 11, 11 11 10 11), (11 19 10 11, 11 18 10 11, 12 18 10 11, 12 19 10 11, 11 19 10 11))")
    };
    // assert
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[0], poly2D[2].getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[1], poly2D[2].getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[2], poly2D[2].getInteriorRingN(1).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DE, poly2DE.getExteriorRing().getCoordinateSequence(), 2));

    assertTrue(GeometryTestCase.checkEqual(csPoly3D[0], poly3D[2].getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3D[1], poly3D[2].getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3D[2], poly3D[2].getInteriorRingN(1).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[0], poly2DM[2].getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[1], poly2DM[2].getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[2], poly2DM[2].getInteriorRingN(1).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[0], poly3DM[2].getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[1], poly3DM[2].getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[2], poly3DM[2].getInteriorRingN(1).getCoordinateSequence()));
  }

  public void testReadMultiPoint() throws Exception {

    // arrange
    double[][] coordinates = new double[][] { new double[] {10, 10}, new double[] {20, 20}};
    CoordinateSequence[] csMP2D = new CoordinateSequence[] {
            createSequence(Ordinate.createXY(), coordinates[0]),
            createSequence(Ordinate.createXY(), coordinates[1])};
    CoordinateSequence[] csMP3D = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZ(), coordinates[0]),
            createSequence(Ordinate.createXYZ(), coordinates[1])};
    CoordinateSequence[] csMP2DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYM(), coordinates[0]),
            createSequence(Ordinate.createXYM(), coordinates[1])};
    CoordinateSequence[] csMP3DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZM(), coordinates[0]),
            createSequence(Ordinate.createXYZM(), coordinates[1])};

    // act
    WKTReader rdr = reader2D;
    MultiPoint mP2D = (MultiPoint) rdr.read("MULTIPOINT ((10 10), (20 20))");
    MultiPoint mP2DE = (MultiPoint) rdr.read("MULTIPOINT EMPTY");
    rdr =  reader3D;
    MultiPoint mP3D = (MultiPoint) rdr.read("MULTIPOINT Z((10 10 10), (20 20 10))");
    rdr =  reader2DM;
    MultiPoint mP2DM = (MultiPoint) rdr.read("MULTIPOINT M((10 10 11), (20 20 11))");
    rdr =  reader3DM;
    MultiPoint mP3DM = (MultiPoint) rdr.read("MULTIPOINT ZM((10 10 10 11), (20 20 10 11))");

    // assert
    assertTrue(GeometryTestCase.checkEqual(csMP2D[0], ((Point)mP2D.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP2D[1], ((Point)mP2D.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(mP2DE.isEmpty());
    assertTrue(mP2DE.getNumGeometries() == 0);
    assertTrue(GeometryTestCase.checkEqual(csMP3D[0], ((Point)mP3D.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP3D[1], ((Point)mP3D.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP2DM[0], ((Point)mP2DM.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP2DM[1], ((Point)mP2DM.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP3DM[0], ((Point)mP3DM.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMP3DM[1], ((Point)mP3DM.getGeometryN(1)).getCoordinateSequence()));
  }

  public void testReadMultiLineString() throws Exception {

    // arrange
    double[][] coordinates = new double[][] { new double[] {10, 10, 20, 20}, new double[] {15, 15, 30, 15}};
    CoordinateSequence[] csMls2D = new CoordinateSequence[] {
            createSequence(Ordinate.createXY(), coordinates[0]),
            createSequence(Ordinate.createXY(), coordinates[1])};
    CoordinateSequence[] csMls3D = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZ(), coordinates[0]),
            createSequence(Ordinate.createXYZ(), coordinates[1])};
    CoordinateSequence[] csMls2DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYM(), coordinates[0]),
            createSequence(Ordinate.createXYM(), coordinates[1])};
    CoordinateSequence[] csMls3DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZM(), coordinates[0]),
            createSequence(Ordinate.createXYZM(), coordinates[1])};

    // act
    WKTReader rdr = reader2D;
    MultiLineString mLs2D = (MultiLineString) rdr.read("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))");
    MultiLineString mLs2DE = (MultiLineString) rdr.read("MULTILINESTRING EMPTY");
    rdr =  reader3D;
    MultiLineString mLs3D = (MultiLineString) rdr.read("MULTILINESTRING Z((10 10 10, 20 20 10), (15 15 10, 30 15 10))");
    rdr = reader2DM;
    MultiLineString mLs2DM = (MultiLineString) rdr.read("MULTILINESTRING M((10 10 11, 20 20 11), (15 15 11, 30 15 11))");
    rdr = reader3DM;
    MultiLineString mLs3DM = (MultiLineString) rdr.read("MULTILINESTRING ZM((10 10 10 11, 20 20 10 11), (15 15 10 11, 30 15 10 11))");

    // assert
    assertTrue(GeometryTestCase.checkEqual(csMls2D[0], ((LineString)mLs2D.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls2D[1], ((LineString)mLs2D.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(mLs2DE.isEmpty());
    assertTrue(mLs2DE.getNumGeometries() == 0);
    assertTrue(GeometryTestCase.checkEqual(csMls3D[0], ((LineString)mLs3D.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls3D[1], ((LineString)mLs3D.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls2DM[0], ((LineString)mLs2DM.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls2DM[1], ((LineString)mLs2DM.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls3DM[0], ((LineString)mLs3DM.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csMls3DM[1], ((LineString)mLs3DM.getGeometryN(1)).getCoordinateSequence()));
  }

  public void testReadMultiPolygon() throws Exception {

    double[] shell1 = new double[] {10, 10, 10, 20, 20, 20, 20, 15, 10, 10};
    double[] ring1 = new double[] {11, 11, 12, 11, 12, 12, 12, 11, 11, 11};
    double[] shell2 = new double[] {60, 60, 70, 70, 80, 60, 60, 60};

    CoordinateSequence[] csPoly2D = new CoordinateSequence[] {
            createSequence(Ordinate.createXY(), shell1),
            createSequence(Ordinate.createXY(), ring1),
            createSequence(Ordinate.createXY(), shell2)};
    CoordinateSequence[] csPoly3D = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZ(), shell1),
            createSequence(Ordinate.createXYZ(), ring1),
            createSequence(Ordinate.createXYZ(), shell2)};
    CoordinateSequence[] csPoly2DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYM(), shell1),
            createSequence(Ordinate.createXYM(), ring1),
            createSequence(Ordinate.createXYM(), shell2)};
    CoordinateSequence[] csPoly3DM = new CoordinateSequence[] {
            createSequence(Ordinate.createXYZM(), shell1),
            createSequence(Ordinate.createXYZM(), ring1),
            createSequence(Ordinate.createXYZM(), shell2)};

    WKTReader rdr = reader2D;
    MultiPolygon[] poly2D = new MultiPolygon[]{
            (MultiPolygon) rdr.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10), (11 11, 12 11, 12 12, 12 11, 11 11)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10), (11 11, 12 11, 12 12, 12 11, 11 11)), ((60 60, 70 70, 80 60, 60 60)))")
    };
    MultiPolygon poly2DE = (MultiPolygon) rdr.read("MULTIPOLYGON EMPTY");
    rdr =  reader3D;
    MultiPolygon[] poly3D = new MultiPolygon[]{
            (MultiPolygon) rdr.read("MULTIPOLYGON Z(((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON Z(((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10), (11 11 10, 12 11 10, 12 12 10, 12 11 10, 11 11 10)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON Z(((10 10 10, 10 20 10, 20 20 10, 20 15 10, 10 10 10), (11 11 10, 12 11 10, 12 12 10, 12 11 10, 11 11 10)), ((60 60 10, 70 70 10, 80 60 10, 60 60 10)))")
    };
    MultiPolygon[] poly2DM = new MultiPolygon[]{
            (MultiPolygon) rdr.read("MULTIPOLYGON M(((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON M(((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11), (11 11 11, 12 11 11, 12 12 11, 12 11 11, 11 11 11)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON M(((10 10 11, 10 20 11, 20 20 11, 20 15 11, 10 10 11), (11 11 11, 12 11 11, 12 12 11, 12 11 11, 11 11 11)), ((60 60 11, 70 70 11, 80 60 11, 60 60 11)))")
    };
    rdr =  reader3DM;
    MultiPolygon[] poly3DM = new MultiPolygon[]{
            (MultiPolygon) rdr.read("MULTIPOLYGON ZM(((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON ZM(((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11), (11 11 10 11, 12 11 10 11, 12 12 10 11, 12 11 10 11, 11 11 10 11)))"),
            (MultiPolygon) rdr.read("MULTIPOLYGON ZM(((10 10 10 11, 10 20 10 11, 20 20 10 11, 20 15 10 11, 10 10 10 11), (11 11 10 11, 12 11 10 11, 12 12 10 11, 12 11 10 11, 11 11 10 11)), ((60 60 10 11, 70 70 10 11, 80 60 10 11, 60 60 10 11)))")
    };

    // assert
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[0], ((Polygon)poly2D[2].getGeometryN(0)).getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[1], ((Polygon)poly2D[2].getGeometryN(0)).getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2D[2], ((Polygon)poly2D[2].getGeometryN(1)).getExteriorRing().getCoordinateSequence()));
    assertTrue(poly2DE.isEmpty());
    assertTrue(poly2DE.getNumGeometries() == 0);

    assertTrue(GeometryTestCase.checkEqual(csPoly3D[0],((Polygon)poly3D[2].getGeometryN(0)).getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3D[1], ((Polygon)poly3D[2].getGeometryN(0)).getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3D[2], ((Polygon)poly3D[2].getGeometryN(1)).getExteriorRing().getCoordinateSequence()));

    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[0], ((Polygon)poly2DM[2].getGeometryN(0)).getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[1], ((Polygon)poly2DM[2].getGeometryN(0)).getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly2DM[2], ((Polygon)poly2DM[2].getGeometryN(1)).getExteriorRing().getCoordinateSequence()));

    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[0], ((Polygon)poly3DM[2].getGeometryN(0)).getExteriorRing().getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[1], ((Polygon)poly3DM[2].getGeometryN(0)).getInteriorRingN(0).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(csPoly3DM[2], ((Polygon)poly3DM[2].getGeometryN(1)).getExteriorRing().getCoordinateSequence()));
  }

  public void testReadGeometryCollection() throws Exception {

    // arrange
    double[][] coordinates = new double[][] { new double[] {10, 10}, new double[] {30, 30},
            new double[] {15, 15, 20, 20}, new double[0], new double[] {10, 10, 20, 20, 30, 40, 10, 10}  };

    CoordinateSequence[] css = new CoordinateSequence[] {
            createSequence(Ordinate.createXY(), coordinates[0]),
            createSequence(Ordinate.createXY(), coordinates[1]),
            createSequence(Ordinate.createXY(), coordinates[2]),
            createSequence(Ordinate.createXY(), coordinates[3]),
            createSequence(Ordinate.createXY(), coordinates[4]),
    };

    // arrange
    WKTReader rdr = GeometryTestCase.getWKTReader(Ordinate.createXY(), 1);
    GeometryCollection gc0 = (GeometryCollection)rdr.read("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))");
    GeometryCollection gc1 = (GeometryCollection)rdr.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))");
    GeometryCollection gc2 = (GeometryCollection)rdr.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))");
    GeometryCollection gc3 = (GeometryCollection)rdr.read("GEOMETRYCOLLECTION EMPTY");

    // assert
    assertTrue(GeometryTestCase.checkEqual(css[0], ((Point)gc0.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[1], ((Point)gc0.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[2], ((LineString)gc0.getGeometryN(2)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[0], ((Point)gc1.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[3], ((LinearRing)gc1.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[2], ((LineString)gc1.getGeometryN(2)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[0], ((Point)gc2.getGeometryN(0)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[4], ((LinearRing)gc2.getGeometryN(1)).getCoordinateSequence()));
    assertTrue(GeometryTestCase.checkEqual(css[2], ((LineString)gc2.getGeometryN(2)).getCoordinateSequence()));
    assertTrue(gc3.isEmpty());
  }

  public void testReadLargeNumbers() throws Exception {
    PrecisionModel precisionModel = new PrecisionModel(1E9);
    GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
    WKTReader reader = new WKTReader(geometryFactory);
    CoordinateSequence point1 = ((Point)reader.read("POINT (123456789.01234567890 10)")).getCoordinateSequence();
    CoordinateSequence point2 = geometryFactory.createPoint(new Coordinate(123456789.01234567890, 10)).getCoordinateSequence();
    assertEquals(point1.getOrdinate(0, CoordinateSequence.X), point2.getOrdinate(0, CoordinateSequence.X), 1E-7);
    assertEquals(point1.getOrdinate(0, CoordinateSequence.Y), point2.getOrdinate(0, CoordinateSequence.Y), 1E-7);
  }

  public void testTurkishLocale() throws Exception {
      Locale original = Locale.getDefault();
      try {
          Locale.setDefault(Locale.forLanguageTag("tr"));
          Point point = (Point) reader2D.read("point (10 20)");
          assertEquals(10.0, point.getX(), 1E-7);
          assertEquals(20.0, point.getY(), 1E-7);
      } finally {
          Locale.setDefault(original);
      }
  }

  private static CoordinateSequence createSequence(EnumSet<Ordinate> ordinateFlags, double[] xy) {

    // get the number of dimension to verify size of provided ordinate values array
    int dimension = requiredDimension(ordinateFlags);

    // inject additional values
    double[] ordinateValues = injectZM(ordinateFlags, xy);
    
    if ((ordinateValues.length % dimension) != 0)
      throw new IllegalArgumentException("ordinateFlags and number of provided ordinate values don't match");

    // get the required size of the sequence
    int size = ordinateValues.length / dimension;

    // create a sequence capable of storing all ordinate values.
    CoordinateSequence res = GeometryTestCase.getCSFactory(ordinateFlags)
            .create(size, requiredDimension(ordinateFlags));

    // fill in values
    int k = 0;
    for(int i = 0; i < ordinateValues.length; i+= dimension) {
      for (int j = 0; j < dimension; j++)
        res.setOrdinate(k, j, ordinateValues[i+j]);
      k++;
    }

    return res;
  }

  private static int requiredDimension(EnumSet<Ordinate> ordinateFlags)
  {
    return ordinateFlags.size();
  }


  private static double[] injectZM(EnumSet<Ordinate> ordinateFlags, double[] xy) {
    int size = xy.length / 2;
    int dimension = requiredDimension(ordinateFlags);
    double[] res = new double[size * dimension];
    int k = 0;
    for (int i = 0; i < xy.length; i+=2) {
      res[k++] = xy[i];
      res[k++] = xy[i+1];
      if (ordinateFlags.contains(Ordinate.Z)) res[k++] = 10;
      if (ordinateFlags.contains(Ordinate.M)) res[k++] = 11;
    }
    return res;
  }
}
