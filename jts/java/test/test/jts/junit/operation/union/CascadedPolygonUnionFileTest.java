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
  
  public void testAfrica2()
  throws Exception
  {
    runTestResource("../../../../../data/africa.wkt", 
        CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void XtestEurope()
  throws Exception
  {
    runTestResource("../../../../../data/europe.wkt", 
  			CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();
  
  private void runTest(String filename, double minimumMeasure) 
  throws IOException, ParseException
  {
    Collection geoms = GeometryUtils.readWKTFile(filename);
    assertTrue(tester.test(geoms, minimumMeasure));
  }
  private void runTestResource(String resource, double minimumMeasure) 
  throws IOException, ParseException
  {
    InputStream is = this.getClass().getResourceAsStream(resource);
    // don't bother if file is missing
    if (is == null) return;
    Collection geoms = GeometryUtils.readWKTFile(new InputStreamReader(is));
    assertTrue(tester.test(geoms, minimumMeasure));
  }
  
}
