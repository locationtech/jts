package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import com.vividsolutions.jts.awt.FontGlyphReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.model.*;

public abstract class BasicTool implements Tool
{
  public static int TOLERANCE_PIXELS = 5;

  public BasicTool() {
    super();
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

//  protected void gestureFinished() throws Exception;

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) 
  {
  }

  public void mouseMoved(MouseEvent e) {
  }

  public Cursor getCursor()
  {
    return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  }

  public void activate() { }
  
  protected GeometryEditPanel panel()
  {
    // this should probably be passed in during setup
    return JTSTestBuilderFrame.instance().getTestCasePanel().getGeometryEditPanel();
  }
  
  protected GeometryEditModel geomModel()
  {
    // this should probably be passed in during setup
    return JTSTestBuilder.model().getGeometryEditModel();
  }
  
  protected TestBuilderModel testBuilderModel()
  {
    // this should probably be passed in during setup
    return JTSTestBuilder.model();
  }
  
  protected Viewport getViewport()
  {
    return panel().getViewport();
  }
  
  Point2D toView(Coordinate modePt)
  {
    return getViewport().toView(modePt);
  }
  
  double toView(double distance)
  {
    return getViewport().getDistanceInView(distance);
  }
  
  Point2D toModel(java.awt.Point viewPt)
  {
    return getViewport().toModel(viewPt);
  }
  
  Coordinate toModelCoordinate(java.awt.Point viewPt)
  {
    return getViewport().toModelCoordinate(viewPt);
  }
  
  double toModelDistance(double viewDist)
  {
    return viewDist / getViewport().getScale();
  }
  
  double getModelTolerance()
  {
    return toModelDistance(TOLERANCE_PIXELS);
  }
  
  protected Coordinate snapInModel(Point2D p)
  {
    Point2D pt = panel().snapToGrid(getViewport().toModel(p));
    return new Coordinate(pt.getX(), pt.getY());
  }
}
