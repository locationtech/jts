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
 * using data from files.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionFileTest extends TestCase 
{
  public CascadedPolygonUnionFileTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionFileTest.class);
  }
  
  public void testAfrica()
  throws Exception
  {
  	runTest(TestFiles.DATA_DIR + "africa.wkt", 
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testEurope()
  throws Exception
  {
  	runTest(TestFiles.DATA_DIR + "europe.wkt", 
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();
  
  private void runTest(String filename, double minimumMeasure) 
  throws IOException, ParseException
  {
  	Collection geoms = GeometryUtils.readWKTFile(filename);
  	assertTrue(tester.test(geoms, minimumMeasure));
  }
  
}
