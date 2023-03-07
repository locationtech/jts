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

import javax.swing.ImageIcon;

public class AppIcons {
  public final static ImageIcon APP = load("app-icon.gif");
  
  public final static ImageIcon ADD = load("Plus.png");
  public final static ImageIcon ADD_SMALL = load("Plus_small.png");
  public final static ImageIcon DELETE = load("Delete.png");
  public final static ImageIcon DELETE_SMALL = load("Delete_small.png");
  
  public final static ImageIcon EXECUTE = load("Execute.png");
  public final static ImageIcon SAVE_IMAGE = load("SaveImage.png");
  public final static ImageIcon UNDO = load("Undo.png");
  public final static ImageIcon CLEAR = load("Delete_small.png");
  public final static ImageIcon GEOM_INSPECT = load("InspectGeometry.png");
  public final static ImageIcon GEOM_EXCHANGE = load("ExchangeGeoms.png");
  
  public final static ImageIcon GEOFUNC_BINARY = load("BinaryGeomFunction.png");
  public final static ImageIcon EDIT_GRID = load("DrawingGrid.png");
  
  public final static ImageIcon DOWN = load("Down.png");
  public final static ImageIcon UP = load("Up.png");
  public final static ImageIcon LEFT = load("Left.png");
  public final static ImageIcon RIGHT = load("Right.png");
  
  public final static ImageIcon ZOOM = load("Magnify.png");
  public final static ImageIcon COPY_TO_TEST = load("CopyToTest.png");
  public final static ImageIcon COPY = load("Copy.png");
  public final static ImageIcon PASTE = load("Paste.png");
  public final static ImageIcon CUT = load("Delete_small.png");
  public final static ImageIcon GEOM_LOAD = load("LoadWKTToTest.png");
  public final static ImageIcon MOVE = load("Move.png");
  
  public final static ImageIcon ICON_COLLECTION 	= load("Icon_GeomCollection.png");
  public final static ImageIcon ICON_COLLECTION_B 	= load("Icon_GeomCollection_B.png");
  public final static ImageIcon ICON_LINEARRING 	= load("Icon_LinearRing.png");
  public final static ImageIcon ICON_LINEARRING_B 	= load("Icon_LinearRing_B.png");
  public final static ImageIcon ICON_LINESTRING 	= load("Icon_LineString.png");
  public final static ImageIcon ICON_LINESTRING_B 	= load("Icon_LineString_B.png");
  public final static ImageIcon ICON_POINT 		= load("Icon_Point.png");
  public final static ImageIcon ICON_POINT_B 	= load("Icon_Point_B.png");
  public final static ImageIcon ICON_POLYGON 	= load("Icon_Polygon.png");
  public final static ImageIcon ICON_POLYGON_B 	= load("Icon_Polygon_B.png");
  
  public static ImageIcon load(String filename) {
    return new ImageIcon(AppIcons.class.getResource(filename));
  }
}
