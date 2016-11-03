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
