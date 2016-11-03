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

package org.locationtech.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;

import org.locationtech.jtstest.testbuilder.model.GeometryType;


public class RectangleTool
extends BoxBandTool
{
  private static RectangleTool singleton = null;

  public static RectangleTool getInstance() {
      if (singleton == null)
          singleton = new RectangleTool();
      return singleton;
  }

  public RectangleTool() {
    super(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  protected void gestureFinished() 
  {      
    geomModel().setGeometryType(GeometryType.POLYGON);
    geomModel().addComponent(getCoordinates());
    panel().updateGeom();
  }

  
}
