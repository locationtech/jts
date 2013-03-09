package com.vividsolutions.jts.operation.overlay;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
