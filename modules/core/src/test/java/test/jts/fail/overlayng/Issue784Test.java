package test.jts.fail.overlayng;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Union of high-precision polygons - result misses one input area.
 * Original issue was a unary union of 6 polygons. 
 * Problem is also manifested in a simpler form by two specific polygons.
 *  
 * See https://github.com/locationtech/jts/issues/784
 *
 */
public class Issue784Test extends TestCase {
  
  public static void main(String args[]) {
    TestRunner.run(Issue784Test.class);
  }
  
  private GeometryFactory gf = new GeometryFactory();
  
  private Function<double[][], Polygon> createPolygon = points -> gf.createPolygon(
      IntStream.range(0, points[0].length)
              .mapToObj(index -> new CoordinateXY(points[0][index], points[1][index]))
              .toArray(CoordinateXY[]::new)
      );

  Polygon p1 = createPolygon.apply(new double[][]{
    {-16.15907384118054, -16.28767148661218, -13.719459554848422, -13.639456293556348, -16.15907384118054},
    {1.0157206673651495, 7.6835050482212575, 5.212481574525698, 1.0643148816523527, 1.0157206673651495}
  });
  Polygon p2 = createPolygon.apply(new double[][]{
    {-12.725226307666448, -13.639456293556348, -13.719459768757051, -12.78759751645274, -12.725226307666448},
    {1.0819470369308437, 1.0643148816523527, 5.212481780339414, 4.315883851167196, 1.0819470369308437}
  });
  Polygon p3 = createPolygon.apply(new double[][]{
  {-13.621824029083443, -16.14144162383529, -16.15907384118054, -13.639456293556348, -13.621824029083443},
  {0.15008489786842003, 0.10149068267229658, 1.0157206673651493, 1.0643148816523527, 0.15008489786842003}
  });
  Polygon p3r = createPolygon.apply(new double[][]{
  {-13.621824029083443, -16.14144162383529, -16.15907384118054, -13.639456293556348, -13.621824029083443},
  {0.1500848978684200, 0.10149068267229658, 1.0157206673651493, 1.0643148816523527, 0.1500848978684200}
  });
  Polygon p4 = createPolygon.apply(new double[][]{
      {-17.219533690879903, -16.28767148661218, -16.15907384118054, -17.073303827070436, -17.219533690879903},
      {8.580102931174729, 7.683505048221257, 1.0157206673651495, 0.9980885120866584, 8.580102931174729}
  });
  Polygon p5 = createPolygon.apply(new double[][]{
      {-12.707594043193543, -13.621824029083443, -13.639456293556348, -12.725226307666448, -12.707594043193543},
      {0.1677170531469111, 0.15008489786842005, 1.0643148816523527, 1.0819470369308437, 0.1677170531469111}
  });
  Polygon p6 = createPolygon.apply(new double[][]{
      {-17.055671609725188, -17.073303827070436, -16.15907384118054, -16.14144162383529, -17.055671609725188},
      {0.0838585273938056, 0.9980885120866584, 1.0157206673651493, 0.1014906826722966, 0.0838585273938056}
  });


  /**
   * Simplest reproducer.
   * 
   * @throws ParseException
   */
  public void testUnion_p3p5() throws ParseException {
    //checkUnion("35 - p3 Rounded", p3r, p5);
    checkUnion("35 - p3 Full", p3, p5);
  }

  /**
   * Original test case.  Simplified version shows same problem.
   * 
   * @throws ParseException
   */
  @Test
  public void xtestUnionOriginal() throws ParseException {
      Polygon expectedUnion = (Polygon) new WKTReader().read(
              "POLYGON ((-17.055671609725188 0.0838585273938056, -17.219533690879903 8.580102931174729, -12.78759751645274 4.315883851167196, -12.707594043193543 0.1677170531469111, -17.055671609725188 0.0838585273938056))"
      );
      Polygon[] polygons = {
              p1, p2, p3, p4, p5, p6
      };

      Geometry gc = gf.createGeometryCollection(polygons);
      System.out.println(gc);
      
      Geometry gcUnion = gc.union();
      System.out.println(gcUnion);

      Geometry mpUnion = gf.createMultiPolygon(polygons).union();
      
      Geometry indUnion = Arrays.stream(polygons)
              .map(Geometry.class::cast)
              .reduce(Geometry::union)
              .orElseThrow(null);

      assertEquals(expectedUnion.getArea(), gcUnion.getArea(), 0.0001); // Fail
      assertEquals(expectedUnion.getArea(), mpUnion.getArea(), 0.0001); // Fail
      assertEquals(expectedUnion.getArea(), indUnion.getArea(), 0.0001); // Pass
  }
  
  /**
   * See simpler case.
   * 
   * @throws ParseException
   */
  public void xtestUnion_p2p3p5() throws ParseException {
      checkUnion("235 - p3 Rounded", p2, p3r, p5);
      checkUnion("235 - p3 Full", p2, p3, p5);
  }

  private void checkUnion(String title, Polygon p2, Polygon p3, Polygon p5) {
    Geometry p25 = union(p2, p5);
    Geometry pall = union(p3, p25);
    double areaSum = p2.getArea() + p3.getArea() + p5.getArea();
    
    checkAreas(title, pall, areaSum);
  }

  private void checkUnion(String title, Polygon p1, Polygon p2) {
    Geometry pUnion = union(p1, p2);
    double areaSum = p1.getArea() + p2.getArea();
    
    checkAreas(title, pUnion, areaSum);
  }
  
  private static Geometry union(Geometry a, Geometry b) {
    return OverlayNGRobust.overlay(a, b, OverlayNG.UNION);
  }
  
  private void checkAreas(String title, Geometry union, double areaSum) {
    boolean isOk = isAreasClose(union, areaSum);
    String status = isOk ? "Success" : "FAILED!";
    System.out.println(title + " - Union status: " + status);
    assertTrue(isOk); 
  }

  private boolean isAreasClose(Geometry geom, double area) {
    double geomArea = geom.getArea();
    double areaDelta = Math.abs(geomArea - area);
    double deltaFrac = areaDelta / Math.max(geomArea, area);
    return deltaFrac < 0.1;
  }
}
