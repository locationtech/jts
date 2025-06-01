/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;

import org.locationtech.jtstest.testbuilder.JTSTestBuilder;

/**
 * Extracts a component of a geometry to a new Test Case
 * @version 1.7
 */
public class ExtractComponentTool extends BoxBandTool {
  private static ExtractComponentTool singleton = null;

  public static ExtractComponentTool getInstance() {
    if (singleton == null)
      singleton = new ExtractComponentTool();
    return singleton;
  }

  private ExtractComponentTool() {
    super(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  protected void gestureFinished() 
  {  
    boolean isSegments = isControlKeyDown();
    JTSTestBuilder.controller().copyElementsToTestCase(getBox(), isSegments);
  }

}
