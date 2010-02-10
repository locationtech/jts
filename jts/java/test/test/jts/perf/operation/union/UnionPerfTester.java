package test.jts.perf.operation.union;


import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import com.vividsolutions.jts.util.Stopwatch;


public class UnionPerfTester 
{
  public static final int CASCADED = 1;
  public static final int ITERATED = 2;
  public static final int BUFFER0 = 3;
  public static final int ORDERED = 4;
  
  public static void run(String testName, int testType, List polys)
  {
  	UnionPerfTester test = new UnionPerfTester(polys);
  	test.run(testName, testType);
  }
  
  public static void runAll(List polys)
  {
  	UnionPerfTester test = new UnionPerfTester(polys);
  	test.runAll();
  }
  
  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();
  GeometryFactory factory = new GeometryFactory();
  
  private List polys;
  
  public UnionPerfTester(List polys) {
  	this.polys = polys;
  }


  
  public void runAll()
  {
    System.out.println("# items: " + polys.size());
    run("Cascaded", CASCADED, polys);
//    run("Buffer-0", BUFFER0, polys);

    run("Iterated", ITERATED, polys);

  }

  public void run(String testName, int testType)
  {
  	System.out.println();
    System.out.println("======= Union Algorithm: " + testName + " ===========");

    Stopwatch sw = new Stopwatch();
    for (int i = 0; i < MAX_ITER; i++) {
    	Geometry union = null;
    	switch (testType) {
    	case CASCADED:
    		union = unionCascaded(polys);
    		break;
    	case ITERATED:
        union = unionAllSimple(polys);
        break;
    	case BUFFER0:
        union = unionAllBuffer(polys);
        break;
    	}
      
//    	printFormatted(union);

    }
    System.out.println("Finished in " + sw.getTimeString());
  }

  private void printFormatted(Geometry geom)
  {
  	WKTWriter writer = new WKTWriter();
  	System.out.println(writer.writeFormatted(geom));
  }
  
  public Geometry unionAllSimple(List geoms)
  {
    Geometry unionAll = null;
    int count = 0;
    for (Iterator i = geoms.iterator(); i.hasNext(); ) {
      Geometry geom = (Geometry) i.next();
      
      if (unionAll == null) {
      	unionAll = (Geometry) geom.clone();
      }
      else {
      	unionAll = unionAll.union(geom);
      }
      
      count++;
      if (count % 100 == 0) {
        System.out.print(".");
//        System.out.println("Adding geom #" + count);
      }
    }
    return unionAll;
  }
  
  public Geometry unionAllBuffer(List geoms)
  {
  	
  	Geometry gColl = factory.buildGeometry(geoms);
  	Geometry unionAll = gColl.buffer(0.0);
    return unionAll;
  }
  
  public Geometry unionCascaded(List geoms)
  {
  	return CascadedPolygonUnion.union(geoms);
  }
  
  /*
  public Geometry unionAllOrdered(List geoms)
  {
//  	return OrderedUnion.union(geoms);
  }
  */
  
  void printItemEnvelopes(List tree)
  {
    Envelope itemEnv = new Envelope();
    for (Iterator i = tree.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (o instanceof List) {
        printItemEnvelopes((List) o);
      }
      else if (o instanceof Geometry) {
        itemEnv.expandToInclude( ((Geometry) o).getEnvelopeInternal());
      }
    }
    System.out.println(factory.toGeometry(itemEnv));
  }
}
