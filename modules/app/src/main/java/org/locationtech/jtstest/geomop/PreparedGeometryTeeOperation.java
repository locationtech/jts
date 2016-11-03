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

package org.locationtech.jtstest.geomop;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

public class PreparedGeometryTeeOperation 
	extends TeeGeometryOperation
{
	private static boolean containsProperly(Geometry g1, Geometry g2)
	{
		return g1.relate(g2, "T**FF*FF*");
	}
	
	public PreparedGeometryTeeOperation()
	{
		super();
	}
	
  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   * 
   * @param chainOp the operation to chain to
   */
  public PreparedGeometryTeeOperation(GeometryMethodOperation chainOp)
  {
  	super(chainOp);
  }

	protected void runTeeOp(String opName, Geometry geometry, Object[] args)
	{
		if (args.length < 1) return;
		if (! (args[0] instanceof Geometry)) return;
		Geometry g2 = (Geometry) args[0];
		
		if (! geometry.isValid())
			throw new IllegalStateException("Input geometry A is not valid");
		if (! g2.isValid())
			throw new IllegalStateException("Input geometry B is not valid");
				
		checkAllPrepOps(geometry, g2);
		checkAllPrepOps(g2, geometry);
	}
	
	private void checkAllPrepOps(Geometry g1, Geometry g2)
	{
    PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(g1);

		checkIntersects(prepGeom, g2);
		checkContains(prepGeom, g2);
		checkContainsProperly(prepGeom, g2);
		checkCovers(prepGeom, g2);	
	}
	
	private void checkIntersects(PreparedGeometry pg, Geometry g2)
	{
		boolean pgResult = pg.intersects(g2);
		boolean expected = pg.getGeometry().intersects(g2);
		
		if (pgResult != expected) {
//			pg.intersects(g2);
			throw new IllegalStateException("PreparedGeometry.intersects result does not match expected");
		}
		
//		System.out.println("Results match!");
	}
	
	private void checkContains(PreparedGeometry pg, Geometry g2)
	{
		boolean pgResult = pg.contains(g2);
		boolean expected = pg.getGeometry().contains(g2);
		
		if (pgResult != expected)
			throw new IllegalStateException("PreparedGeometry.contains result does not match expected");
		
//		System.out.println("Results match!");
	}
	
	private void checkContainsProperly(PreparedGeometry pg, Geometry g2)
	{
		boolean pgResult = pg.containsProperly(g2);
		boolean expected = containsProperly(pg.getGeometry(), g2);
		
		if (pgResult != expected)
			throw new IllegalStateException("PreparedGeometry.containsProperly result does not match expected");
		
//		System.out.println("Results match!");
	}
	
	private void checkCovers(PreparedGeometry pg, Geometry g2)
	{
		boolean pgResult = pg.covers(g2);
		boolean expected = pg.getGeometry().covers(g2);
		
		if (pgResult != expected)
			throw new IllegalStateException("PreparedGeometry.covers result does not match expected");
		
//		System.out.println("Results match!");
	}
	
	static class PreparedGeometryOp
	{
		public static boolean intersects(Geometry g1, Geometry g2)
		{
	    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
	    PreparedGeometry prepGeom = pgFact.create(g1);
	    return prepGeom.intersects(g2);
		}
		public static boolean contains(Geometry g1, Geometry g2)
		{
	    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
	    PreparedGeometry prepGeom = pgFact.create(g1);
	    return prepGeom.contains(g2);
		}
		public static boolean covers(Geometry g1, Geometry g2)
		{
	    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
	    PreparedGeometry prepGeom = pgFact.create(g1);
	    return prepGeom.contains(g2);
		}
	}

}
