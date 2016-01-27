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

package com.vividsolutions.jtstest.testbuilder;

import java.awt.Cursor;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

public class AppCursors
{
  public static Cursor DRAW_GEOM = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("DrawCursor.png").getImage(), new java.awt.Point(4, 26),
      "Draw");

  public static Cursor EDIT_VERTEX = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("MoveVertexCursor.gif").getImage(),
      new java.awt.Point(16, 16), "MoveVertex");

  public static Cursor HAND = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("Hand.gif").getImage(), new java.awt.Point(7, 7), "Pan");

  public static Cursor ZOOM = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("MagnifyCursor.gif").getImage(),
      new java.awt.Point(16, 16), "Zoom In");

}
