package test.jts.perf.operation.polygonize;

import java.util.Collection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class PolygonizerPerfTest extends PerformanceTestCase {

  GeometryFactory geomFact = new GeometryFactory();
  private Geometry circles;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(PolygonizerPerfTest.class);
  }

  public PolygonizerPerfTest(String name) {
    super(name);
    setRunSize(new int[] {1000, 10000, 20000});
    setRunIterations(1);
  }

  public void startRun(int size)
  {
    System.out.println("Running with size " + size);
    
    circles = createCircles(size);
  }
  
  private Geometry createCircles(int size) {
    Polygon[] polys = new Polygon[size];

    double radius = 10;
    double gap = 4 * radius;
    int nOnSide = (int) Math.sqrt(size) + 1; 
    for (int index = 0; index < size; index++) {
      int i = index % nOnSide;
      int j = index - (nOnSide * i);
      double x = i * gap;
      double y = j * gap;
      
      Point pt = geomFact.createPoint(new Coordinate(x,y));
      polys[index] = (Polygon) pt.buffer(radius);
    }
    return geomFact.createMultiPolygon(polys);
  }

  public void runDisjointCircles()
  {
    boolean extractOnlyPolygonal = false;
    Polygonizer polygonizer = new Polygonizer(extractOnlyPolygonal);
    polygonizer.add(circles);
    Collection output = polygonizer.getPolygons();
  }
}
