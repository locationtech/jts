
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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.operation.buffer.validate.*;
import org.locationtech.jts.util.*;

/**
 * Test buffers generated around set of random linestrings.
 * Intended to stress-test the correctness of buffer generation.
 * The random linestring sets tend to have numerous holes when buffered, 
 * which is a good test.
 * 
 * @version 1.7
 */
public class RandomLineBufferStressTest 
{

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader rdr = new WKTReader(geometryFactory);

  public static void main(String args[]) {
  	try {
  		(new RandomLineBufferStressTest()).run();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  public RandomLineBufferStressTest() {  }

  void run()
  throws Exception
  {
  	while (true) {
  		run(10);
  		run(100);
  		run(200);
  	}
  }
  
  void run(int numPts)
  throws Exception
  {
  	double lineScale = 1.0;
  	
  	Geometry line = RandomOffsetLineStringGenerator.generate(lineScale, numPts, geometryFactory);
  	System.out.println();
  	System.out.println(line);
  	
		runCase(line, 10, lineScale, numPts);
		runCase(line, 1, lineScale, numPts);
		runCase(line, .1, lineScale, numPts);
  }
  
  private int caseCount = 0;
  
  void runCase(Geometry line, double dist, double lineScale, int numPts)
  throws Exception
  {
  	caseCount++;
  	System.out.println("Running case " + caseCount 
  			+ "  (line scale = " + lineScale
  			+ "  buffer dist = " + dist
  			+ "  num pts = " + numPts
  			+ " )");
  	checkBuffer(line, dist);
  }
  
  void checkBuffer(Geometry g, double distance)
  {
  	Geometry buf = g.buffer(distance);
  	String isValidMsg = BufferResultValidator.isValidMsg(g, distance, buf);
  	if (isValidMsg != null) {
  		System.out.println("Input: ");
  		System.out.println(g);
  		System.out.println("Buffer: ");
  		System.out.println(buf);

  		throw new IllegalStateException(isValidMsg);
  	}
  }
  
  
}


