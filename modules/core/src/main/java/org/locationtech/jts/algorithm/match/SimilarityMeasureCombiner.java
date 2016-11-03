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

package org.locationtech.jts.algorithm.match;

/**
 * Provides methods to mathematically combine {@link SimilarityMeasure} values.
 * 
 * @author Martin Davis
 *
 */
public class SimilarityMeasureCombiner 
{
	public static double combine(double measure1, double measure2)
	{
		return Math.min(measure1, measure2);
	}

}
