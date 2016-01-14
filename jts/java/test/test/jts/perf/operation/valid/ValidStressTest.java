package test.jts.perf.operation.valid;

import java.io.PrintStream;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;
import test.jts.util.IOUtil;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jtstest.function.FunctionsUtil;

/**
 * Stress-tests {@link IsValidOp} 
 * on invalid polygons with many intersections.
 * 
 * @author mdavis
 *
 */
public class ValidStressTest  
{
  public static void main(String args[]) {
	  (new ValidStressTest()).run();
  }

  public ValidStressTest() {
  }

  public static int SIZE = 10000;
  
  public void run()
  {
	  Envelope env =  new Envelope(0,100,0,100);
	Polygon comb1 = comb(env, SIZE);
	Coordinate centre = env.centre();
	AffineTransformation trans = AffineTransformation.rotationInstance(0.5 * Math.PI, centre.x, centre.y);
	Polygon comb2 = (Polygon) trans.transform(comb1);    
	MultiPolygon mp = geomFact.createMultiPolygon(new Polygon[] { comb1, comb2 } );
	//System.out.println(mp);
	boolean isValid = mp.isValid();
	System.out.println("Is Valid = " + isValid);
  }
  
  static GeometryFactory geomFact = new GeometryFactory();

  static Polygon comb(Envelope env, int nArms)
  {	
	int npts = 4 * (nArms - 1) + 2 + 2 + 1;
	Coordinate[] pts = new Coordinate[npts];
	double armWidth = env.getWidth() / (2 * nArms - 1);
	double armLen = env.getHeight() - armWidth;
	
	double xBase = env.getMinX();
	double yBase = env.getMinY();
	
	int ipts = 0;
	for (int i = 0; i < nArms; i++) {
		double x1 = xBase + i * 2 * armWidth;
		double y1 = yBase + armLen + armWidth;
		pts[ipts++] = new Coordinate(x1, y1);
		pts[ipts++] = new Coordinate(x1 + armWidth, y1);
		if (i < nArms - 1) {
			pts[ipts++] = new Coordinate(x1 + armWidth, yBase + armWidth);
			pts[ipts++] = new Coordinate(x1 + 2 * armWidth, yBase + armWidth);
		}
	}
	pts[ipts++] = new Coordinate(env.getMaxX(), yBase);
	pts[ipts++] = new Coordinate(xBase, yBase);
	pts[ipts++] = new Coordinate(pts[0]);
	
	return geomFact.createPolygon(pts);
  }

}
