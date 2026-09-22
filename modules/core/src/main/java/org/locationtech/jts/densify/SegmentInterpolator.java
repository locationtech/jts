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

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * A scheme used to generate intermediate points
 * 
 * @author Kevin Smith
 */
@FunctionalInterface
public interface SegmentInterpolator {
	/**
	 * Generate the additional points along the given segment of a coordinate array
	 * 
	 * @param seg               The current segment being interpolated
	 * @param pts               The full sequence of original points
	 * @param i                 The index of the starting element of the current
	 *                          segment in the full sequence
	 * @param distanceTolerance The maximum length allowable between consecutive
	 *                          result points
	 * @return iterator over the intermediate points to divide the given segment
	 */
	Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i, double distanceTolerance);
}