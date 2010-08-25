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

public abstract class IndicatorTool extends BasicTool
{
  private Color bandColor = AppConstants.BAND_CLR;
  
  private Point mousePoint;
  private Shape lastShapeDrawn;
  private String lastLabelDrawn = null;
  private Point lastLabelLoc = null;

  private boolean isIndicatorVisible = false;
  private Color originalColor;
  private Stroke originalStroke;
  private Font originalFont;

  public IndicatorTool() {
    super();
  }

  /**
   * Important for XOR drawing. Even if #getShape returns null, this method
   * will return true between calls of #redrawShape and #clearShape.
   */
  public boolean isIndicatorVisible() {
    return isIndicatorVisible;
  }

  private void setIndicatorVisible(boolean isIndicatorVisible) {
    this.isIndicatorVisible = isIndicatorVisible;
  }

  protected void clearIndicator() {
    clearShape(getGraphics2D());
  }

  private void clearShape(Graphics2D graphics) {
    if (!isIndicatorVisible) {
      return;
    }
    drawShapeXOR(graphics, lastShapeDrawn, lastLabelDrawn, lastLabelLoc);
    setIndicatorVisible(false);
  }

  private void redrawShape(Graphics2D graphics) throws Exception {
    clearShape(graphics);
    drawShapeXOR(graphics);

    //<<TODO:INVESTIGATE>> Race conditions on the shapeOnScreen field?
    //Might we need synchronization? [Jon Aquino]
    setIndicatorVisible(true);
  }

  private void drawShapeXOR(Graphics2D g) throws Exception {
    Shape newShape = getShape();
    String label = getLabel();
    drawShapeXOR(g, newShape, label, mousePoint);
    lastShapeDrawn = newShape;
    lastLabelDrawn = label;
    lastLabelLoc = mousePoint;
  }

  private void drawShapeXOR(Graphics2D graphics, Shape shape, String label, Point labelLoc) {
    setup(graphics);
    try {
      if (shape != null) {
        graphics.draw(shape);
      }
      /*
      if (label != null)
        graphics.drawString(label, labelLoc.x, labelLoc.y);
*/
    } 
    finally {
      cleanup(graphics);
    }
  }

  private void setup(Graphics2D graphics) {
    originalColor = graphics.getColor();
    originalStroke = graphics.getStroke();
    originalFont = graphics.getFont();
    graphics.setFont(new Font(FontGlyphReader.FONT_SANSERIF, Font.PLAIN, 14));
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
    graphics.setFont(originalFont);
  }

  private void recordLabel(Point p)
  {
    mousePoint = new Point(p.x + 5, p.y);
  }
  
  private String getLabel()
  {
    if (mousePoint == null) return null;
    return mousePoint.x + "," + mousePoint.y;
  }
//  protected void gestureFinished() throws Exception;

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) 
  {
    recordLabel(e.getPoint());    
  }

  public void mouseMoved(MouseEvent e) {
    recordLabel(e.getPoint());
  }


}
