
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.perf.operation.buffer;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.operation.buffer.*;
/**
 * Tests self-snapping issues
 * 
 * @version 1.7
 */
public class BufferCorrectnessTest 
{

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader rdr = new WKTReader(geometryFactory);

  public static void main(String args[]) {
  	try {
  		(new BufferCorrectnessTest()).run7();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  public BufferCorrectnessTest() {  }

  void run7()
  throws Exception
  {
  	// buffer fails
  	String wkt = "MULTILINESTRING ((1335558.59524 631743.01449, 1335572.28215 631775.89056, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335576.62035 631803.90754),  (1335573.2578018496 631782.1915185435, 1335580.70187 631802.08139))";
  	Geometry g = rdr.read(wkt);
  	Geometry buf = g.buffer(15);
  	System.out.println(buf);
  };
  
  void run6()
  throws Exception
  {
  	// polygon with two vertices very close - mitred negative buffer lies outside input
  	String wkt = "POLYGON ((589081.1515112884 4518509.334764771, 589103.7370954598 4518497.015419995, 589099.8017397423 4518490.719003885, 589097.1198886324 4518486.20858194, 589090.9424687021 4518475.819013388, 589081.1515112884 4518509.334764771))";
  	Geometry g = rdr.read(wkt);
  	
  	BufferParameters params = new BufferParameters(8, BufferParameters.CAP_ROUND, BufferParameters.JOIN_MITRE, 5);
  	Geometry buf = new BufferOp(g, params).getResultGeometry(-5);
    
  	System.out.println(buf);
  };
  
  void run5()
  throws Exception
  {
  	// polygon with two vertices very close - mitred negative buffer lies outside input
  	String wkt = "POLYGON ((588722.7612465625 4518964.956739423, 588755.2073151038 4518948.2420851765, 588750.2892019567 4518938.490656119, 588750.2892047082 4518938.490654858, 588741.1098934844 4518920.290260831, 588722.7612465625 4518964.956739423))";
  	Geometry g = rdr.read(wkt);
  	
  	BufferParameters params = new BufferParameters(8, BufferParameters.CAP_ROUND, BufferParameters.JOIN_MITRE, 5);
  	Geometry buf = new BufferOp(g, params).getResultGeometry(-5);
    
  	System.out.println(buf);
  };
  
  void run4()
  throws Exception
  {
//  	String wkt = "LINESTRING (1872699.676 530329.155, 1872712.232 530255.793, 1872724.601 530183.526, 1872737.157 530110.164, 1872749.713 530036.802, 1872762.082 529964.535, 1872774.638 529891.173, 1872787.194 529817.811, 1872799.563 529745.545, 1872812.119 529672.183, 1872824.675 529598.821, 1872837.044 529526.555, 1872849.6 529453.194, 1872862.156 529379.832, 1872874.524 529307.566, 1872887.08 529234.205, 1872899.636 529160.844, 1872912.005 529088.578, 1872924.561 529015.217, 1872937.117 528941.856, 1872949.486 528869.59, 1872962.042 528796.23)";
//  	String wkt = "LINESTRING(1872762.082 529964.535, 1872774.638 529891.173, 1872787.194 529817.811)";
  	String wkt = "LINESTRING (1872612.157 530840.503, 1872624.713 530767.14, 1872637.269 530693.777)";
  	Geometry g = rdr.read(wkt);
  	
  	BufferParameters params = new BufferParameters(10, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_MITRE, 10);
  	Geometry buf = new BufferOp(g, params).getResultGeometry(200);
    

  	System.out.println(buf);
  };
  
  void run3()
  throws Exception
  {
  	String wkt = "MULTILINESTRING ((1335558.59524 631743.01449, 1335572.28215 631775.89056, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335576.62035 631803.90754), (1335558.59524 631743.01449, 1335573.2578018496 631782.1915185435), (1335573.2578018496 631782.1915185435, 1335580.70187 631802.08139))";
  	Geometry g = rdr.read(wkt);
  	Geometry buf = g.buffer(15);
  	System.out.println(buf);
  };
  
  void run2()
  throws Exception
  {
  	String wkt = "POLYGON ((-2531.310546875 -17.19328498840332, -2518.694580078125 -27.471830368041992, -2564.515869140625 -44.53504943847656, -2531.310546875 -17.19328498840332))";
  	Geometry g = rdr.read(wkt);
  	Geometry buf = g.buffer(1.0, 1);
  	System.out.println(buf);
  }
  
  void run()
  throws Exception
  {
  	doBuffer("LINESTRING (110 320, 280 290, 170 150)", 20.0, -1);
//  	doBuffer("LINESTRING (10 0, 0 0, 10 1)");
  };
  
  void doBuffer(String wkt, double dist)
  throws Exception
  {
  	Geometry g = rdr.read(wkt);
  	Geometry buf = g.buffer(dist, -4);
  	System.out.println(buf);
  };
  
  void doBuffer(String wkt, double dist, int quadSegs)
  throws Exception
  {
  	Geometry g = rdr.read(wkt);
  	Geometry buf = g.buffer(dist, quadSegs);
  	System.out.println(buf);
  };
  
  
}
