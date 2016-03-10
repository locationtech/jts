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

import java.util.ArrayList;
import java.util.List;


/**
 * Utilities for working with {@link QuadEdge}s.
 * 
 * @author mbdavis
 * 
 */
public class QuadEdgeUtil 
{
	/**
	 * Gets all edges which are incident on the origin of the given edge.
	 * 
	 * @param start
	 *          the edge to start at
	 * @return a List of edges which have their origin at the origin of the given
	 *         edge
	 */
	public static List findEdgesIncidentOnOrigin(QuadEdge start) {
		List incEdge = new ArrayList();

		QuadEdge qe = start;
		do {
			incEdge.add(qe);
			qe = qe.oNext();
		} while (qe != start);

		return incEdge;
	}

}
