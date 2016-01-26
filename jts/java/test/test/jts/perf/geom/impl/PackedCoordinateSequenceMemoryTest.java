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

package test.jts.perf.geom.impl;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.*;
import com.vividsolutions.jts.util.*;

public class PackedCoordinateSequenceMemoryTest 
{

	 public static void main(String args[]) {
		 PackedCoordinateSequenceMemoryTest test = new PackedCoordinateSequenceMemoryTest();
	  	test.run();
	  }

//	 PackedCoordinateSequenceFactory coordSeqFact = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 2);
	 CoordinateArraySequenceFactory coordSeqFact = CoordinateArraySequenceFactory.instance();
	 GeometryFactory geomFact = new GeometryFactory(coordSeqFact);
	 
	 PackedCoordinateSequenceMemoryTest()
	 {
		 
	 }
	 
	 static final int GEOMS = 1000;
	 static final int GEOM_SIZE = 1000;
	 
	 
	 void run()
	 {
		 runToMemoryOverflow();
	 }
	 
	 void runToMemoryOverflow()
	 {
		 List geoms = new ArrayList();
		 while (true) {
			 geoms.add(createGeometry());
			 System.out.println(geoms.size());
		 }
	 }
	 
	 Geometry createGeometry()
	 {
		 GeometricShapeFactory shapeFact = new GeometricShapeFactory(geomFact);
		 shapeFact.setSize(100.0);
		 shapeFact.setNumPoints(GEOM_SIZE);
		 return shapeFact.createCircle();
	 }
}
