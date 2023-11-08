/*
 * Copyright (c) 2017 Jia Yu.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.strtree;

import java.io.Serializable;
import java.util.Comparator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * The Class GeometryDistanceComparator.
 */
public class GeometryDistanceComparator implements Comparator<Geometry>, Serializable{
	
	/** The normal order. */
	boolean normalOrder;

	/** The query center. */
	Point queryCenter;
	
	/**
	 * Instantiates a new Geometry distance comparator.
	 *
	 * @param queryCenter the query center
	 * @param normalOrder The true means puts the least record at the head of this queue. peek() will get the least element. Vice versa.
	 */
	public GeometryDistanceComparator(Point queryCenter, boolean normalOrder)
	{
		this.queryCenter = queryCenter;
		this.normalOrder = normalOrder;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Geometry g1, Geometry g2) {
		double distance1 = g1.getEnvelopeInternal().distance(this.queryCenter.getEnvelopeInternal());
		double distance2 = g2.getEnvelopeInternal().distance(this.queryCenter.getEnvelopeInternal());
		if(this.normalOrder)
		{
			if (distance1 > distance2) {
				return 1;
			} else if (distance1 == distance2) {
				return 0;
			}
			return -1;
		}
		else
		{
			if (distance1 > distance2) {
				return -1;
			} else if (distance1 == distance2) {
				return 0;
			}
			return 1;
		}

	}
}
