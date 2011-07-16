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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.*;
import java.text.NumberFormat;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.math.MathUtil;
import com.vividsolutions.jtstest.testbuilder.AppConstants;
import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.model.DrawingGrid;
import com.vividsolutions.jtstest.testbuilder.ui.ColorUtil;

public class GridRenderer {
  private static final int MIN_VIEW_GRID_SIZE = 5;


  private Viewport viewport;

  private DrawingGrid grid;

  private boolean isEnabled = true;
  
  private NumberFormat gridSizeFormat;


  public GridRenderer(Viewport viewport, DrawingGrid grid) {
    this.viewport = viewport;
    this.grid = grid;
    gridSizeFormat = NumberFormat.getInstance();
    gridSizeFormat.setGroupingUsed(false);
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void paint(Graphics2D g) {
    if (! isEnabled)
      return;
    try {
    drawAxes(g);
    drawLinedGrid(g);
//    drawDottedGrid(g);
    drawGridSizeLabel(g, viewport.gridMagnitudeModel());
    }
    // guards against crazy data causing problems
    catch (ArithmeticException ex) {
      return;
    }
  }

  private boolean isResolvable() {
    Point2D p1 = viewport.toModel(new Point(0, 0));
    Point2D p2 = viewport.toModel(new Point(MIN_VIEW_GRID_SIZE, 0));
    return grid.isResolvable(p1, p2);
  }

  private static final Coordinate MODEL_ORIGIN = new Coordinate(0, 0);

  private void drawAxes(Graphics2D g) {
    // draw XY axes
    g.setColor(AppConstants.AXIS_CLR);
    g.setStroke(new BasicStroke(AppConstants.AXIS_WIDTH));

    Point2D viewOrigin = viewport.toView(MODEL_ORIGIN);
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

  private int maxVisibleMagnitude()
  {
  	double visibleExtentModel = viewport.getModelEnv().maxExtent();
  	// if input is bogus then just return something reasonable
  	if (visibleExtentModel <= 0.0)
  		return 1;
  	double log10 = MathUtil.log10(visibleExtentModel);
  	return (int) log10;
  }
  
  /*
  private static final int MIN_GRID_PIXELS = 2;
  
  private int gridMagnitudeModel()
  {
  	double pixelSizeModel = viewport.toModel(1);
  	double pixelSizeModelLog = Math.log10(pixelSizeModel);
  	int gridMag = (int) Math.ceil(pixelSizeModelLog);
  	
  	// Check if grid size is too small and if so increase it one magnitude
  	double gridSizeModel = Math.pow(10, gridMag);
  	double gridSizeView = viewport.toView(gridSizeModel);
//  	System.out.println("\ncand gridSizeView= " + gridSizeView);
  	if (gridSizeView <= MIN_GRID_PIXELS )
  		gridMag += 1;
  	
//  	System.out.println("pixelSize= " + pixelSize + "  pixelLog10= " + pixelSizeLog);
  	return gridMag;
  }
  */
  
  private static final int GRID_MAJOR_LINE_CLR = 220;
  private static final int GRID_MINOR_LINE_CLR = 240;
  
  private void drawLinedGrid(Graphics2D g) 
  {
    int gridMagModel = viewport.gridMagnitudeModel();
    double gridSizeModel = Math.pow(10, gridMagModel);
    double gridSizeView = viewport.toView(gridSizeModel);
    Envelope modelEnv = viewport.getModelEnv();
  	
  	//System.out.println("gridSizeView= " + gridSizeView);
    
  	/**
  	 * Major Grid (10x)
  	 */
  	double gridSize10Model = 10 * gridSizeModel;
  	PrecisionModel pmGrid10 = new PrecisionModel(1.0/gridSize10Model);
  	double basex10Model = pmGrid10.makePrecise(modelEnv.getMinX());
  	double basey10Model = pmGrid10.makePrecise(modelEnv.getMinY());
    Point2D basePt10View = viewport.toView(new Coordinate(basex10Model, basey10Model));
  	double gridSize10View = viewport.toView(gridSize10Model);

  	/**
  	 * Major-Major Grid (100x)
  	 */
  	double gridSize100Model = 100 * gridSizeModel;
  	PrecisionModel pmGrid100 = new PrecisionModel(1.0/gridSize100Model);
  	double basex100Model = pmGrid100.makePrecise(modelEnv.getMinX());
  	double basey100Model = pmGrid100.makePrecise(modelEnv.getMinY());
    Point2D basePt100View = viewport.toView(new Coordinate(basex100Model, basey100Model));
  	double gridSize100View = viewport.toView(gridSize100Model);

  	/**
  	 * Minor Grid
  	 * Only display if dots are sparse enough
  	 */
  	if (gridSizeView >= 4) {  	
    	PrecisionModel pmGrid = new PrecisionModel(1.0/gridSizeModel);
    	double basexModel = pmGrid.makePrecise(modelEnv.getMinX());
    	double baseyModel = pmGrid.makePrecise(modelEnv.getMinY());
      Point2D basePtView = viewport.toView(new Coordinate(basexModel, baseyModel));
      	    
	    g.setStroke(new BasicStroke());
      g.setColor(ColorUtil.gray(GRID_MINOR_LINE_CLR));
      drawGridLines(g, basePtView.getX(), basePtView.getY(), gridSizeView);
  	}

    g.setStroke(new BasicStroke());
    g.setColor(ColorUtil.gray(GRID_MAJOR_LINE_CLR));
    drawGridLines(g, basePt10View.getX(), basePt10View.getY(), gridSize10View);

    /**
     * Major-Major Grid (100x)
     */
    Stroke strokeMajor2 = new BasicStroke(1, // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {4, 6}, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeMajor2);
    g.setColor(ColorUtil.gray(100));
    drawGridLines(g, basePt100View.getX(), basePt100View.getY(), gridSize100View);
    
  	/**
  	 * Semi-Major dots (10x + 5) 
  	 */
    float dash10Offset = ((int) basePt10View.getY()) % (int) gridSize10View;
    dash10Offset = (float) (gridSize10View - dash10Offset + gridSize10View/2);

    g.setColor(Color.BLACK);
    Stroke strokeMid = new BasicStroke(1,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {0, (float) gridSize10View/2}, // Dash pattern
        dash10Offset);                   // Dash phase 
    g.setStroke(strokeMid);

    drawGridLines(g, 
    		basePt10View.getX() - gridSize10View/2, 
    		0, 
    		gridSize10View/2);
  }

  private void drawDottedGrid(Graphics2D g) 
  {
    int gridMagModel = viewport.gridMagnitudeModel();
    double gridSizeModel = Math.pow(10, gridMagModel);
    double gridSizeView = viewport.toView(gridSizeModel);
    
    //System.out.println("gridSizeView= " + gridSizeView);
    
    /**
     * Major Grid (10x)
     */
    double gridSize10Model = 10 * gridSizeModel;
    PrecisionModel pmGrid10 = new PrecisionModel(1.0/gridSize10Model);
    Envelope modelEnv = viewport.getModelEnv();
    double basex10Model = pmGrid10.makePrecise(modelEnv.getMinX());
    double basey10Model = pmGrid10.makePrecise(modelEnv.getMinY());
    Point2D basePt10View = viewport.toView(new Coordinate(basex10Model, basey10Model));
    double gridSize10View = viewport.toView(gridSize10Model);

    g.setStroke(new BasicStroke());
    g.setColor(AppConstants.GRID_MAJOR_CLR);
    drawGridLines(g, basePt10View.getX(), basePt10View.getY(), gridSize10View);
    
    /**
     * Major-Major Grid (100x)
     */
    double gridSize100Model = 100 * gridSizeModel;
    PrecisionModel pmGrid100 = new PrecisionModel(1.0/gridSize100Model);
    double basex100Model = pmGrid100.makePrecise(modelEnv.getMinX());
    double basey100Model = pmGrid100.makePrecise(modelEnv.getMinY());
    Point2D basePt100View = viewport.toView(new Coordinate(basex100Model, basey100Model));
    double gridSize100View = viewport.toView(gridSize100Model);

    Stroke strokeMajor2 = new BasicStroke(1,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {4, 6}, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeMajor2);

    g.setColor(AppConstants.GRID_MINOR_CLR);
    drawGridLines(g, basePt100View.getX(), basePt100View.getY(), gridSize100View);
    
    /**
     * Semi-Major grid (10x + 5) 
     */
    /*
    // disabled for now - a bit too many lines
    g.setColor(AppConstants.GRID_MAJOR_CLR);
    Stroke strokeMid = new BasicStroke(1,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {4, 6}, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeMid);

    drawGridLines(g, 
        basePt10View.getX() - gridSize10View/2, 
        basePt10View.getY() + gridSize10View/2, 
        gridSize10View);
*/
    float dash10Offset = ((int) basePt10View.getY()) % (int) gridSize10View;
    dash10Offset = (float) (gridSize10View - dash10Offset + gridSize10View/2);


    /**
     * Minor Grid
     * Only display if dots are sparse enough
     */
    if (gridSizeView >= 4) {    
      PrecisionModel pmGrid = new PrecisionModel(1.0/gridSizeModel);
      double basexModel = pmGrid.makePrecise(modelEnv.getMinX());
      double baseyModel = pmGrid.makePrecise(modelEnv.getMinY());
      Point2D basePtView = viewport.toView(new Coordinate(basexModel, baseyModel));
      float dashOffset = ((int) basePtView.getY()) % (int) gridSizeView;
      dashOffset = (float) gridSizeView - dashOffset;
            
      //System.out.println("dashOffset= " + dashOffset);
      
      Stroke strokeMinor = new BasicStroke(1,                  // Width of stroke
          BasicStroke.CAP_SQUARE,  // End cap style
          BasicStroke.JOIN_MITER, // Join style
          10,                  // Miter limit
          new float[] {0, (float) gridSizeView }, // Dash pattern
          dashOffset);                   // Dash phase 
      g.setStroke(strokeMinor);
      g.setColor(AppConstants.GRID_MINOR_CLR);
      drawGridLines(g, basePtView.getX(), 0, gridSizeView);
    }

    /**
     * Semi-Major dots (10x + 5) 
     */
    g.setColor(Color.BLACK);
    Stroke strokeMid = new BasicStroke(1,                  // Width of stroke
        BasicStroke.CAP_SQUARE,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        new float[] {0, (float) gridSize10View/2}, // Dash pattern
        dash10Offset);                   // Dash phase 
    g.setStroke(strokeMid);

    drawGridLines(g, 
        basePt10View.getX() - gridSize10View/2, 
        0, 
        gridSize10View/2);
  }
  
  private void drawGridSizeLabel(Graphics2D g, int gridMagModel)
  {
    /**
     * Draw grid size text
     */
    g.setColor(Color.BLUE);

  	int viewHeight = (int) viewport.getHeightInView();
  	int viewWidth = (int) viewport.getWidthInView();
  	
  	if (Math.abs(gridMagModel) <= 3) {
  		// display as number
  		double gridSize = Math.pow(10, gridMagModel);
  		g.drawString(gridSizeFormat.format(gridSize), 2, viewHeight - 1);
  	}
  	else {
  		// display as exponent
  		g.drawString("10", 2, viewHeight - 1);
  		g.drawString(gridMagModel + "", 20, viewHeight - 8);
  	}
  }

  private void drawFixedGrid(Graphics2D g) {
    // draw grid major lines
    
    double gridSize = grid.getGridSize();
    double gridSizeInView = gridSize * viewport.getScale();
    //System.out.println(gridSizeInView);
    
    Point2D ptLL = viewport.getLowerLeftCornerInModel();

    double minx = grid.snapToMajorGrid(ptLL).getX();
    double miny = grid.snapToMajorGrid(ptLL).getY();

    Point2D minPtView = viewport.toView(new Coordinate(minx, miny));

    g.setColor(AppConstants.GRID_MAJOR_CLR);
    drawGridLines(g, minPtView.getX(), minPtView.getY(), gridSizeInView);
  }
  
  private void drawGridLines(Graphics2D g, double minx, double maxy, double gridSizeInView)
  {
    double viewWidth = viewport.getWidthInView();
    double viewHeight = viewport.getHeightInView();
    
    //Point2D minPtView = viewport.toView(new Coordinate(minx, miny));


    /**
     * Can't draw right to edges of panel, because
     * Swing inset border occupies that space.
     */
    // draw vertical grid lines
    for (double x = minx; x < viewWidth; x += gridSizeInView) {
    	// don't draw grid line right next to panel border
      if (x < 2) continue;
      g.draw(new Line2D.Double(x, 0, x, viewHeight - 0));
    }
    // skip drawing horizontal grid lines if maxy is invalid
    if (maxy <= 0) return;
    for (double y = maxy; y > 0; y -= gridSizeInView) {
    	// don't draw grid line right next to panel border
      if (y < 2) continue;
      g.draw(new Line2D.Double(0, y, viewWidth - 0, y));
    }
  }
  
  private static final int TICK_LEN = 5;
  private static final int SCALE_TEXT_OFFSET_X = 40;
  private static final int SCALE_TEXT_OFFSET_Y = 2;
  
  /**
   * Not very pleasing
   * 
   * @param g
   */
  private void drawScaleMarks(Graphics2D g) 
  {
  	Envelope viewEnv = viewport.getViewEnv();
  	
  	int viewMag = maxVisibleMagnitude();
  	double gridIncModel = Math.pow(10.0, viewMag);
  	double gridIncView = viewport.toView(gridIncModel);
  	
  	// ensure at least 3 ticks are shown
  	if (3 * gridIncView > viewEnv.maxExtent()) {
  		gridIncView /= 10.0;
  		viewMag -= 1;
  	}
  	
    g.setColor(Color.BLACK);
  	
    // draw X axis ticks
  	double tickX = viewport.getWidthInView() - gridIncView;
  	int viewHeight = (int) viewport.getHeightInView();
  	while (tickX > 0) {
  		g.draw(new Line2D.Double(tickX, viewHeight + 1, tickX, viewHeight - TICK_LEN));
  		tickX -= gridIncView;
  	}
  	
  	// draw Y axis ticks
  	double tickY = viewport.getHeightInView() - gridIncView;
  	int viewWidth = (int) viewport.getWidthInView();
  	while (tickY > 0) {
  		g.draw(new Line2D.Double(viewWidth + 1, tickY, viewWidth - TICK_LEN, tickY));
  		tickY -= gridIncView;
  	}
  	
  	// draw Scale magnitude
  	g.drawString("10", viewWidth - 35, viewHeight - 1);
  	g.drawString(viewMag+"", viewWidth - 20, viewHeight - 8);
  }


}
