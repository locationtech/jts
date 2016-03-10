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

package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Envelope;

public class EnvelopeUtil 
{
	public static double minExtent(Envelope env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w < h) return w;
		return h;
	}
	public static double maxExtent(Envelope env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w > h) return w;
		return h;
	}
}
