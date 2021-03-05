/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.util;

import java.awt.Color;
import java.lang.reflect.Method;

import org.locationtech.jts.geom.Geometry;

/**
 * A proxy to call TestBuilder functions dynamically.
 * If TestBuilder is not present, functions act as a no-op.
 * <p>
 * This class is somewhat experimental at the moment, so
 * is not recommended for production use.
 * 
 * @author Martin Davis
 *
 */
public class TestBuilderProxy {
  
  private static final String CLASS_FUNCTIONS_UTIL = "org.locationtech.jtstest.function.FunctionsUtil";
  private static Class<?> tbClass;
  private static Method methodShowIndicator;
  private static Method methodShowIndicatorLine;

  private static void init() {
    if (tbClass != null) return;
    try {
      tbClass = TestBuilderProxy.class.getClassLoader().loadClass(CLASS_FUNCTIONS_UTIL);
      methodShowIndicator = tbClass.getMethod("showIndicator", Geometry.class);
      methodShowIndicatorLine = tbClass.getMethod("showIndicator", Geometry.class, Color.class);
    }
    catch (Exception ex) {
      // Fail silently to avoid unexpected output in production
      //System.err.println("TestBuilderProxy: Can't init");
    }
  }

  /**
   * Tests whether the proxy is active (i.e. the TestBuilder is available).
   * This allows avoiding expensive geometry materialization if not needed.
   * 
   * @return true if the proxy is active
   */
  public static boolean isActive() {
    init();
    return tbClass != null;
  }
  
  // TODO: expose an option in the TestBuilder to make this inactive
  // This will avoid a huge performance hit if the visualization is not needed

  public static void showIndicator(Geometry geom) {
    init();
    if (methodShowIndicator == null) return;
    
    try {
      methodShowIndicator.invoke(null, geom);
    } catch (Exception e) {
      // Fail silently to avoid unexpected output in production
      // Or perhaps should fail noisy, since at this point the function should be working?
    }
  }
  public static void showIndicator(Geometry geom, Color lineClr) {
    init();
    if (methodShowIndicatorLine == null) return;
    
    try {
      methodShowIndicatorLine.invoke(null, geom, lineClr);
    } catch (Exception e) {
      // Fail silently to avoid unexpected output in production
      // Or perhaps should fail noisy, since at this point the function should be working?
    }
  }
}
