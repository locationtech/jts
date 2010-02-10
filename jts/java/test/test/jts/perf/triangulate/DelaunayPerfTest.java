package test.jts.perf.triangulate;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.triangulate.*;

public class DelaunayPerfTest 
{
  public static void main(String args[]) {
  	DelaunayPerfTest test = new DelaunayPerfTest();
  	test.run();
  }
  
	public void run()
	{
		run(10);
		run(10);
		run(100);
		run(1000);
		run(10000);
		run(20000);
		run(30000);
		run(100000);
		run(200000);
		run(300000);
		run(1000000);
//	run(2000000);
	run(3000000);
	}
	
	final static GeometryFactory geomFact = new GeometryFactory();
	
	final static double SIDE_LEN = 10.0;
	
	public void run(int nPts)
	{
		List pts = randomPoints(nPts);
		System.out.println("# pts: " + pts.size());
		Stopwatch sw = new Stopwatch();
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
		builder.setSites(pts);
		
//		Geometry g = builder.getEdges(geomFact);
		// don't actually form output geometry, to save time and memory
		builder.getSubdivision();
		
		System.out.println("  --  Time: " + sw.getTimeString()
				+ "  Mem: " + Memory.usedTotalString());
//		System.out.println(g);
	}
	
	List randomPointsInGrid(int nPts)
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
	
	List randomPoints(int nPts)
	{
		List pts = new ArrayList();
		
		for (int i = 0; i < nPts; i++) {
				double x = SIDE_LEN * Math.random();
				double y = SIDE_LEN * Math.random();
				pts.add(new Coordinate(x, y));
		}
		return pts;
	}
}
