/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.perf.geom.prep;

import java.util.*;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.SineStarFactory;
import com.vividsolutions.jts.util.GeometricShapeFactory;

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
