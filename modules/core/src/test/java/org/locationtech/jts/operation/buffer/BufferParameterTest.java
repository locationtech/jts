package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

/**
 * Tests for the effect of buffer parameter values.
 * 
 * @author Martin Davis
 *
 */
public class BufferParameterTest extends GeometryTestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(BufferParameterTest.class);
  }
  
  public BufferParameterTest(String name) {
    super(name);
  }
  
  public void testQuadSegsNeg() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, -99, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs0() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 0, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs1() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 1, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs2() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 2, 
        "POLYGON ((70 30, 70 80, 72.92893218813452 87.07106781186548, 80 90, 87.07106781186548 87.07106781186548, 90 80, 90 20, 87.07106781186548 12.928932188134524, 80 10, 20 10, 12.928932188134523 12.928932188134524, 10 20, 12.928932188134524 27.071067811865476, 20 30, 70 30))");
  }

  public void testQuadSegs2Bevel() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 2, BufferParameters.JOIN_BEVEL,
        "POLYGON ((70 30, 70 80, 72.92893218813452 87.07106781186548, 80 90, 87.07106781186548 87.07106781186548, 90 80, 90 20, 80 10, 20 10, 12.928932188134523 12.928932188134524, 10 20, 12.928932188134524 27.071067811865476, 20 30, 70 30))");
  }

  //----------------------------------------------------
  
  public void testMitreRight0() {
    checkBuffer("LINESTRING (20 20, 20 80, 80 80)", 
        10.0, bufParamFlatMitre(0),
        "POLYGON ((10 80, 20 90, 80 90, 80 70, 30 70, 30 20, 10 20, 10 80))");
  }  
  
  public void testMitreRight1() {
    checkBuffer("LINESTRING (20 20, 20 80, 80 80)", 
        10.0, bufParamFlatMitre(1),
        "POLYGON ((10 20, 10 84.14213562373095, 15.857864376269049 90, 80 90, 80 70, 30 70, 30 20, 10 20))");
  }

  public void testMitreRight2() {
    checkBuffer("LINESTRING (20 20, 20 80, 80 80)", 
        10.0, bufParamFlatMitre(2),
        "POLYGON ((10 20, 10 90, 80 90, 80 70, 30 70, 30 20, 10 20))");
  }
  
  public void testMitreNarrow0() {
    checkBuffer("LINESTRING (10 20, 20 80, 30 20)", 
        10.0, bufParamFlatMitre(0),
        "POLYGON ((10.136060761678563 81.64398987305357, 29.863939238321436 81.64398987305357, 39.863939238321436 21.643989873053574, 20.136060761678564 18.356010126946426, 20 19.172374697017812, 19.863939238321436 18.356010126946426, 0.1360607616785625 21.643989873053574, 10.136060761678563 81.64398987305357))");
  }
  
  public void testMitreNarrow1() {
    checkBuffer("LINESTRING (10 20, 20 80, 30 20)", 
        10.0, bufParamFlatMitre(1),
        "POLYGON ((11.528729116169634 90, 28.47127088383036 90, 39.863939238321436 21.643989873053574, 20.136060761678564 18.356010126946426, 20 19.172374697017812, 19.863939238321436 18.356010126946426, 0.1360607616785625 21.643989873053574, 11.528729116169634 90))");
  }
  
  public void testMitreNarrow5() {
    checkBuffer("LINESTRING (10 20, 20 80, 30 20)", 
        10.0, bufParamFlatMitre(5),
        "POLYGON ((18.1953957828363 130, 21.804604217163696 130, 39.863939238321436 21.643989873053574, 20.136060761678564 18.356010126946426, 20 19.172374697017812, 19.863939238321436 18.356010126946426, 0.1360607616785625 21.643989873053574, 18.1953957828363 130))");
  }
  
  public void testMitreNarrow10() {
    checkBuffer("LINESTRING (10 20, 20 80, 30 20)", 
        10.0, bufParamFlatMitre(10),
        "POLYGON ((20 140.82762530298217, 39.863939238321436 21.643989873053574, 20.136060761678564 18.356010126946426, 20 19.172374697017812, 19.863939238321436 18.356010126946426, 0.1360607616785625 21.643989873053574, 20 140.82762530298217))");
  }
  
  public void testMitreObtuse0() {
    checkBuffer("LINESTRING (10 10, 50 20, 90 10)", 
        1.0, bufParamFlatMitre(0),
        "POLYGON ((49.75746437496367 20.970142500145332, 50.24253562503633 20.970142500145332, 90.24253562503634 10.970142500145332, 89.75746437496366 9.029857499854668, 50 18.969223593595583, 10.242535625036332 9.029857499854668, 9.757464374963668 10.970142500145332, 49.75746437496367 20.970142500145332))");
  }
  
  public void testMitreObtuse1() {
    checkBuffer("LINESTRING (10 10, 50 20, 90 10)", 
        1.0, bufParamFlatMitre(1),
        "POLYGON ((9.757464374963668 10.970142500145332, 49.876894374382324 21, 50.12310562561766 20.999999999999996, 90.24253562503634 10.970142500145332, 89.75746437496366 9.029857499854668, 50 18.969223593595583, 10.242535625036332 9.029857499854668, 9.757464374963668 10.970142500145332))");
  }
  
  public void testMitreObtuse2() {
    checkBuffer("LINESTRING (10 10, 50 20, 90 10)", 
        1.0, bufParamFlatMitre(2),
        "POLYGON ((50 21.030776406404417, 90.24253562503634 10.970142500145332, 89.75746437496366 9.029857499854668, 50 18.969223593595583, 10.242535625036332 9.029857499854668, 9.757464374963668 10.970142500145332, 50 21.030776406404417))");
  }
  
  //----------------------------------------------------

  public void testMitreSquareCCW1() {
    checkBuffer("POLYGON((0 0, 100 0, 100 100, 0 100, 0 0))", 
        10.0, bufParamFlatMitre(1),
        "POLYGON ((-10 -4.142135623730949, -10 104.14213562373095, -4.142135623730949 110, 104.14213562373095 110, 110 104.14213562373095, 110 -4.142135623730949, 104.14213562373095 -10, -4.142135623730949 -10, -10 -4.142135623730949))");
  }
  
  public void testMitreSquare1() {
    checkBuffer("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))", 
        10.0, bufParamFlatMitre(1),
        "POLYGON ((-4.14213562373095 -10, -10 -4.14213562373095, -10 104.14213562373095, -4.14213562373095 110, 104.14213562373095 110, 110 104.14213562373095, 110 -4.142135623730951, 104.14213562373095 -10, -4.14213562373095 -10))");
  }
  
  private void checkBuffer(String wkt, double dist, int quadSegs, String wktExpected) {
    checkBuffer( wkt, dist, quadSegs, BufferParameters.JOIN_ROUND, wktExpected);
  }
  
  private void checkBuffer(String wkt, double dist, int quadSegs, int joinStyle, String wktExpected) {
    BufferParameters param = new BufferParameters();
    param.setQuadrantSegments(quadSegs);
    param.setJoinStyle(joinStyle);
    checkBuffer(wkt, dist, param, wktExpected);
  }

  private void checkBuffer(String wkt, double dist, BufferParameters param, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = BufferOp.bufferOp(geom, dist, param);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, 0.00001);
  }

  private static BufferParameters bufParamFlatMitre(double mitreLimit) {
    BufferParameters param = new BufferParameters();
    param.setJoinStyle(BufferParameters.JOIN_MITRE);
    param.setMitreLimit(mitreLimit);
    param.setEndCapStyle(BufferParameters.CAP_FLAT);
    return param;
  }
}
