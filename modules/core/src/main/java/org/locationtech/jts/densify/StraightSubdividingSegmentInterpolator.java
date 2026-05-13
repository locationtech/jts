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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Produces points dividing the segment into a power of two in a straight line.
 * 
 * @author Kevin Smith
 */
public class StraightSubdividingSegmentInterpolator extends SubdividingSegmentInterpolator {

	@Override
	public Coordinate midpoint(LineSegment seg) {
		Coordinate p = seg.midPoint();
		fillZ(seg, p);
		return p;
	}

}