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

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import org.locationtech.jts.geom.*;

public abstract class LineBandTool extends IndicatorTool 
{
  private List coordinates = new ArrayList();  // in model space
  protected Coordinate tentativeCoordinate;

  // set this to true if band should be closed
  private boolean closeRing = false;
  private int clickCountToFinish = 2; 
  private boolean drawBandLines = true;
  
  public LineBandTool() {
    super();
  }

  public LineBandTool(Cursor cursor) {
    super(cursor);
  }

  protected void setCloseRing(boolean closeRing) {
    this.closeRing = closeRing;
  }

  protected void setClickCountToFinishGesture(int clickCountToFinish)
  {
  	this.clickCountToFinish = clickCountToFinish;
  }
  
  protected void setDrawBandLines(boolean drawBandLines)
  {
  	this.drawBandLines = drawBandLines;
  }
  
  /**
   * Returns an empty List once the shape is cleared.
   * 
   * @see LineBandTool#clearShape
   */
  public List getCoordinates() {
    return Collections.unmodifiableList(coordinates);
  }

  public Coordinate lastCoordinate()
  {
    if (coordinates.size() <= 0) return null;
    return (Coordinate) coordinates.get(coordinates.size()-1);
  }
  
  public void mouseReleased(MouseEvent e) {
    try {
      // Can't assert that coordinates is not empty at this point
      // because
      // of the following situation: NClickTool, n=1, user
      // double-clicks.
      // Two events are generated: clickCount=1 and clickCount=2.
      // When #mouseReleased is called with the clickCount=1 event,
      // coordinates is not empty. But then #finishGesture is called and
      // the
      // coordinates are cleared. When #mouseReleased is then called
      // with
      // the clickCount=2 event, coordinates is empty! 

      // Even though drawing is done in #mouseLocationChanged, call it
      // here
      // also so that #isGestureInProgress returns true on a mouse
      // click.
      // This is mainly for the benefit of OrCompositeTool, which
      // calls #isGestureInProgress. 
      // Can't do this in #mouseClicked because #finishGesture may be
      // called
      // by #mouseReleased (below), which happens before #mouseClicked,
      // resulting in an IndexOutOfBoundsException in #redrawShape. 
      if (e.getClickCount() == 1) {
        // A double-click will generate two events: one with
        // click-count = 1 and
        // another with click-count = 2. Handle the click-count = 1
        // event and
        // ignore the rest. Otherwise, the following problem can
        // occur:
        // -- A click-count = 1 event is generated; #redrawShape is
        // called
        // -- #isFinishingClick returns true; #finishGesture is called
        // -- #finishGesture clears the points
        // -- A click-count = 2 event is generated; #redrawShape is
        // called.
        // An IndexOutOfBoundsException is thrown because points is
        // empty.
        tentativeCoordinate = toModelSnapped(e.getPoint());
        redrawIndicator();
      }

      super.mouseReleased(e);

      // Check for finish at #mouseReleased rather than #mouseClicked.
      // #mouseReleased is a more general condition, as it applies to
      // both
      // drags and clicks. 
      if (isFinishingRelease(e)) {
        finishGesture();
      }
    } catch (Throwable t) {
    }
  }

  protected void mouseLocationChanged(MouseEvent e) {
    try {
      tentativeCoordinate = toModelSnapped(e.getPoint());
      redrawIndicator();
    } catch (Throwable t) {
    }
  }

  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    mouseLocationChanged(e);
  }

  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);
    mouseLocationChanged(e);
  }

  protected void add(Coordinate c) {
    // don't add repeated coords
    if (coordinates.size() > 0 && c.equals2D((Coordinate) coordinates.get(coordinates.size()-1)))
      return;
    coordinates.add(c);
  }

  public void mousePressed(MouseEvent e) {
    try {
      super.mousePressed(e);

      // Don't add more than one point for double-clicks. A double-click
      // will
      // generate two events: one with click-count = 1 and another with
      // click-count = 2. Handle the click-count = 1 event and ignore
      // the rest.
      if (e.getClickCount() != 1) {
        return;
      }

      add(toModelSnapped(e.getPoint()));
    } catch (Throwable t) {
      //              getPanel().getContext().handleThrowable(t);
    }
  }

  protected Shape getShape() {
    if (coordinates.isEmpty()) {
      return null;
    }
    Point2D firstPoint = toView(
        (Coordinate) coordinates.get(0));
    GeneralPath path = new GeneralPath();
    path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
    if (! drawBandLines)
    	return path;
    
    for (int i = 1; i < coordinates.size(); i++) { 
      Coordinate nextCoordinate = (Coordinate) coordinates.get(i);
      Point2D nextPoint = toView(nextCoordinate);
      path.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
    }
    Point2D tentativePoint = toView(tentativeCoordinate);
    path.lineTo((int) tentativePoint.getX(), (int) tentativePoint.getY());
    // close path (for rings only)
    if (closeRing)
      path.lineTo((int) firstPoint.getX(), (int) firstPoint.getY());

    drawVertices(path);
    
    return path;
  }

  private void drawVertices(GeneralPath path)
  {
    for (int i = 0; i < coordinates.size(); i++) { 
      Coordinate coord = (Coordinate) coordinates.get(i);
      Point2D p = toView(coord);
      path.moveTo((int) p.getX()-2, (int) p.getY()-2);
      path.lineTo((int) p.getX()+2, (int) p.getY()-2);
      path.lineTo((int) p.getX()+2, (int) p.getY()+2);
      path.lineTo((int) p.getX()-2, (int) p.getY()+2);
      path.lineTo((int) p.getX()-2, (int) p.getY()-2);
    }

  }
  
  protected boolean isFinishingRelease(MouseEvent e) {
    return e.getClickCount() == clickCountToFinish;
  }

  protected Coordinate[] toArray(List coordinates) {
    return (Coordinate[]) coordinates.toArray(new Coordinate[] {});
  }

  protected void finishGesture() throws Exception {
    clearIndicator();
    try {
      bandFinished();
    } 
    finally {
      coordinates.clear();
    }
  }

  protected abstract void bandFinished() throws Exception;

}
