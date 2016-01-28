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
package org.locationtech.jts.awt;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.*;

/**
 * Transforms a geometry {@link Coordinate} into a Java2D {@link Point},
 * possibly with a mathematical transformation of the ordinate values.
 * Transformation from a model coordinate system to a view coordinate system 
 * can be efficiently performed by supplying an appropriate transformation.
 * 
 * @author Martin Davis
 */
public interface PointTransformation {
	/**
	 * Transforms a {@link Coordinate} into a Java2D {@link Point}.
	 * 
	 * @param src the source Coordinate 
	 * @param dest the destination Point
	 */
  public void transform(Coordinate src, Point2D dest);
}