package test.jts.perf.triangulate;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.triangulate.*;

public class VoronoiPerfTest 
{
  public static void main(String args[]) {
  	VoronoiPerfTest test = new VoronoiPerfTest();
  	test.run();
  }
  
	public void run()
	{
		run(10);
		run(100);
		run(1000);
		run(10000);
		run(100000);
		run(1000000);
	}
	
	final static GeometryFactory geomFact = new GeometryFactory();
	
	final static double SIDE_LEN = 10.0;
	
	public void run(int nPts)
	{
		List pts = randomPoints(nPts);
		Stopwatch sw = new Stopwatch();
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
		builder.setSites(pts);
		
		Geometry g = builder.getEdges(geomFact);
		System.out.println("# pts: " + pts.size() + "  --  " + sw.getTimeString());
//		System.out.println(g);
	}
	
	List randomPoints(int nPts)
	{
		List pts = new ArrayList();
		
		int nSide = (int) Math.sqrt(nPts) + 1;
		
		for (int i = 0; i < nSide; i++) {
			for (int j = 0; j < nSide; j++) {
				double x = i * SIDE_LEN + SIDE_LEN * Math.random();
				double y = j * SIDE_LEN + SIDE_LEN * Math.random();
				pts.add(new Coordinate(x, y));
			}
		}
		return pts;
	}
}
