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
package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.model.DrawingGrid;

public class GridRenderer {
  private static final int MIN_VIEW_GRID_SIZE = 5;

  private static final Color axisColor = Color.gray;

  private static final Color gridColor = Color.lightGray;

  private static final Color gridMajorColor = new Color(240, 240, 240);

  private Viewport viewport;

  private DrawingGrid grid;

  private boolean isEnabled = true;

  public GridRenderer(Viewport viewport, DrawingGrid grid) {
    this.viewport = viewport;
    this.grid = grid;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void paint(Graphics2D g) {
    if (! isEnabled)
      return;
    if (isResolvable())
      drawCells(g);
    drawAxes(g);
    drawScaleMarks(g);
  }

  private boolean isResolvable() {
    Point2D p1 = viewport.toModel(new Point(0, 0));
    Point2D p2 = viewport.toModel(new Point(MIN_VIEW_GRID_SIZE, 0));
    return grid.isResolvable(p1, p2);
  }

  /**
   * No longer used - too slow.
   * 
   * @param g2
   */
  public void drawGridPoints(Graphics2D g2) {
    double gridDX = grid.getGridSize();

    Point2D ptLL = viewport.getLowerLeftCornerInModel();
    Point2D ptUR = viewport.getUpperRightCornerInModel();
    double minx = grid.snapToGrid(ptLL).getX();
    double maxx = grid.snapToGrid(ptUR).getX();
    double miny = grid.snapToGrid(ptLL).getY();
    double maxy = grid.snapToGrid(ptUR).getY();

    // draw grid points
    g2.setColor(gridColor);
    for (double x = minx; x < maxx; x += gridDX) {
      for (double y = miny; y < maxy; y += gridDX) {
        // draw points
        g2.draw(new Line2D.Double(x, y, x, y));
      }
    }

  }

  private static final Coordinate MODEL_ORIGIN = new Coordinate(0, 0);

  public void drawAxes(Graphics2D g) {
    // draw XY axes
    g.setColor(axisColor);

    Point2D viewOrigin = viewport.convert(MODEL_ORIGIN);
    double vOriginX = viewOrigin.getX();
    double vOriginY = viewOrigin.getY();

    if (vOriginX >= 0.0 && vOriginX <= viewport.getWidthInView()) {
      g.draw(new Line2D.Double(vOriginX, 0, vOriginX, viewport
              .getHeightInView()));
    }

    if (vOriginY >= 0.0 && vOriginY <= viewport.getHeightInView()) {
      g.draw(new Line2D.Double(0, vOriginY, viewport.getWidthInView(), vOriginY));
    }
  }

  public void drawCells(Graphics2D g) {
    // draw grid major lines
    g.setColor(gridMajorColor);
    
    double gridSize = grid.getGridSize();
    double gridSizeInView = gridSize * viewport.getScale();
    //System.out.println(gridSizeInView);
    
    
    Point2D ptLL = viewport.getLowerLeftCornerInModel();

    double minx = grid.snapToMajorGrid(ptLL).getX();
    double miny = grid.snapToMajorGrid(ptLL).getY();

    double viewWidth = viewport.getWidthInView();
    double viewHeight = viewport.getHeightInView();
    
    Point2D minPtView = viewport.convert(new Coordinate(minx, miny));

    /**
     * Can't draw right to edges of panel, because
     * Swing inset border occupies that space.
     */
    for (double x = minPtView.getX(); x < viewWidth; x += gridSizeInView) {
      if (x < 2) continue;
      g.draw(new Line2D.Double(x, 0, x, viewHeight - 0));
    }
    for (double y = minPtView.getY(); y > 0; y -= gridSizeInView) {
      if (y < 2) continue;
      g.draw(new Line2D.Double(0, y, viewWidth - 0, y));
    }
  }
  
  private int visibleMagnitude()
  {
  	double viewSizeInModel = Math.max(viewport.getHeightInModel(), viewport.getWidthInModel());
  	// if input is bogus then just return something reasonable
  	if (viewSizeInModel <= 0.0)
  		return 1;
  	double log10 = Math.log10(viewSizeInModel);
  	return (int) log10;
  }
  
  private static final int TICK_LEN = 5;
  private static final int SCALE_TEXT_OFFSET_X = 40;
  private static final int SCALE_TEXT_OFFSET_Y = 2;
  
  public void drawScaleMarks(Graphics2D g) 
  {
  	Envelope viewEnv = viewport.getViewEnv();
  	
  	int viewMag = visibleMagnitude();
  	double tickIncModel = Math.pow(10.0, viewMag);
  	double tickIncView = viewport.getDistanceInView(tickIncModel);
  	
  	// ensure at least 3 ticks are shown
  	if (3 * tickIncView > viewEnv.maxExtent()) {
  		tickIncView /= 10.0;
  		viewMag -= 1;
  	}
  	
    g.setColor(Color.BLACK);
  	
    // X axis
  	double tickX = viewport.getWidthInView() - tickIncView;
  	int viewHeight = (int) viewport.getHeightInView();
  	while (tickX > 0) {
  		g.draw(new Line2D.Double(tickX, viewHeight + 1, tickX, viewHeight - TICK_LEN));
  		tickX -= tickIncView;
  	}
  	
  	// Y axis
  	double tickY = viewport.getHeightInView() - tickIncView;
  	int viewWidth = (int) viewport.getWidthInView();
  	while (tickY > 0) {
  		g.draw(new Line2D.Double(viewWidth + 1, tickY, viewWidth - TICK_LEN, tickY));
  		tickY -= tickIncView;
  	}
  	
  	// Draw Scale magnitude
  	g.drawString("10", viewWidth - 35, viewHeight - 1);
  	g.drawString(viewMag+"", viewWidth - 20, viewHeight - 8);
  }


}
