
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
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
public class OffsetCurveCorrectnessTest 
{

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader rdr = new WKTReader(geometryFactory);

  public static void main(String args[]) {
  	try {
  		(new OffsetCurveCorrectnessTest()).run7();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  public OffsetCurveCorrectnessTest() {  }

  void run7()
  throws Exception
  {
  	// buffer fails
  	String wkt = "MULTILINESTRING ((1335558.59524 631743.01449, 1335572.28215 631775.89056, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335576.62035 631803.90754),  (1335558.59524 631743.01449, 1335573.2578018496 631782.1915185435),  (1335573.2578018496 631782.1915185435, 1335580.70187 631802.08139))";
  	Geometry g = rdr.read(wkt);
  	Geometry curve = bufferOffsetCurve(g, 15);
  	System.out.println(curve);
    //assert(curve.isValid());
  }
  
	public static Geometry bufferOffsetCurve(Geometry g, double distance)	
	{		
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        g.getFactory().getPrecisionModel(),
        new BufferParameters());
    Coordinate[] pts = g.getCoordinates();
    Coordinate[] curvePts = null;
    if (g instanceof Polygonal) {
      curvePts = ocb.getRingCurve(pts, 1, distance);
    }
    else {
      curvePts = ocb.getLineCurve(pts, distance);
    }
    Geometry curve = g.getFactory().createLineString(curvePts);
    return curve;
	}


}
