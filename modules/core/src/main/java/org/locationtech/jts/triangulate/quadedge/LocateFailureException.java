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
package org.locationtech.jts.triangulate.quadedge;

import org.locationtech.jts.geom.*;

public class LocateFailureException 
	extends RuntimeException 
{
	private static String msgWithSpatial(String msg, LineSegment seg) {
		if (seg != null)
			return msg + " [ " + seg + " ]";
		return msg;
	}

	private LineSegment seg = null;

	public LocateFailureException(String msg) {
		super(msg);
	}

	public LocateFailureException(String msg, LineSegment seg) {
		super(msgWithSpatial(msg, seg));
		this.seg = new LineSegment(seg);
	}

	public LocateFailureException(LineSegment seg) {
		super(
				"Locate failed to converge (at edge: "
						+ seg
						+ ").  Possible causes include invalid Subdivision topology or very close sites");
		this.seg = new LineSegment(seg);
	}

	public LineSegment getSegment() {
		return seg;
	}

}
