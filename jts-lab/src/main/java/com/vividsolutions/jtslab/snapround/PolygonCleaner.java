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

package com.vividsolutions.jtslab.snapround;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jtslab.geom.util.GeometryEditorEx;
import com.vividsolutions.jtslab.geom.util.GeometryEditorEx.GeometryEditorOperation;

public class PolygonCleaner implements GeometryEditorOperation {

  public static Geometry clean(Geometry geom) {
    GeometryEditorEx editor = new GeometryEditorEx(new PolygonCleaner());
    return editor.edit(geom);
  }
  
  @Override
  public Geometry edit(Geometry geometry, GeometryFactory targetFactory) {
    if (geometry instanceof Polygonal) {
      return geometry.buffer(0);
    }
    return geometry;
  }

}
