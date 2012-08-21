package test.jts.perf.geom.prep;

import test.jts.perf.ThreadTestCase;
import test.jts.perf.ThreadTestRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.util.SineStarFactory;

/**
 * Tests for race conditons in the PreparedGeometry classes.
 * 
 * @author Martin Davis
 *
 */
public class PreparedGeometryThreadSafeTest extends ThreadTestCase
{
  public static void main(String[] args) {
    ThreadTestRunner.run(new PreparedGeometryThreadSafeTest());
  }

  int nPts = 1000;
  GeometryFactory factory = new GeometryFactory(new PrecisionModel(1.0));
  
  protected PreparedGeometry pg;
  protected Geometry g;

  public PreparedGeometryThreadSafeTest()
  {
    
  }
  
  public void setup()
  {
    Geometry sinePoly = createSineStar(new Coordinate(0, 0), 100000.0, nPts);
    pg = PreparedGeometryFactory.prepare(sinePoly);
    g = createSineStar(new Coordinate(10, 10), 100000.0, 100);
  }
  
  Geometry createSineStar(Coordinate origin, double size, int nPts) {
    SineStarFactory gsf = new SineStarFactory(factory);
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    Geometry poly = gsf.createSineStar();
    return poly;
  }
  
  @Override
  public Runnable getRunnable(final int threadIndex)
  {
    return new Runnable() {

      public void run()
      {
        while (true) {
          System.out.println(threadIndex);
          pg.intersects(g);
        }
      }
    
    };
  }
}
