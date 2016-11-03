/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.geom.impl;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.util.GeometricShapeFactory;

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
