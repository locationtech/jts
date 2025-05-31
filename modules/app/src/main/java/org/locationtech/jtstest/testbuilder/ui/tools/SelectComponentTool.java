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
import java.awt.event.MouseEvent;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;

/**
 * Selects components of a geometry
 * @version 1.7
 */
public class SelectComponentTool extends BoxBandTool {
  private static SelectComponentTool singleton = null;

  public static SelectComponentTool getInstance() {
    if (singleton == null)
      singleton = new SelectComponentTool();
    return singleton;
  }

  private SelectComponentTool() {
    super(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  protected void gestureFinished() 
  {  
    JTSTestBuilder.controller().selectComponents(getBox());
  }
  
  public void mouseClicked(MouseEvent e) {
    Geometry box = getBox(e);
    JTSTestBuilder.controller().selectComponents(box);
  }

  private Geometry getBox(MouseEvent e) {
    Coordinate pt = toModelSnapped(e.getPoint());
    Envelope env = new Envelope(pt);
    Geometry box = JTSTestBuilder.getGeometryFactory().toGeometry(env);
    return box;
  }

}
