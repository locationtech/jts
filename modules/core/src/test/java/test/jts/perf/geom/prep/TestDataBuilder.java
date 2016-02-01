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
package test.jts.perf.geom.prep;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.util.GeometricShapeFactory;



public class TestDataBuilder 
{
  private GeometryFactory geomFact = new GeometryFactory();

	private Coordinate origin = new Coordinate(0, 0); 
	private double size = 100.0;
	private int testDim = 1;
	
	public TestDataBuilder()
	{
		
	}
	
	public TestDataBuilder(GeometryFactory geomFact)
	{
		this.geomFact = geomFact;
	}
	
	public void setExtent(Coordinate origin, double size)
	{
		this.origin = origin;
		this.size = size;
	}
	
	public void setTestDimension(int testDim)
	{
		this.testDim = testDim;
	}
	
	public Geometry createCircle(int nPts) {
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		Geometry circle = gsf.createCircle();
		// Polygon gRect = gsf.createRectangle();
		// Geometry g = gRect.getExteriorRing();
		return circle;
	}
  
  public Geometry createSineStar(int nPts) {
		SineStarFactory gsf = new SineStarFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		gsf.setArmLengthRatio(0.1);
		gsf.setNumArms(20);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  public List createTestGeoms(Envelope env, int nItems, double size, int nPts)
  {
    int nCells = (int) Math.sqrt(nItems);

  	List geoms = new ArrayList();
  	double width = env.getWidth();
  	double xInc = width / nCells;
  	double yInc = width / nCells;
  	for (int i = 0; i < nCells; i++) {
    	for (int j = 0; j < nCells; j++) {
    		Coordinate base = new Coordinate(
    				env.getMinX() + i * xInc,
    				env.getMinY() + j * yInc);
    		Geometry line = createLine(base, size, nPts);
    		geoms.add(line);
    	}
  	}
  	return geoms;
  }
  
  Geometry createLine(Coordinate base, double size, int nPts)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Geometry circle = gsf.createCircle();
//    System.out.println(circle);
    return circle.getBoundary();
  }
  

}
