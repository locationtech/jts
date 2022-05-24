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

import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jtstest.testbuilder.AppCursors;


/**
 * @version 1.7
 */
public class MoveTool 
extends IndicatorTool 
{
  private static MoveTool instance = null;

  Point2D startIndicatorLoc = null;
  Coordinate currentVertexLoc = null;
  
  public static MoveTool getInstance() {
    if (instance == null)
      instance = new MoveTool();
    return instance;
  }

  private MoveTool() {
    super(AppCursors.EDIT_VERTEX);
  }

  public void mousePressed(MouseEvent e) {
    //TODO: only start move if cursor is over geometry
    startIndicatorLoc = e.getPoint();
  	currentVertexLoc = null;
    
    // initiate move
  	currentVertexLoc = toModelCoordinate(e.getPoint());
    redrawIndicator();
  }

  public void mouseReleased(MouseEvent e) {
    clearIndicator();
    // execute the move
    if (startIndicatorLoc != null) {
      Coordinate startLoc = toModelCoordinate((Point) startIndicatorLoc);
      Coordinate newLoc = toModelSnapped(e.getPoint());
      geomModel().moveGeometry(startLoc, newLoc);
    }
  }

  public void mouseDragged(MouseEvent e) {
  	currentVertexLoc = toModelSnapped(e.getPoint());
    if (startIndicatorLoc != null)
      redrawIndicator();
  }

  protected Shape getShape() 
  {
    Point2D currentIndicatorLoc = toView(currentVertexLoc);
    GeneralPath line = new GeneralPath();
    line.moveTo((float) currentIndicatorLoc.getX(), (float) currentIndicatorLoc.getY());
    Point2D pt = startIndicatorLoc;
    line.lineTo((float) pt.getX(), (float) pt.getY());
    return line;
  }

}
