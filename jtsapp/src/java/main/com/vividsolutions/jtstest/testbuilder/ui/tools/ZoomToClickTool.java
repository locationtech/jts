

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
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import com.vividsolutions.jtstest.testbuilder.AppConstants;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;


/**
 * @version 1.7
 */
public class ZoomToClickTool extends BasicTool 
{
  private double zoomFactor = 2;
  private Cursor cursor = Cursor.getDefaultCursor();
  private Point zoomBoxStart = null;
  private Point zoomBoxEnd = null;
  
  public ZoomToClickTool() { }

  public ZoomToClickTool(double zoomFactor, Cursor cursor) {
    this();
    this.zoomFactor = zoomFactor;
    this.cursor = cursor;
  }

  public Cursor getCursor() {
    return cursor;
  }

  public void mouseClicked(MouseEvent mouseEvent) 
  {
    // determine if zoom in (left) or zoom out (right)
    double realZoomFactor = SwingUtilities.isRightMouseButton(mouseEvent)
         ? (1d / zoomFactor) : zoomFactor;
    Point center = mouseEvent.getPoint();
    panel().zoom(center, realZoomFactor);
  }

  public void mousePressed(MouseEvent e)
  {
  	zoomBoxStart = e.getPoint();
  	zoomBoxEnd= e.getPoint();
  }
  
  public void mouseReleased(MouseEvent e)
  {
  	// don't process this event if the mouse was clicked or dragged a very short distance
  	if (! isSignificantMouseMove())
  		return;
  	
    // zoom to extent box
  	int centreX = (zoomBoxEnd.x + zoomBoxStart.x) / 2; 
  	int centreY = (zoomBoxEnd.y + zoomBoxStart.y) / 2; 
  	Point centre = new Point(centreX, centreY);
  	
  	int dx = Math.abs(zoomBoxEnd.x - zoomBoxStart.x);
  	int dy = Math.abs(zoomBoxEnd.y - zoomBoxStart.y);
  	// ensure deltas are valid
  	if (dx <= 0) dx = 1;
  	if (dy <= 0) dy = 1;
  	
		GeometryEditPanel panel = panel();
		double widthFactor = panel.getSize().width / dx;
		double heightFactor = panel.getSize().height / dy;
		double zoomFactor = Math.min(widthFactor, heightFactor);

//  	double zoomFactor = 2;
  	panel().zoom(centre, zoomFactor);
  }
  
  public void mouseDragged(MouseEvent e)
  {
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
  
  public void activate() { }
  
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

