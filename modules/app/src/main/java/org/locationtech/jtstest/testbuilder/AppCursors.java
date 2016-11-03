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

package org.locationtech.jtstest.testbuilder;

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
