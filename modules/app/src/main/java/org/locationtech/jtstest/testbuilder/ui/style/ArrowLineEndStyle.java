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

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class ArrowLineEndStyle 
  extends LineEndStyle {

  private static final int FILL_ALPHA = 150;
  private final static double ANGLE = 18;
  private final static double LENGTH = 15;
  private static final double OFFSET_SIZE = 5;
  private boolean filled = true;

  // default in case colour is not set
  private Color color = Color.RED;

  public ArrowLineEndStyle(Color color, boolean start, boolean filled) {
      super(start);
      setColor(color);
      this.filled = filled;
  }
  public void setColor(Color color) {
    this.color = ColorUtil.setAlpha(color, FILL_ALPHA);
  }

  protected void paint(Point2D terminal, Point2D next, Viewport viewport,
      Graphics2D g) throws NoninvertibleTransformException 
  {
    // can't compute valid arrow for zero-length segments
    if (terminal.equals(next)) {
      return;
    }

    g.setPaint(color);
    GeneralPath arrowhead = arrowheadPath(next, terminal, LENGTH,
        ANGLE, OFFSET_SIZE);
    if (filled) {
      arrowhead.closePath();
      g.fill(arrowhead);
    }
    // draw to get effect of stroke
    g.draw(arrowhead);
  }

  /**
   * @param finLength
   *          required distance from the tip to each fin's tip
   */
  public static GeneralPath arrowheadPath(Point2D p0, Point2D p1, 
      Point2D tipPt,
      double finLength, double finAngle) {
    return arrowheadPath(p0, tipPt, finLength, finLength, 0);
  }
  /**
   * @param finLength
   *          required distance from the tip to each fin's tip
   */
  public static GeneralPath arrowheadPath(Point2D p0, Point2D tipPt,
      double finLength, double finAngle, 
      double offsetSize) {
    GeneralPath arrowhead = new GeneralPath();
    Point2D offset = AWTUtil.vector(p0, tipPt, offsetSize);
    Point2D finTip1 = fin(tipPt, p0, finLength, finAngle);
    Point2D finTip2 = fin(tipPt, p0, finLength, -finAngle);
    arrowhead.moveTo((float) finTip1.getX() - offset.getX(), 
        (float) finTip1.getY() - offset.getY() );
    arrowhead.lineTo((float) tipPt.getX() - offset.getX(), 
        (float) tipPt.getY() - offset.getY());
    arrowhead.lineTo((float) finTip2.getX() - offset.getX(), 
        (float) finTip2.getY() - offset.getY());

    return arrowhead;
  }

  public static Point2D fin(Point2D shaftTip, Point2D shaftTail, double length,
      double angle) {
    double shaftLength = shaftTip.distance(shaftTail);
    Point2D finTail = shaftTip;
    Point2D finTip = AWTUtil.add(AWTUtil.multiply(AWTUtil.subtract(shaftTail,
        shaftTip), length / shaftLength), finTail);
    AffineTransform affineTransform = new AffineTransform();
    affineTransform.rotate((angle * Math.PI) / 180, finTail.getX(), finTail
        .getY());

    return affineTransform.transform(finTip, null);
  }

}
