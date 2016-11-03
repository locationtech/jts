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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.model.*;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public abstract class BasicTool implements Tool
{
  protected Cursor cursor = Cursor.getDefaultCursor();

  private PrecisionModel gridPM;

  private GeometryEditPanel panel;
  
  public BasicTool() {
    super();
  }

  public BasicTool(Cursor cursor) {
    super();
    this.cursor = cursor;
  }

  protected Graphics2D getGraphics2D() {
    Graphics2D g = (Graphics2D) panel().getGraphics();
    if (g != null) {
      // guard against g == null
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
    }
    return g;
  }

  public void mouseClicked(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseDragged(MouseEvent e)   {  }
  public void keyPressed(KeyEvent e)  { }
  public void keyReleased(KeyEvent e)  { }
  public void keyTyped(KeyEvent e)  {  }
  public void mouseMoved(MouseEvent e) {  }
  public void mouseWheelMoved(MouseWheelEvent e) {  }
  
  public Cursor getCursor()
  {
    return cursor;
  }

  /**
   * Called when tool is activated.
   * 
   * If subclasses override this method they must call <tt>super.activate()</tt>.
   */
  public void activate(GeometryEditPanel panel) 
  {
    this.panel = panel;
  	gridPM = getViewport().getGridPrecisionModel();
    this.panel.setCursor(getCursor());
    this.panel.addMouseListener(this);
    this.panel.addMouseMotionListener(this);
    this.panel.addMouseWheelListener(this);
  }
 
  public void deactivate() 
  {
    this.panel.removeMouseListener(this);
    this.panel.removeMouseMotionListener(this);
    this.panel.removeMouseWheelListener(this);
  }

  protected GeometryEditPanel panel()
  {
    // this should probably be passed in during setup
    //return JTSTestBuilderFrame.instance().getTestCasePanel().getGeometryEditPanel();
    return panel;
  }
  
  protected GeometryEditModel geomModel()
  {
    // this should probably be passed in during setup
    return JTSTestBuilder.model().getGeometryEditModel();
  }
  
  private Viewport getViewport()
  {
    return panel().getViewport();
  }
  
  Point2D toView(Coordinate modePt)
  {
    return getViewport().toView(modePt);
  }
  
  double toView(double distance)
  {
    return getViewport().toView(distance);
  }
  
  Point2D toModel(java.awt.Point viewPt)
  {
    return getViewport().toModel(viewPt);
  }
  
  Coordinate toModelCoordinate(java.awt.Point viewPt)
  {
    return getViewport().toModelCoordinate(viewPt);
  }
  
  double toModel(double viewDist)
  {
    return viewDist / getViewport().getScale();
  }
  
  double getModelSnapTolerance()
  {
    return toModel(AppConstants.TOLERANCE_PIXELS);
  }
  
  protected Coordinate toModelSnapped(Point2D p)
  {
  	return toModelSnappedIfCloseToViewGrid(p);  
  }
  
  protected Coordinate toModelSnappedToViewGrid(Point2D p)
  {
  	// snap to view grid
  	Coordinate pModel = getViewport().toModelCoordinate(p);
  	gridPM.makePrecise(pModel);
  	return pModel;
  }
  
  protected Coordinate toModelSnappedIfCloseToViewGrid(Point2D p)
  {
  	// snap to view grid if close to view grid point
  	Coordinate pModel = getViewport().toModelCoordinate(p);
  	Coordinate pSnappedModel = new Coordinate(pModel);
  	gridPM.makePrecise(pSnappedModel);
  	
  	double tol = getModelSnapTolerance();
  	if (pModel.distance(pSnappedModel) <= tol)
  		return pSnappedModel;
  	return pModel;
  }
  
  protected double gridSize()
  {
    return getViewport().getGridSizeModel();
  }
  
  /*
  protected Coordinate toModelSnappedToDrawingGrid(Point2D p)
  {
    Point2D pt = panel().snapToGrid(getViewport().toModel(p));
    return new Coordinate(pt.getX(), pt.getY());
  }
  */
}
