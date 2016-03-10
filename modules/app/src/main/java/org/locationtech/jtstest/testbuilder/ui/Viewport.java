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

package org.locationtech.jtstest.testbuilder.ui;

import java.awt.*; 
import java.awt.geom.*; 
import java.text.NumberFormat;

import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.math.MathUtil;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;


/**
 * Maintains the information associated with mapping 
 * the model view to the screen
 * 
 * @author Martin Davis
 *
 */
public class Viewport implements PointTransformation
{
  private static double INITIAL_SCALE = 1.0;
  private static int INITIAL_ORIGIN_X = -10;
  private static int INITIAL_ORIGIN_Y = -10;

  private GeometryEditPanel panel;
  
  /**
   * Origin of view in model space
   */
  private Point2D originInModel =
      new Point2D.Double(INITIAL_ORIGIN_X, INITIAL_ORIGIN_Y);

  /**
   * The scale is the factor which model distance 
   * is multiplied by to get view distance
   */
  private double scale = 1;
  private PrecisionModel scalePM = new PrecisionModel(scale);
  private NumberFormat scaleFormat;
  
  private Envelope viewEnvInModel;
  private AffineTransform modelToViewTransform;
  private java.awt.geom.Point2D.Double srcPt = new java.awt.geom.Point2D.Double(0, 0);
  private java.awt.geom.Point2D.Double destPt = new java.awt.geom.Point2D.Double(0, 0);

  private Dimension viewSize;

  public Viewport(GeometryEditPanel panel) {
    this.panel = panel;
    setScaleNoUpdate(1.0);
  }

  private void viewUpdated()
  {
    panel.forceRepaint();
  }
  
  public Envelope getModelEnv()
  {
  	return viewEnvInModel;
  }
  
  public Envelope getViewEnv() {
    return new Envelope(
        0,
        getWidthInView(),
        0,
        getHeightInView());
  }

  public double getScale() {
    return scale;
  }

  private void setScaleNoUpdate(double scale) {
    this.scale = snapScale(scale);
    scalePM = new PrecisionModel(this.scale);   
    
    scaleFormat = NumberFormat.getInstance();
    int fracDigits = (int) (MathUtil.log10(this.scale));
    if (fracDigits < 0) fracDigits = 0;
    //System.out.println("scale = " + this.scale);
    //System.out.println("fracdigits = " + fracDigits);
    scaleFormat.setMaximumFractionDigits(fracDigits);
    // don't show commas
    scaleFormat.setGroupingUsed(false);
  }

  private void setScale(double scale) {
    setScaleNoUpdate(scale);
    update();
  }
  
  private void setOrigin(double viewOriginX, double viewOriginY) {
    this.originInModel = new Point2D.Double(viewOriginX, viewOriginY);
    update();
  }
  
  public NumberFormat getScaleFormat()
  {
    return scaleFormat;
  }
  
  private static final double ROUND_ERROR_REMOVAL = 0.00000001;
  
  /**
     * Snaps scale to nearest multiple of 2, 5 or 10.
     * This ensures that model coordinates entered
     * via the geometry view
     * don't carry more precision than the zoom level warrants.
   * 
   * @param scaleRaw
   * @return
   */
  private static double snapScale(double scaleRaw)
  {
    double scale = snapScaleToSingleDigitPrecision(scaleRaw);
    return scale;
  }
  
  private static double snapScaleToSingleDigitPrecision(double scaleRaw)
  {
    // if the rounding error is not nudged, snapping can "stick" at some values
    double pow10 = Math.floor(MathUtil.log10(scaleRaw) + ROUND_ERROR_REMOVAL);
    double nearestLowerPow10 = Math.pow(10, pow10);
    
    int scaleDigit = (int) (scaleRaw / nearestLowerPow10);
    double scale = scaleDigit * nearestLowerPow10;
    
    //System.out.println("requested scale = " + scaleRaw + " scale = " + scale  + "   Pow10 = " + pow10);
    return scale;
  }
  
  /**
   * Not used - scaling to multiples of 10,5,2 is too coarse.
   *  
   * 
   * @param scaleRaw
   * @return
   */
  private static double snapScaleTo_10_2_5(double scaleRaw)
  {
    // if the rounding error is not nudged, snapping can "stick" at some values
    double pow10 = Math.floor(MathUtil.log10(scaleRaw) + ROUND_ERROR_REMOVAL);
    double scaleRoundedToPow10 = Math.pow(10, pow10);
    
    double scale = scaleRoundedToPow10;
    // rounding to a power of 10 is too coarse, so allow some finer gradations
    //*
    if (3.5 * scaleRoundedToPow10 <= scaleRaw)
      scale = 5 * scaleRoundedToPow10;
    else if (2 * scaleRoundedToPow10 <= scaleRaw)
      scale = 2 * scaleRoundedToPow10;
    //*/
    
    //System.out.println("requested scale = " + scaleRaw + " scale = " + scale  + "   Pow10 = " + pow10);
    return scale;
  }
  
  public boolean intersectsInModel(Envelope env)
  {
  	return viewEnvInModel.intersects(env);
  }
  
  public Point2D toModel(Point2D viewPt) {
    srcPt.x = viewPt.getX();
    srcPt.y = viewPt.getY();
    try {
    	getModelToViewTransform().inverseTransform(srcPt, destPt);
    } catch (NoninvertibleTransformException ex) {
      return new Point2D.Double(0, 0);
      //Assert.shouldNeverReachHere();
    }
    
    // snap to scale grid
    double x = scalePM.makePrecise(destPt.x);
    double y = scalePM.makePrecise(destPt.y);
    
    return new Point2D.Double(x, y);
  }

