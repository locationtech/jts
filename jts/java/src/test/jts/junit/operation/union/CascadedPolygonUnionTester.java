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
 * Compares the results of CascadedPolygonUnion to Geometry.union()
 * using shape similarity measures.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTester
{
	public static final double MIN_SIMILARITY_MEAURE = 0.999999;;
	
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);

	GeometryFactory geomFact = new GeometryFactory();
	
  public CascadedPolygonUnionTester() {
  }
  
  public boolean test(Collection geoms, double minimumMeasure) 
  {
    System.out.println("Computing Iterated union");
    Geometry union1 = unionIterated(geoms);
    System.out.println("Computing Cascaded union");
    Geometry union2 = unionCascaded(geoms);
    
    System.out.println("Testing similarity with min measure = " + minimumMeasure);
    
    double areaMeasure = (new AreaSimilarityMeasure()).measure(union1, union2);
    double hausMeasure = (new HausdorffSimilarityMeasure()).measure(union1, union2);
    double overallMeasure = SimilarityMeasureCombiner.combine(areaMeasure, hausMeasure);
    
    System.out.println(
    		"Area measure = " + areaMeasure
    		+ "   Hausdorff measure = " + hausMeasure
    		+ "    Overall = " + overallMeasure);
 	 
  	return overallMeasure > minimumMeasure;
  }

  /*
  private void OLDdoTest(String filename, double distanceTolerance) 
  throws IOException, ParseException
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List geoms = fileRdr.read();
    
    System.out.println("Computing Iterated union");
    Geometry union1 = unionIterated(geoms);
    System.out.println("Computing Cascaded union");
    Geometry union2 = unionCascaded(geoms);
    
    System.out.println("Testing similarity with tolerance = " + distanceTolerance);
    boolean isSameWithinTolerance =  SimilarityValidator.isSimilar(union1, union2, distanceTolerance);
    
 	
  	assertTrue(isSameWithinTolerance);
  }
*/
  
  public Geometry unionIterated(Collection geoms)
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

  public Geometry unionCascaded(Collection geoms)
  {
  	return CascadedPolygonUnion.union(geoms);
  }

}
