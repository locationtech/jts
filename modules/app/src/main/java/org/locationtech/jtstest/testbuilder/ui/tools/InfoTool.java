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

import java.awt.event.MouseEvent;

import org.locationtech.jtstest.testbuilder.JTSTestBuilder;


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
    JTSTestBuilder.controller().displayInfo(toModelCoordinate(e.getPoint()));
  }

}
