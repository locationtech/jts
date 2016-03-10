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
package org.locationtech.jts.triangulate.quadedge;

import org.locationtech.jts.geom.LineSegment;

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
