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
package org.locationtech.jtstest.testbuilder;

import java.awt.Color;

import javax.swing.UIManager;

public class AppColors {

  public static final Color BACKGROUND = UIManager.getColor ( "Panel.background" );
  public static final Color BACKGROUND_ERROR = Color.PINK;
  public static final Color TAB_FOCUS = UIManager.getColor ("TabbedPane.highlight" );
  public static final Color GEOM_VIEW_BACKGROUND = Color.white;

}
