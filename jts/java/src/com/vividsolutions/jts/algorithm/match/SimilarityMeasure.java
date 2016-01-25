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

package com.vividsolutions.jts.algorithm.match;

import com.vividsolutions.jts.geom.*;

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
