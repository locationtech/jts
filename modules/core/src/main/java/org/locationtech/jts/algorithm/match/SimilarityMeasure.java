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

import org.locationtech.jts.geom.Geometry;

/**
 * An interface for classes which measures the degree of similarity between two {@link Geometry}s.
 * The computed measure lies in the range [0, 1].
 * Higher measures indicate a great degree of similarity.
 * A measure of 1.0 indicates that the input geometries are identical
 * A measure of 0.0 indicates that the geometries
 * have essentially no similarity.
 * The precise definition of "identical" and "no similarity" may depend on the 
 * exact algorithm being used.
 * 
 * @author mbdavis
 *
 */
public interface SimilarityMeasure
{
	
	double measure(Geometry g1, Geometry g2);
}