  public Coordinate toModelCoordinate(Point2D viewPt) {
    Point2D p = toModel(viewPt);
    return new Coordinate(p.getX(), p.getY());
  }

  public void transform(Coordinate modelCoordinate, Point2D point)
  {
    point.setLocation(modelCoordinate.x, modelCoordinate.y);
    getModelToViewTransform().transform(point, point);
  }

  public Point2D toView(Coordinate modelCoordinate)
  {
    Point2D.Double pt = new Point2D.Double();
    transform(modelCoordinate, pt);
    return pt;
  }

  public Point2D toView(Point2D modelPt)
  {
    return toView(modelPt, new Point2D.Double());
  }

  public Point2D toView(Point2D modelPt, Point2D viewPt)
  {
    return getModelToViewTransform().transform(modelPt, viewPt);
  }

  /**
   * Converts a distance in the view to a distance in the model.
   * 
   * @param viewDist
   * @return the model distance
   */
  public double toModel(double viewDist)
  {
    return viewDist / scale;
  }
  
  /**
   * Converts a distance in the model to a distance in the view.
   * 
   * @param modelDist
   * @return the view distance
   */
  public double toView(double modelDist)
  {
    return modelDist * scale;
  }
  
  public void update(Dimension viewSize)
  {
    this.viewSize = viewSize;
    update();
  }
  
  private void update()
  {
    updateModelToViewTransform();
    viewEnvInModel = computeEnvelopeInModel();
    viewUpdated();
  }
  
  private void updateModelToViewTransform() {
    modelToViewTransform = new AffineTransform();
    modelToViewTransform.translate(0, viewSize.height);
    modelToViewTransform.scale(1, -1);
    modelToViewTransform.scale(scale, scale);
    modelToViewTransform.translate(-originInModel.getX(), -originInModel.getY());
  }

  public AffineTransform getModelToViewTransform() {
    if (modelToViewTransform == null) {
      updateModelToViewTransform();
    }
    return modelToViewTransform;
  }

  public void zoomToInitialExtent() {
    setScale(INITIAL_SCALE);
    setOrigin(INITIAL_ORIGIN_X, INITIAL_ORIGIN_Y);
  }

  public void zoom(Envelope zoomEnv) {
    double xScale = getWidthInView() / zoomEnv.getWidth();
    double yScale = getHeightInView() / zoomEnv.getHeight();
    double zoomScale = Math.min(xScale, yScale);
    setScale(zoomScale);
    double xCentering = (getWidthInModel() - zoomEnv.getWidth()) / 2d;
    double yCentering = (getHeightInModel() - zoomEnv.getHeight()) / 2d;
    setOrigin(zoomEnv.getMinX() - xCentering, zoomEnv.getMinY() - yCentering);
  }

  public void zoomPan(double dx, double dy) {
    setOrigin(originInModel.getX() - dx,
        originInModel.getY() - dy);
  }

  /**
   * Zoom to a point, ensuring that the zoom point remains in the same screen location.
   * 
   * @param zoomPt
   * @param zoomFactor
   */
  public void zoom(Point2D zoomPt, double zoomScale) {
    double originOffsetX = zoomPt.getX() - originInModel.getX();
    double originOffsetY = zoomPt.getY() - originInModel.getY();
    
    // set scale first, because it may be snapped
    double scalePrev = getScale();
    setScale(zoomScale);
    
    double actualZoomFactor = getScale() / scalePrev;
    double zoomOriginX = zoomPt.getX() - originOffsetX / actualZoomFactor;
    double zoomOriginY = zoomPt.getY() - originOffsetY / actualZoomFactor;
    setOrigin(zoomOriginX,  zoomOriginY);
  }

  private double getWidthInModel() {
    return toModel(viewSize.width);
  }

  private double getHeightInModel() {
    return toModel(viewSize.height);
  }

  public Point2D getLowerLeftCornerInModel() {
    return toModel(new Point(0, viewSize.height));
  }

  public double getHeightInView() {
    return viewSize.height;
  }

  public double getWidthInView() {
    return viewSize.width;
  }

  private Envelope computeEnvelopeInModel() {
    return new Envelope(
        originInModel.getX(),
        originInModel.getX() + getWidthInModel(),
        originInModel.getY(),
        originInModel.getY() + getHeightInModel());
  }

  public boolean containsInModel(Coordinate p)
  {
    return viewEnvInModel.contains(p);
  }

  private static final int MIN_GRID_RESOLUTION_PIXELS = 2;
  
  /**
   * Gets the magnitude (power of 10)
   * for the basic grid size.
   * 
   * @return the magnitude
   */
  public int gridMagnitudeModel()
  {
  	double pixelSizeModel = toModel(1);
  	double pixelSizeModelLog = MathUtil.log10(pixelSizeModel);
  	int gridMag = (int) Math.ceil(pixelSizeModelLog);
  	
  	/**
  	 * Check if grid size is too small and if so increase it one magnitude
  	 */
  	double gridSizeModel = Math.pow(10, gridMag);
  	double gridSizeView = toView(gridSizeModel);
//  	System.out.println("\ncand gridSizeView= " + gridSizeView);
  	if (gridSizeView <= MIN_GRID_RESOLUTION_PIXELS )
  		gridMag += 1;
  	
//  	System.out.println("pixelSize= " + pixelSize + "  pixelLog10= " + pixelSizeLog);
  	return gridMag;
  }

  /**
   * Gets a PrecisionModel corresponding to the grid size.
   * 
   * @return the precision model
   */
  public PrecisionModel getGridPrecisionModel()
  {
  	double gridSizeModel = getGridSizeModel();
  	return new PrecisionModel(1.0/gridSizeModel);
  }
  
  public double getGridSizeModel()
  {
    return Math.pow(10, gridMagnitudeModel());
  }
}
