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

  public static final Color GEOM_A = Color.BLUE;
  public static final Color GEOM_B = Color.RED;
  public static final Color GEOM_RESULT = new Color(100, 150, 0); // YellowGreen

  public static final Color GEOM_A_HIGHLIGHT_CLR = new Color(0, 0, 255);
  public static final Color GEOM_A_LINE_CLR = new Color(0, 0, 255, 150);
  public static final Color GEOM_A_FILL_CLR = new Color(200, 200, 255, 150);
  public static final Color GEOM_A_BAND = Color.cyan;
  
  public static final Color GEOM_B_HIGHLIGHT_CLR = new Color(255, 0, 0);
  public static final Color GEOM_B_LINE_CLR = new Color(150, 0, 0, 150);
  public static final Color GEOM_B_FILL_CLR = new Color(255, 200, 200, 150);
  public static final Color GEOM_B_BAND = Color.pink;
  // YellowGreen
  public static final Color GEOM_RESULT_LINE_CLR = new Color(120, 180, 0, 200);
  // Yellow
  public static final Color GEOM_RESULT_FILL_CLR = new Color(255, 255, 100, 100);
  
  public static final Color GEOM_VIEW_BACKGROUND = Color.white;
  
  public static final Color BACKGROUND_FOCUS = Color.white;
  public static final Color BACKGROUND = UIManager.getColor ( "Panel.background" );
  public static final Color BACKGROUND_ERROR = Color.PINK;
  public static final Color TAB_FOCUS = UIManager.getColor ("TabbedPane.highlight" );
  
  public static final Color GEOM_SELECT_LINE_CLR = new Color(0, 204, 204, 200);
  public static final Color GEOM_SELECT_FILL_CLR = new Color(150, 255, 255, 100);
  
  

}
