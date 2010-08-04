package test.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jtstest.testbuilder.topostretch.*;

public class TestTopologyStretcher 
{
	
	WKTReader reader = new WKTReader();
	
  public static void main(String args[]) {
  	try {
  		(new TestTopologyStretcher()).run();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  public TestTopologyStretcher()
  {
  	
  }
  
  String TRI = "POLYGON ((0 0, 100 0.0000001, 100 0, 0 0))";
  String BOWTIE = "POLYGON ((0 0, 0 150, 100 100.000001, 160 150, 160 0, 100 100, 0 0))";
  
  String PRIM = "POLYGON ((1355.2556682087008539 2641.0482955551756277, 2675.2549625870647105 2642.4131547503075126, 2695.5193111868375127 3962.2575986814194948, 1375.5200168084738834 3960.8927394862880647, 1355.2556682087008539 2641.0482955551756277))"; 
  String NEG = "POLYGON ((2030.4535768477128386 3631.6140581010754431, 2035.5196639976556980 3961.5751690838537797, 2365.5194875922470601 3961.9163838826366373, 2360.4534004423039733 3631.9552728998587554, 2030.4535768477128386 3631.6140581010754431)) ";
  
  String PN_DIFF = "POLYGON (( 2035.5196639976557 3961.575169083854, 2695.5193111868375 3962.2575986814195, 2675.2549625870647 2642.4131547503075, 1355.2556682087009 2641.0482955551756, 1375.5200168084739 3960.892739486288, 2035.5196639976557 3961.575169083854 ), ( 2035.5196639976557 3961.575169083854, 2030.4535768477128 3631.6140581010754, 2360.453400442304 3631.9552728998588, 2365.519487592247 3961.9163838826366, 2035.5196639976557 3961.575169083854 ))";
  		
  void run() throws Exception {
//		run(TRI);
		//run(BOWTIE);
  	
  	run(PN_DIFF, 10.0);
  //	run(PRIM, NEG);
	}
  
  void run(String wkt, double offset) throws Exception {
		Geometry g = reader.read(wkt);
		TopologyStretcher stretcher = new TopologyStretcher(g);

		Geometry[] strGeoms = stretcher.stretch(1e-3, offset);
		Geometry strGeom = strGeoms[0];
		System.out.println(strGeom);
	}

	void run(String wkt1, String wkt2) throws Exception {
		Geometry g1 = reader.read(wkt1);
		Geometry g2 = reader.read(wkt2);
		TopologyStretcher stretcher = new TopologyStretcher(g1, g2);

		Geometry[] strGeoms = stretcher.stretch(1e-3, 10.0);
		Geometry strGeom1 = (Geometry) strGeoms[0];
		System.out.println(strGeom1);
		Geometry strGeom2 = (Geometry) strGeoms[1];
		System.out.println(strGeom2);
	}
}
