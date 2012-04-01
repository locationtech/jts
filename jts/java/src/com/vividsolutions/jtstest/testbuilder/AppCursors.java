package com.vividsolutions.jtstest.testbuilder;

import java.awt.Cursor;
import java.awt.Toolkit;

public class AppCursors
{
  public static Cursor DRAW_GEOM = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("DrawCursor.png").getImage(), new java.awt.Point(4, 26),
      "Draw");

  public static Cursor EDIT_VERTEX = Toolkit.getDefaultToolkit().createCustomCursor(
      IconLoader.icon("MoveVertexCursor.gif").getImage(),
      new java.awt.Point(16, 16), "MoveVertex");

}
