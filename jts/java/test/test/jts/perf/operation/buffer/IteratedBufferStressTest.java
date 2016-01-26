
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

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.buffer.validate.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.*;

/**
 * Test repeated buffering of a given input shape.
 * Intended to test the robustness of buffering.
 * Repeated buffering tends to generate challenging
 * somewhat pathological linework, which stresses the buffer algorithm.
 * 
 * @version 1.7
 */
public class IteratedBufferStressTest 
{

  private PrecisionModel precisionModel = new PrecisionModel();
//  private PrecisionModel precisionModel = new PrecisionModel(1);
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader rdr = new WKTReader(geometryFactory);

  public static void main(String args[]) {
  	try {
  		(new IteratedBufferStressTest()).run();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  String inputWKT = 
  	"POLYGON ((110 320, 190 220, 60 200, 180 120, 120 40, 290 150, 410 40, 410 230, 500 340, 320 310, 260 370, 220 310, 110 320), (220 260, 250 180, 290 220, 360 150, 350 250, 260 280, 220 260))";

  public IteratedBufferStressTest() {  }

  void run()
  throws Exception
  {
  	Stopwatch totalSW = new Stopwatch();
  	Geometry base = rdr.read(inputWKT);
  	double dist = 1.0;
  	while (true) {
  		Geometry b1 = doBuffer(base, dist);
  		Geometry b2 = doBuffer(b1, -dist);
  		dist += 1;
  		base = b2;
  		System.out.println("----------------------  " + totalSW.getTimeString());
  		System.out.println();
  	}
  }
  
  Geometry doBuffer(Geometry g, double dist)
  {
		System.out.println("Buffering with dist = " + dist);
		Geometry buf = g.buffer(dist);
		System.out.println("Buffer result has " + buf.getNumPoints() + " vertices");
		
		System.out.println(buf);
		return buf;

  }
}


