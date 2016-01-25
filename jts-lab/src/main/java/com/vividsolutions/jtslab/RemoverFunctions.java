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

package com.vividsolutions.jtslab;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtslab.clean.SmallHoleRemover;

public class RemoverFunctions {
  public static Geometry removeSmallHoles(Geometry geom, double areaTolerance)
  {
    return SmallHoleRemover.clean(geom, areaTolerance);
  }
  

}
