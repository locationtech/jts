package org.locationtech.jts.operation.union;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import test.jts.GeometryTestCase;

public class OverlapUnionTest extends GeometryTestCase {

  public OverlapUnionTest(String name) {
    super(name);
  }


  public static void main(String[] args) {
    junit.textui.TestRunner.run(OverlapUnionTest.class);
  }
  
  public void testFixedPrecCausingBorderChange() throws ParseException {
    
    String a = "POLYGON ((130 -10, 20 -10, 20 22, 30 20, 130 20, 130 -10))";
    String b = "MULTIPOLYGON (((50 0, 100 450, 100 0, 50 0)), ((53 28, 50 28, 50 30, 53 30, 53 28)))";
    
    checkUnionWithTopologyFailure(a, b, 1);
  }

  public void testFullPrecision() throws ParseException {
    
    String a = "POLYGON ((130 -10, 20 -10, 20 22, 30 20, 130 20, 130 -10))";
    String b = "MULTIPOLYGON (((50 0, 100 450, 100 0, 50 0)), ((53 28, 50 28, 50 30, 53 30, 53 28)))";
    
    checkUnion(a, b);
  }

  public void testSimpleOverlap() throws ParseException {
    
    String a = "MULTIPOLYGON (((0 400, 50 400, 50 350, 0 350, 0 400)), ((200 200, 220 200, 220 180, 200 180, 200 200)), ((350 100, 370 100, 370 80, 350 80, 350 100)))";
    String b = "MULTIPOLYGON (((430 20, 450 20, 450 0, 430 0, 430 20)), ((100 300, 124 300, 124 276, 100 276, 100 300)), ((230 170, 210 170, 210 190, 230 190, 230 170)))";
    
    checkUnionOptimized(a, b);
  }


  /**
   * It is hard to create a situation where border segments change by 
   * enough to cause an invalid geometry to be returned.
   * One way is to use a fixed precision model, 
   * which will cause segments to move enough to 
   * intersect with non-overlapping components.
   * <p>
   * However, the current union algorithm
   * emits topology failures for these situations, since
   * it is not performing snap-rounding. 
   * These exceptions are irrelevant to the correctness
   * of the OverlapUnion algorithm, so are prevented from being reported as a test failure.
   * 
   * @param wktA
   * @param wktB
   * @param scaleFactor
   * @throws ParseException
   */
  private void checkUnionWithTopologyFailure(String wktA, String wktB, double scaleFactor) throws ParseException {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    GeometryFactory geomFact = new GeometryFactory(pm);
    WKTReader rdr = new WKTReader(geomFact);

    Geometry a = rdr.read(wktA);
    Geometry b = rdr.read(wktB);
    
    OverlapUnion union = new OverlapUnion(a, b);
    
    Geometry result = null;
    try {
      result = union.union();
    }
    catch (TopologyException ex) {
      boolean isOptimized = union.isUnionOptimized();
      
      // if the optimized algorithm was used then this is a real error
      if (isOptimized) throw ex;
      
      // otherwise the error is probably due to the fixed precision
      // not being handled by the current union code
      return;
    }
    assertTrue( "OverlapUnion result is invalid", result.isValid());
  }
  
  private void checkUnion(String wktA, String wktB) throws ParseException {
    checkUnion(wktA, wktB, false);
  }
  
  private void checkUnionOptimized(String wktA, String wktB) throws ParseException {
    checkUnion(wktA, wktB, true);
  }
  
  private void checkUnion(String wktA, String wktB, boolean isCheckOptimized) throws ParseException {
    PrecisionModel pm = new PrecisionModel();
    GeometryFactory geomFact = new GeometryFactory(pm);
    WKTReader rdr = new WKTReader(geomFact);

    Geometry a = rdr.read(wktA);
    Geometry b = rdr.read(wktB);
    
    OverlapUnion union = new OverlapUnion(a, b);
    Geometry result = union.union();
    
    if (isCheckOptimized) {
      boolean isOptimized = union.isUnionOptimized();
      assertTrue("Union was not performed using combine", isOptimized);
    }
    
    assertTrue( "OverlapUnion result is invalid", result.isValid());
  }
}
