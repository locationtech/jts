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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

import org.locationtech.jts.awt.GeometryCollectionShape;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jtstest.testbuilder.AppCursors;
import org.locationtech.jtstest.testbuilder.geom.GeometryComponentTransformer;
import org.locationtech.jtstest.testbuilder.geom.GeometryLocation;


/**
 * @version 1.7
 */
public class MoveTool 
extends IndicatorTool 
{
  private static MoveTool instance = null;

  private Point2D startIndicatorLoc = null;
  private Coordinate currentVertexLoc = null;
  private Geometry targetComp;
  
  public static MoveTool getInstance() {
    if (instance == null)
      instance = new MoveTool();
    return instance;
  }

  private MoveTool() {
    super(AppCursors.EDIT_VERTEX);
  }

  private Geometry getComponent(Coordinate pt, double tolerance) {
    List<GeometryLocation> geoms = geomModel().getElements(pt, tolerance);
    if (geoms.size() <= 0) return null;
    return geoms.get(0).getElement();
  }
  
  public void mousePressed(MouseEvent e) {
    startIndicatorLoc = null;
    //TODO: only start move if cursor is over geometry
    Coordinate mousePtModel = toModelCoordinate(e.getPoint());
    double tolModel = getModelSnapTolerance();
    Geometry comp = getComponent(mousePtModel, tolModel);
    if (comp == null) {
      return;
    }
    //-- Ctl-Drag -> use component, otherwise use entire geom
    targetComp = e.isControlDown() ? comp : null;
    
    //-- over a geom - start gesture
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
      execute(startLoc, newLoc, e.isControlDown());
    }
    startIndicatorLoc = null;
  }

  private void execute(Coordinate fromLoc, Coordinate toLoc, boolean isComponentMoved) {
    double dx = toLoc.getX() - fromLoc.getX();
    double dy = toLoc.getY() - fromLoc.getY();
    AffineTransformation trans = AffineTransformation.translationInstance(dx, dy);
    Geometry geomTrans = null;
    if (isComponentMoved) {
      geomTrans = GeometryComponentTransformer.transform(geomModel().getGeometry(), targetComp, trans);
    }
    else {
      geomTrans = GeometryComponentTransformer.transform(geomModel().getGeometry(), trans);
    }
    geomModel().setGeometry(geomTrans);
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
    
    GeometryCollectionShape ind = new GeometryCollectionShape();
    ind.add(line);
    
    int dx = (int) (currentIndicatorLoc.getX() - startIndicatorLoc.getX());
    int dy = (int) (currentIndicatorLoc.getY() - startIndicatorLoc.getY());
    Rectangle rect = boxTarget(dx, dy);
    if (rect != null) {
      ind.add(rect);
    }

    return ind;
  }

  private Rectangle boxTarget(int dx, int dy) {
    Envelope env = null;
    if (targetComp != null) {
      env = targetComp.getEnvelopeInternal();
    }
    else if (geomModel().getGeometry() != null) {
      env = geomModel().getGeometry().getEnvelopeInternal();
    }
    if (env == null) return null;
    return box(env, dx, dy);
  }

  
  private Rectangle box(Envelope env, int dx, int dy) {
    Coordinate envLL = new Coordinate(env.getMinX(), env.getMinY());
    Point2D boxLL = toView(envLL);
    Coordinate envUR = new Coordinate(env.getMaxX(), env.getMaxY());
    Point2D boxUR = toView(envUR);
    int width = (int) Math.abs(boxUR.getX() - boxLL.getX());
    int height = (int) Math.abs(boxUR.getY() - boxLL.getY());
    return new Rectangle((int) boxLL.getX() + dx, (int) boxUR.getY() + dy, width, height);
  }
}
