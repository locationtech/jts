package test.jts.perf.operation.union;

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class PolygonUnionPerfTest 
{

  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  GeometryFactory factory = new GeometryFactory();
  
  public static void main(String[] args) {
    PolygonUnionPerfTest test = new PolygonUnionPerfTest();
    
//    test.test();
    test.testRampItems();
    
  }

  boolean testFailed = false;

  public PolygonUnionPerfTest() {
  }

  public void testRampItems()
  {
    int nPts = 1000;
    
    test(5, nPts, 10.0);
    test(5, nPts, 10.0);
    test(25, nPts, 10.0);
    test(50, nPts, 10.0);
    test(100, nPts, 10.0);
    test(200, nPts, 10.0);
    test(400, nPts, 10.0);  
    test(500, nPts, 10.0);  
    test(1000, nPts, 10.0);  
    test(2000, nPts, 10.0);  
    test(4000, nPts, 10.0);  
  }
  
  public void test()
  {
//    test(5, 100, 10.0);
    test(1000, 100, 10.0);
  }
  
  public void test(int nItems, int nPts, double size)
  {
    System.out.println("---------------------------------------------------------");
    System.out.println("# pts/item: " + nPts);

    List polys = createPolys(nItems, size, nPts);
    
//    System.out.println();
    //System.out.println("Running with " + nPts + " points");
    
    UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }
  
  /**
   * Creates a grid of circles with a small percentage of overlap
   * in both directions.
   * This approximated likely real-world cases well, 
   * and seems to produce
   * close to worst-case performance for the Iterated algorithm.
   * 
   * Sample times: 
   * 1000 items/100 pts - Cascaded: 2718 ms, Iterated 150 s
   * 
   * @param nItems
   * @param size
   * @param nPts
   * @return
   */
  List createPolys(int nItems, double size, int nPts)
  {

  	// between 0 and 1
    double overlapPct = 0.2;

    
    int nCells = (int) Math.sqrt(nItems);

    List geoms = new ArrayList();
//    double width = env.getWidth();
    double width = nCells * (1 - overlapPct) * size;
    
    // this results in many final polys
    double height = nCells * 2 * size;
    
    // this results in a single final polygon
//    double height = width;
    
    double xInc = width / nCells;
    double yInc = height / nCells;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        Coordinate base = new Coordinate(
            i * xInc,
            j * yInc);
        Geometry poly = createPoly(base, size, nPts);
        geoms.add(poly);
//        System.out.println(poly);
      }
    }
    return geoms;
  }
  
  Geometry createPoly(Coordinate base, double size, int nPts)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory(factory);
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    
    Geometry poly = gsf.createCircle();
//    Geometry poly = gsf.createRectangle();
    
//    System.out.println(circle);
    return poly;
  }
  
    
}
