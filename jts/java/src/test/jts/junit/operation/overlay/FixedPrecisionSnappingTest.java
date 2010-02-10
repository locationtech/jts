package test.jts.junit.operation.overlay;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import junit.framework.TestCase;

public class FixedPrecisionSnappingTest extends TestCase 
{
	PrecisionModel pm = new PrecisionModel(1.0);
	GeometryFactory fact = new GeometryFactory(pm);
	WKTReader rdr = new WKTReader(fact);
	
	public FixedPrecisionSnappingTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(FixedPrecisionSnappingTest.class);
	}

	public void testTriangles()
		throws ParseException
	{
		Geometry a = rdr.read("POLYGON ((545 317, 617 379, 581 321, 545 317))");
		Geometry b = rdr.read("POLYGON ((484 290, 558 359, 543 309, 484 290))");
		a.intersection(b);
	}
}
