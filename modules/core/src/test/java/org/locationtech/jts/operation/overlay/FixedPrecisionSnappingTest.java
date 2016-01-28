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

package org.locationtech.jts.operation.overlay;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

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
