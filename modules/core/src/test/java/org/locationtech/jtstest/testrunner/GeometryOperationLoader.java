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

package org.locationtech.jtstest.testrunner;

import org.locationtech.jtstest.geomop.GeometryOperation;

/**
 * Loads a GeometryOperation class
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometryOperationLoader
{
  /**
   * If anything bad happens while creating the geometry operation, just print a message and fail
   * @param classLoader
   * @param geomOpClassname
   */
  public static GeometryOperation createGeometryOperation(ClassLoader classLoader, String geomOpClassname)
  {
    Class geomOpClass = null;
    try {
      geomOpClass = classLoader.loadClass(geomOpClassname);
    }
    catch (ClassNotFoundException ex) {
      System.out.println("ERROR: Class not found - " + geomOpClassname);
      return null;
    }
    try {
      GeometryOperation geometryOp = (GeometryOperation) geomOpClass.newInstance();
      return geometryOp;
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

}
