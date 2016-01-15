package test.jts.perf.operation.valid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * Stress-tests {@link IsValidOp} 
 * by running it on an invalid MultiPolygon with many intersections.
 * In JTS 1.14 and earlier this takes a very long time to run, 
 * since all intersections are computed before the invalid result is returned. 
 * In fact it is only necessary to detect a single intersection in order
 * to determine invalidity.
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
  
  static GeometryFactory geomFact = new GeometryFactory();
  
  public void run()
  {
	Envelope env =  new Envelope(0,100,0,100);
	MultiPolygon mp = crossingCombs(env);
	//System.out.println(mp);
	boolean isValid = mp.isValid();
	System.out.println("Is Valid = " + isValid);
  }

  private MultiPolygon crossingCombs(Envelope env) {
	Polygon comb1 = comb(env, SIZE);
	Coordinate centre = env.centre();
	AffineTransformation trans = AffineTransformation.rotationInstance(0.5 * Math.PI, centre.x, centre.y);
	Polygon comb2 = (Polygon) trans.transform(comb1);    
	MultiPolygon mp = geomFact.createMultiPolygon(new Polygon[] { comb1, comb2 } );
	return mp;
  }

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
