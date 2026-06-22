/*
 * Copyright (c) 2026 Kevin Smith.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.densify;

import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Produces points by dividing the segment in two, offsetting perpendicularly by
 * a random amount proportional to the length of the segment and repeating until
 * the threshold is met.
 * <p>
 * The result is a random fractal shape resembling a natural coastline or river. 
 * 
 * @author Kevin Smith
 */
public class FractalSegmentInterpolator extends SubdividingSegmentInterpolator {

	private static final double MIDWAY = 0.5;
	
	/**
	 * Create a FractalSegmentInterpolator
	 * 
	 * @param proportion Maximum offset as a proportion of the segment length
	 * @param rand Random number generator to use
	 */
	public FractalSegmentInterpolator(double proportion, Random rand) {
		this.rand = rand;
		this.proportion = proportion;
	}

	private final Random rand;
	private final double proportion;

	@Override
	public Coordinate midpoint(LineSegment seg) {
		Coordinate p = seg.pointAlongOffset(MIDWAY, seg.getLength() * (rand.nextDouble() * 2 - 1) * proportion);
		fillZ(seg, p);
		return p;
	}

}