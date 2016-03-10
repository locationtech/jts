/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.union;

import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.algorithm.match.AreaSimilarityMeasure;
import org.locationtech.jts.algorithm.match.HausdorffSimilarityMeasure;
import org.locationtech.jts.algorithm.match.SimilarityMeasureCombiner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

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
    //System.out.println("Computing Iterated union");
    Geometry union1 = unionIterated(geoms);
    //System.out.println("Computing Cascaded union");
    Geometry union2 = unionCascaded(geoms);
    
    //System.out.println("Testing similarity with min measure = " + minimumMeasure);
    
    double areaMeasure = (new AreaSimilarityMeasure()).measure(union1, union2);
    double hausMeasure = (new HausdorffSimilarityMeasure()).measure(union1, union2);
    double overallMeasure = SimilarityMeasureCombiner.combine(areaMeasure, hausMeasure);
    
    //System.out.println(
    //		"Area measure = " + areaMeasure
    //		+ "   Hausdorff measure = " + hausMeasure
    //		+ "    Overall = " + overallMeasure);
 	 
  	return overallMeasure > minimumMeasure;
  }

  /*
  private void OLDdoTest(String filename, double distanceTolerance) 
  throws IOException, ParseException
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List geoms = fileRdr.read();
    
    //System.out.println("Computing Iterated union");
    Geometry union1 = unionIterated(geoms);
    //System.out.println("Computing Cascaded union");
    Geometry union2 = unionCascaded(geoms);
    
    //System.out.println("Testing similarity with tolerance = " + distanceTolerance);
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
