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

package com.vividsolutions.jtstest.testbuilder.model;

import com.vividsolutions.jts.geom.Geometry;

public class FunctionParameters {

  public static String toString(Object[] param)
  {
  	if (param == null) return "";
  	
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < param.length; i++) {
      if (i > 0) buf.append(", ");
      buf.append(toString(param[i]));
    }
    return buf.toString();
  }

  public static String toString(Object o)
  {
    if (o == null) return "null";
    if (o instanceof Geometry)
      return ((Geometry) o).getGeometryType();
    return o.toString();
  }
}
