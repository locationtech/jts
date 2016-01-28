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
package org.locationtech.jts.generator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * 
 * Creates a point based on the bounding box. 
 * 
 * This implementation returns the centroid.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class PointGenerator extends GeometryGenerator {

	/**
	 * @see org.locationtech.jts.generator.GeometryGenerator#create()
	 * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
	 */
	public Geometry create() {
		if(geometryFactory == null){
			throw new NullPointerException("GeometryFactory is not declared");
		}
		if(boundingBox == null || boundingBox.isNull()){
			throw new NullPointerException("Bounding Box is not declared");
		}
		
		Point p = geometryFactory.toGeometry(boundingBox).getCentroid();
		geometryFactory.getPrecisionModel().makePrecise(p.getCoordinate());
		return p;
	}

}
