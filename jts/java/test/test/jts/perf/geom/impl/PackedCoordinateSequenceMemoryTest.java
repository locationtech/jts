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
