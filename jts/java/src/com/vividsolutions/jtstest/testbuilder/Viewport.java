package com.vividsolutions.jtstest.testbuilder;

import java.awt.*; 
import java.awt.geom.*; 
import java.text.NumberFormat;

import com.vividsolutions.jts.awt.PointTransformation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.Assert;


/**
 * Maintains the information associated with mapping 
 * the model view to the screen
 * 
 * @author Martin Davis
 *
 */
public class Viewport implements PointTransformation
{
  private static int INITIAL_VIEW_ORIGIN_X = -10;

  private static int INITIAL_VIEW_ORIGIN_Y = -10;


  private GeometryEditPanel panel;
  
  /**
   * Origin of view in model space
   */
  private Point2D viewOriginInModel =
      new Point2D.Double(INITIAL_VIEW_ORIGIN_X, INITIAL_VIEW_ORIGIN_Y);

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

  public Viewport(GeometryEditPanel panel) {
    this.panel = panel;
    setScaleNoUpdate(1.0);
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

  public void setScaleNoUpdate(double scale) {
    this.scale = snapScale(scale);
    scalePM = new PrecisionModel(this.scale);   
    
    scaleFormat = NumberFormat.getInstance();
    int fracDigits = (int) (Math.log10(this.scale));
    if (fracDigits < 0) fracDigits = 0;
    //System.out.println("scale = " + this.scale);
    //System.out.println("fracdigits = " + fracDigits);
    scaleFormat.setMaximumFractionDigits(fracDigits);
    // don't show commas
    scaleFormat.setGroupingUsed(false);
  }

  public void setScale(double scale) {
    setScaleNoUpdate(scale);
    update();
  }

  public NumberFormat getScaleFormat()
  {
    return scaleFormat;
  }
  
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
    double pow10 = Math.floor(Math.log10(scaleRaw));
    double roundTo10 = Math.pow(10, pow10);
    
    double scale = roundTo10;
    if (3.5 * roundTo10 <= scaleRaw)
      scale = 5 * roundTo10;
    else if (2 * roundTo10 <= scaleRaw)
      scale = 2 * roundTo10;
    
    //System.out.println("requested scale = " + scaleRaw + "scale = " + scale  + "   Pow10 = " + pow10);
    return scale;
  }
  

  public double getViewOriginX() {
    return viewOriginInModel.getX();
  }

  public double getViewOriginY() {
    return viewOriginInModel.getY();
  }
  
  public void setViewOrigin(double viewOriginX, double viewOriginY) {
    this.viewOriginInModel = new Point2D.Double(viewOriginX, viewOriginY);
    update();
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
      Assert.shouldNeverReachHere();
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
    Point2D.Double viewPt = new Point2D.Double();
    return getModelToViewTransform().transform(modelPt, viewPt);
  }

  public void update()
  {
    updateModelToViewTransform();
    viewEnvInModel = computeEnvelopeInModel();
    panel.forceRepaint();
  }
  
  private void updateModelToViewTransform() {
    modelToViewTransform = new AffineTransform();
    modelToViewTransform.translate(0, panel.getSize().height);
    modelToViewTransform.scale(1, -1);
    modelToViewTransform.scale(scale, scale);
    modelToViewTransform.translate(-viewOriginInModel.getX(), -viewOriginInModel.getY());
  }

  public AffineTransform getModelToViewTransform() {
    if (modelToViewTransform == null) {
      updateModelToViewTransform();
    }
    return modelToViewTransform;
  }

  public void zoomToInitialExtent() {
    setScale(1);
    setViewOrigin(INITIAL_VIEW_ORIGIN_X, INITIAL_VIEW_ORIGIN_Y);
  }

  public void zoom(Envelope zoomEnv) {
    zoomToInitialExtent();
    double xScale = getWidthInModel() / zoomEnv.getWidth();
    double yScale = getHeightInModel() / zoomEnv.getHeight();
    setScale(Math.min(xScale, yScale));
    double xCentering = (getWidthInModel() - zoomEnv.getWidth()) / 2d;
    double yCentering = (getHeightInModel() - zoomEnv
        .getHeight()) / 2d;
    setViewOrigin(zoomEnv.getMinX() - xCentering, 
        zoomEnv.getMinY() - yCentering);
  }

  public double getWidthInModel() {
    return getUpperRightCornerInModel().getX()
        - getLowerLeftCornerInModel().getX();
  }

  public double getHeightInModel() {
    return getUpperRightCornerInModel().getY()
        - getLowerLeftCornerInModel().getY();
  }

  public Point2D getLowerLeftCornerInModel() {
    Dimension size = panel.getSize();
    return toModel(new Point(0, size.height));
  }

  public Point2D getUpperRightCornerInModel() {
    Dimension size = panel.getSize();
    return toModel(new Point(size.width, 0));
  }

  public double getHeightInView() {
    return panel.getSize().height;
  }

  public double getWidthInView() {
    return panel.getSize().getWidth();
  }

  public double toModel(double viewDist)
  {
  	return viewDist / scale;
  }
  
  public double toView(double modelDist)
  {
  	return modelDist * scale;
  }
  
  private Envelope computeEnvelopeInModel() {
    double widthInModel = panel.getWidth() / scale;
    double heighInModel = panel.getHeight() / scale;

    return new Envelope(
        viewOriginInModel.getX(),
        viewOriginInModel.getX() + widthInModel,
        viewOriginInModel.getY(),
        viewOriginInModel.getY() + heighInModel);
  }

  public boolean containsInModel(Coordinate p)
  {
    return viewEnvInModel.contains(p);
  }


}
