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
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.event.MouseEvent;

import com.vividsolutions.jtstest.*;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;

/**
 * @version 1.7
 */
public class InfoTool extends BasicTool {
  private static InfoTool singleton = null;

  public static InfoTool getInstance() {
    if (singleton == null)
      singleton = new InfoTool();
    return singleton;
  }

  private InfoTool() {
  }

  public void mousePressed(MouseEvent e) 
  {
    JTSTestBuilderFrame.instance().displayInfo(toModelCoordinate(e.getPoint()));
  }

}
