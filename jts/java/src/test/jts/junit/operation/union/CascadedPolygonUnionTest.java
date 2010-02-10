package test.jts.junit.operation.union;

import java.util.*;
import java.io.*;
import test.jts.TestFiles;
import test.jts.junit.*;

import com.vividsolutions.jts.algorithm.match.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.operation.union.*;
import junit.framework.TestCase;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using synthetic datasets.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTest extends TestCase 
{
	GeometryFactory geomFact = new GeometryFactory();
	
  public CascadedPolygonUnionTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionTest.class);
  }
  
  public void testBoxes()
  throws Exception
  {
  	runTest(GeometryUtils.readWKT(
  			new String[] {
  				"POLYGON ((80 260, 200 260, 200 30, 80 30, 80 260))",
  				"POLYGON ((30 180, 300 180, 300 110, 30 110, 30 180))",
  				"POLYGON ((30 280, 30 150, 140 150, 140 280, 30 280))"
  			}),
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs1()
  throws Exception
  {
  	Collection geoms = createDiscs(5, 0.7);
  	
  	System.out.println(geomFact.buildGeometry(geoms));
  	
  	runTest(geoms,
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  
  public void testDiscs2()
  throws Exception
  {
  	Collection geoms = createDiscs(5, 0.55);
  	
  	System.out.println(geomFact.buildGeometry(geoms));
  	
  	runTest(geoms,
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  
  // TODO: add some synthetic tests
  
  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();
  
  private void runTest(Collection geoms, double minimumMeasure) 
  {
  	assertTrue(tester.test(geoms, minimumMeasure));
  }
  
  private Collection createDiscs(int num, double radius)
  {
  	List geoms = new ArrayList();
  	for (int i = 0; i < num; i++) {
    	for (int j = 0; j < num; j++) {
    		Coordinate pt = new Coordinate(i, j);
    		Geometry ptGeom = geomFact.createPoint(pt);
    		Geometry disc = ptGeom.buffer(radius);
    		geoms.add(disc);
    	}
  	}
  	return geoms;
  }
}
