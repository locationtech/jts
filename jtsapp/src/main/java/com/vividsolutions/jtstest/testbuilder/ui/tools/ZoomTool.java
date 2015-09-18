

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import com.vividsolutions.jtstest.testbuilder.AppConstants;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;


/**
 * @version 1.7
 */
public class ZoomTool extends BasicTool 
{
  private double zoomFactor = 2;
  private Point zoomBoxStart = null;
  private Point zoomBoxEnd = null;
  
  public ZoomTool(double zoomFactor, Cursor cursor) {
    super(cursor);
    this.zoomFactor = zoomFactor;
  }

  public void mouseClicked(MouseEvent mouseEvent) 
  {
    // determine if zoom in (left) or zoom out (right)
    double realZoomFactor = SwingUtilities.isRightMouseButton(mouseEvent)
         ? (1d / zoomFactor) : zoomFactor;
    panel().zoom(toModel(mouseEvent.getPoint()), realZoomFactor);
  }

  public void mousePressed(MouseEvent e)
  {
  	zoomBoxStart = e.getPoint();
  	zoomBoxEnd= e.getPoint();
  }
  
  public void mouseReleased(MouseEvent e) {
    // don't process this event if the mouse was clicked or dragged a very short
    // distance
    zoomBoxEnd = e.getPoint();
    if (!isSignificantMouseMove())
      return;
    if (e.isControlDown()) {
      Point2D destination = toModel(e.getPoint());
      PanTool.pan(panel(), zoomBoxStart, destination);
      return;
    }
    panel().zoom(toModel(zoomBoxStart), toModel(zoomBoxEnd));
  }
  
  public void mouseDragged(MouseEvent e)
  {
    // if panning don't draw zoom box
    if (e.isControlDown()) return;
    
  	Point currPoint = e.getPoint();
  	Graphics g = panel().getGraphics();
  	g.setColor(AppConstants.BAND_CLR);
  	g.setXORMode(Color.white);
  	// erase old rectangle
  	drawRect(g);

  	// draw new zoom box
  	zoomBoxEnd = currPoint;
  	drawRect(g);
  }
  
  public void mouseWheelMoved(MouseWheelEvent e) {
    int notches = e.getWheelRotation();
    double zoomFactor = Math.abs(notches) * 2;
    if (notches > 0 && zoomFactor > 0) zoomFactor = 1.0 / zoomFactor;
    panel().zoom(toModel(e.getPoint()), zoomFactor);
  }
  
  private static final int MIN_MOVEMENT = 3;
  
  private boolean isSignificantMouseMove()
  {
  	if (Math.abs(zoomBoxStart.x - zoomBoxEnd.x) < MIN_MOVEMENT)
  		return false;
  	if (Math.abs(zoomBoxStart.y - zoomBoxEnd.y) < MIN_MOVEMENT)
  		return false;
  	return true;
  }
  
  public void drawRect(Graphics g)
  {
  	Point base = new Point(Math.min(zoomBoxStart.x, zoomBoxEnd.x),
  			Math.min(zoomBoxStart.y, zoomBoxEnd.y));
  	int width = Math.abs(zoomBoxEnd.x - zoomBoxStart.x);
  	int height = Math.abs(zoomBoxEnd.y - zoomBoxStart.y);
  	g.drawRect(base.x, base.y, width, height);
  }
  
}

