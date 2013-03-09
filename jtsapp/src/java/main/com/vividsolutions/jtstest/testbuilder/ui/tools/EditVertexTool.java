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
import java.awt.geom.*;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.vividsolutions.jts.awt.GeometryCollectionShape;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.AppCursors;
import com.vividsolutions.jtstest.testbuilder.IconLoader;
import com.vividsolutions.jtstest.testbuilder.geom.*;

/**
 * @version 1.7
 */
public class EditVertexTool 
extends IndicatorTool 
{
  private static EditVertexTool instance = null;

  //Point2D currentIndicatorLoc = null;
  Coordinate currentVertexLoc = null;
  
  private Coordinate selectedVertexLocation = null;
  private Coordinate[] adjVertices = null;

  public static EditVertexTool getInstance() {
    if (instance == null)
      instance = new EditVertexTool();
    return instance;
  }

  private EditVertexTool() {
    super();
    cursor = AppCursors.EDIT_VERTEX;
  }

  public void mousePressed(MouseEvent e) {
  	currentVertexLoc = null;
    if (SwingUtilities.isRightMouseButton(e))
      return;
    
    // initiate moving a vertex
    Coordinate mousePtModel = toModelCoordinate(e.getPoint());
    double tolModel = getModelSnapTolerance();

    selectedVertexLocation = geomModel().locateVertexPt(mousePtModel, tolModel);
    if (selectedVertexLocation != null) {
      adjVertices = geomModel().findAdjacentVertices(selectedVertexLocation);
      currentVertexLoc = selectedVertexLocation;
      redrawIndicator();
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e))
      return;
    
    clearIndicator();
    // finish the move of the vertex
    if (selectedVertexLocation != null) {
      Coordinate newLoc = toModelSnapped(e.getPoint());
      geomModel().moveVertex(selectedVertexLocation, newLoc);
    }
  }

  public void mouseDragged(MouseEvent e) {
  	currentVertexLoc = toModelSnapped(e.getPoint());
    if (selectedVertexLocation != null)
      redrawIndicator();
  }

  public void mouseClicked(MouseEvent e) {
    if (! SwingUtilities.isRightMouseButton(e))
      return;
    
    Coordinate mousePtModel = toModelCoordinate(e.getPoint());
    double tolModel = getModelSnapTolerance();

    boolean isMove = ! e.isControlDown();
    if (isMove) {
      GeometryLocation geomLoc = geomModel().locateNonVertexPoint(mousePtModel, tolModel);
      //System.out.println("Testing: insert vertex at " + geomLoc);
      if (geomLoc != null) {
        geomModel().setGeometry(geomLoc.insert());
      }
    }
    else {  // is a delete
      GeometryLocation geomLoc = geomModel().locateVertex(mousePtModel, tolModel);
      //System.out.println("Testing: delete vertex at " + geomLoc);
      if (geomLoc != null) {
        geomModel().setGeometry(geomLoc.delete());
      }
    }
  }

  protected Shape getShape() 
  {
  	GeometryCollectionShape ind = new GeometryCollectionShape();
  	Point2D currentIndicatorLoc = toView(currentVertexLoc);
  	ind.add(getIndicatorCircle(currentIndicatorLoc));
  	if (adjVertices != null) {
  		for (int i = 0; i < adjVertices.length; i++) {
  	    GeneralPath line = new GeneralPath();
  	    line.moveTo((float) currentIndicatorLoc.getX(), (float) currentIndicatorLoc.getY());
  	    Point2D pt = toView(adjVertices[i]);
  	    line.lineTo((float) pt.getX(), (float) pt.getY());
  	    ind.add(line);
  		}
  	}
  	return ind;
  	
//    return getIndicatorCircle(currentIndicatorLoc);
  }

  private static final double IND_CIRCLE_RADIUS = 10.0;

  protected Shape getIndicatorCircle(Point2D p) {
    return new Ellipse2D.Double(p.getX() - (IND_CIRCLE_RADIUS / 2), p.getY()
        - (IND_CIRCLE_RADIUS / 2), IND_CIRCLE_RADIUS, IND_CIRCLE_RADIUS);
  }

}
