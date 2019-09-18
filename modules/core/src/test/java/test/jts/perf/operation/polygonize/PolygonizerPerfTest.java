package test.jts.perf.operation.polygonize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Test performance of {@link Polygonizer}.
 * Force large number of hole-in-shell tests, 
 * as well as testing against large ring 
 * for point-in-polygon computation.
 * 
 * @author mdavis
 *
 */
public class PolygonizerPerfTest extends PerformanceTestCase {

  private static final int BUFFER_SEGS = 10;
  
  GeometryFactory geomFact = new GeometryFactory();
  private Geometry testCircles;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(PolygonizerPerfTest.class);
  }

  public PolygonizerPerfTest(String name) {
    super(name);
    setRunSize(new int[] {10, /* 100, 200, 300, 400, 500, */ 1000, 2000});
    setRunIterations(1);
  }

  public void startRun(int num)
  {
    System.out.println("Running with size " + num);
    
    double size = 100;
    List<Polygon> polys = createCircleGrid(num, size, BUFFER_SEGS);
    
    Polygon surround = createAnnulus(size/2, size/2, 2*size, size, 1000 * BUFFER_SEGS);
    polys.add(surround);
    testCircles = geomFact.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
    //System.out.println(testCircles);
  }
  
  private List<Polygon> createCircleGrid(int num, double size, int bufferSegs) {
    List<Polygon> polys = new ArrayList<Polygon>();

    int nOnSide = (int) Math.sqrt(num) + 1; 
    double radius = size / nOnSide / 4;
    double gap = 4 * radius;

    for (int index = 0; index < num; index++) {
      int iy = index / nOnSide;
      int ix = index % nOnSide;
      double x = ix * gap;
      double y = iy * gap;
      
      Polygon poly = createAnnulus(x, y, radius, radius/2, bufferSegs);
      polys.add( poly );
    }
    return polys;
  }

  private Polygon createAnnulus(double x, double y, double radius, double innerRadius, int bufferSegs) {
    Point pt = geomFact.createPoint(new Coordinate(x, y));
    LinearRing shell = bufferRing(pt, radius, bufferSegs);
    LinearRing hole = bufferRing(pt, innerRadius, bufferSegs);
    return pt.getFactory().createPolygon(shell, new LinearRing[] { hole });
  }

  private LinearRing bufferRing(Point pt, double radius, int bufferSegs) {
    Polygon buf = (Polygon) pt.buffer(radius, bufferSegs);
    return buf.getExteriorRing();
  }

  public void runDisjointCirclesInsideDonut()
  {
    boolean extractOnlyPolygonal = false;
    Polygonizer polygonizer = new Polygonizer(extractOnlyPolygonal);
    polygonizer.add(testCircles);
    Collection output = polygonizer.getPolygons();
  }
}
