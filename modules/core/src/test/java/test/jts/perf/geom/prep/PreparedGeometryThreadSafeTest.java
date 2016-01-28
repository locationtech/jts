/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
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
