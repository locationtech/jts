package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.model.*;

public abstract class BasicTool implements Tool, MouseListener, MouseMotionListener
{
  public static int TOLERANCE_PIXELS = 5;

  private Color bandColor = AppConstants.BAND_CLR;
  
  private Shape lastShapeDrawn;
  private boolean shapeOnScreen = false;
  private Color originalColor;
  private Stroke originalStroke;

  public BasicTool() {
    super();
  }

  /**
   * Important for XOR drawing. Even if #getShape returns null, this method
   * will return true between calls of #redrawShape and #clearShape.
   */
  public boolean isIndicatorVisible() {
    return shapeOnScreen;
  }

  private void setShapeOnScreen(boolean shapeOnScreen) {
    this.shapeOnScreen = shapeOnScreen;
  }

  protected void clearIndicator() {
    clearShape(getGraphics2D());
  }

  private void clearShape(Graphics2D graphics) {
    if (!shapeOnScreen) {
      return;
    }
    drawShapeXOR(graphics, lastShapeDrawn);
    setShapeOnScreen(false);
  }

  private void redrawShape(Graphics2D graphics) throws Exception {
    clearShape(graphics);
    drawShapeXOR(graphics);

    //<<TODO:INVESTIGATE>> Race conditions on the shapeOnScreen field?
    //Might we need synchronization? [Jon Aquino]
    setShapeOnScreen(true);
  }

  protected void drawShapeXOR(Graphics2D g) throws Exception {
    Shape newShape = getShape();
    drawShapeXOR(g, newShape);
    lastShapeDrawn = newShape;
  }

  protected void drawShapeXOR(Graphics2D graphics, Shape shape) {
    setup(graphics);
    try {
      if (shape != null) {
          graphics.draw(shape);
      }
    } 
    finally {
      cleanup(graphics);
    }
  }

  protected void setup(Graphics2D graphics) {
    originalColor = graphics.getColor();
    originalStroke = graphics.getStroke();
    graphics.setColor(bandColor);
    graphics.setXORMode(Color.white);
//    graphics.setStroke(stroke);
  }

  protected void redrawIndicator() 
  {
    try {
      redrawShape(getGraphics2D());
    }
    catch (Exception ex) {
      // no other way to handle exception
      ex.printStackTrace();
    }
  }

  /**
   * Gets the shape for displaying the current state of the action.
   * Subclasses should override.
   * 
   * @return null if nothing should be drawn
   */
  protected Shape getShape()
  {
    return null;
  }

  /*
  protected void setStroke(Stroke stroke) {
    this.stroke = stroke;
  }
*/
  
  protected Graphics2D getGraphics2D() {
    Graphics2D g = (Graphics2D) panel().getGraphics();
    if (g != null) {
      // guard against g == null
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
    }
    return g;
  }

  protected void cleanup(Graphics2D graphics) {
    graphics.setPaintMode();
    graphics.setColor(originalColor);
    graphics.setStroke(originalStroke);
  }

//  protected void gestureFinished() throws Exception;

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {}

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
