/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.geom.prep;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;

import test.jts.perf.ThreadTestCase;
import test.jts.perf.ThreadTestRunner;


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
