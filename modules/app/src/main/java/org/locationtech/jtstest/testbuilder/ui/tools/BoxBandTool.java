

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
package org.locationtech.jtstest.testbuilder.ui.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;



/**
 * @version 1.7
 */
public abstract class BoxBandTool extends IndicatorTool 
{
  
  private Point zoomBoxStart = null;
  private Point zoomBoxEnd = null;
  
  public BoxBandTool() { }

  public BoxBandTool(Cursor cursor) {
    super(cursor);
  }

  public void mousePressed(MouseEvent e)
  {
  	zoomBoxStart = e.getPoint();
  	zoomBoxEnd = null;
  }
  
  public void mouseReleased(MouseEvent e)
  {
    clearIndicator();
  	// don't process this event if the mouse was clicked or dragged a very short distance
  	if (! isSignificantMouseMove())
  		return;
  	
    gestureFinished();
  }
  
  public void mouseDragged(MouseEvent e)
  {
    super.mouseDragged(e);
    zoomBoxEnd = e.getPoint();
    redrawIndicator();
  }
  
  protected Shape getShape()
  {
    if (zoomBoxEnd == null) return null;
    
    Envelope envModel = getEnvelope();
    
    Point2D base = toView(new Coordinate(envModel.getMinX(), envModel.getMaxY()));
    double width = toView(envModel.getWidth());
    double height = toView(envModel.getHeight());
    return new Rectangle2D.Double(base.getX(), base.getY(), width, height);
  }

  private static final int MIN_MOVEMENT = 3;
  
  private boolean isSignificantMouseMove()
  {
    if (zoomBoxEnd == null) return false;

    if (Math.abs(zoomBoxStart.x - zoomBoxEnd.x) < MIN_MOVEMENT)
      return false;
    if (Math.abs(zoomBoxStart.y - zoomBoxEnd.y) < MIN_MOVEMENT)
      return false;
    return true;
  }  
  
  /**
   * Gets the envelope of the indicated rectangle,
   * in model coordinates.
   * 
   * @return
   */
  protected Envelope getEnvelope() {
    Coordinate start = toModelSnapped(zoomBoxStart);
    Coordinate end = toModelSnapped(zoomBoxEnd);
    return new Envelope(start, end);
  }

  protected Geometry getBox()
  {
    return JTSTestBuilder.getGeometryFactory().toGeometry(getEnvelope());
  }
  
  /**
   * Getes the coordinates for the rectangle
   * starting with the lower left point.
   * The coordinates are oriented CW.
   * 
   * @return the coordinates for the rectangle
   */
  protected List getCoordinatesOfEnvelope()
  {
    Envelope env = getEnvelope();
    
    List coords = new ArrayList();
    coords.add(new Coordinate(env.getMinX(), env.getMinY()));
    coords.add(new Coordinate(env.getMinX(), env.getMaxY()));
    coords.add(new Coordinate(env.getMaxX(), env.getMaxY()));
    coords.add(new Coordinate(env.getMaxX(), env.getMinY()));
    coords.add(new Coordinate(env.getMinX(), env.getMinY()));
    return coords;
  }
  
  /**
   * Gets the coordinates for the rectangle
   * starting at the first point clicked.
   * The coordinates are oriented CW.
   * 
   * @return the coordinates for the rectangle
   */
  protected List getCoordinates()
  {
    Coordinate start = toModelSnapped(zoomBoxStart);
    Coordinate end = toModelSnapped(zoomBoxEnd);
    
    boolean isCW = (start.x < end.x && start.y < end.y)
    || (start.x > end.x && start.y > end.y);
    
    Coordinate mid1 = new Coordinate(start.x, end.y);
    Coordinate mid2 = new Coordinate(end.x, start.y);
    
    /**
     * Form rectangle starting at start point, 
     * and oriented CW.
     */
    List coords = new ArrayList();
    coords.add(new Coordinate(start));
    if (isCW) 
      coords.add(mid1);
    else 
      coords.add(mid2);
    
    coords.add(new Coordinate(end));
    
    if (isCW) 
      coords.add(mid2);
    else 
      coords.add(mid1);

    coords.add(new Coordinate(start));
    return coords;
  }
  
  
  protected void gestureFinished() 
  {
    // basic tool does nothing.
    // Subclasses should override
  }

}

