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

package org.locationtech.jtstest.testbuilder.model;

public interface GeometryType {
  public final static int WELLKNOWNTEXT = 1;

  public final static int GEOMETRYCOLLECTION = 1;
  public final static int MULTIPOLYGON = 2;
  public final static int MULTILINESTRING = 3;
  public final static int MULTIPOINT = 4;
  public final static int POLYGON = 5;
  public final static int LINESTRING = 6;
  public final static int POINT = 7;

}
