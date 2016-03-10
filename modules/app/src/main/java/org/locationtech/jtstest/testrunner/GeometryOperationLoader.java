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