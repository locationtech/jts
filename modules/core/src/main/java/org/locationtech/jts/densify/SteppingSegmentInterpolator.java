/*
 * Copyright (c) 2016 Vivid Solutions.
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

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Interpolator which generates points using a function of segFract, where
 * segFract is evenly stepped along the length of the segment.
 * 
 * @author Kevin Smith
 */
public abstract class SteppingSegmentInterpolator implements SegmentInterpolator {
	@Override
	public Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i, double distanceTolerance) {
		return new Iterator<Coordinate>() {
			int j = 1;
			double len = seg.getLength();
			int densifiedSegCount = (int) Math.ceil(len / distanceTolerance);
			double densifiedSegLen = len / densifiedSegCount;

			@Override
			public boolean hasNext() {
				return j < densifiedSegCount;
			}

			@Override
			public Coordinate next() {
				double segFract = (j * densifiedSegLen) / len;
				j++;
				return pointAlong(seg, segFract);
			}

		};

	}

	/**
	 * Generate a point a given proportion of the way along a segment.
	 * @param seg      A segment to interpolate along
	 * @param segFract A value between 0 and 1 representing a position along the
	 *                 segment
	 * @return A point along the segment
	 */
	public abstract Coordinate pointAlong(LineSegment seg, double segFract);

	/**
	 * Fill in the z value of a point along a segment if the both endpoints have z set.
	 * 
	 * @param seg
	 * @param segFract
	 * @param p
	 */
	protected final void fillZ(LineSegment seg, double segFract, Coordinate p) {
		if (!Double.isNaN(seg.p0.z) && !Double.isNaN(seg.p1.z)) {
			p.setZ(seg.p0.z + segFract * (seg.p1.z - seg.p0.z));
		}
	}
}