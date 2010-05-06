package com.vividsolutions.jts.geom.util;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;
import com.vividsolutions.jts.geom.*;


public class RandomShapeFactory 
{
  protected GeometryFactory geomFact;
  protected PrecisionModel precModel = null;
  protected Envelope extentEnv = new Envelope(0,1,0,1);
  protected Geometry extentPoly = null;
  private PointOnGeometryLocator extentLocator;

  /**
   * Create a shape factory which will create shapes using the default
   * {@link GeometryFactory}.
   */
  public RandomShapeFactory()
  {
    this(new GeometryFactory());
  }

  /**
   * Create a shape factory which will create shapes using the given
   * {@link GeometryFactory}.
   *
   * @param geomFact the factory to use
   */
  public RandomShapeFactory(GeometryFactory geomFact)
  {
    this.geomFact = geomFact;
    precModel = geomFact.getPrecisionModel();
  }

  public void setExtent(Envelope extentEnv)
  {
  	this.extentEnv = extentEnv;
  }
  
  public Envelope getEnvelope()
  {
  	return extentEnv;
  }
  
  /**
   * Sets a polygonal mask.
   * 
   * @param mask
   * @ throws IllegalArgumentException if the mask is not polygonal
   */
  public void setExtent(Geometry mask)
  {
  	if (! (mask instanceof Polygonal))
  		throw new IllegalArgumentException("Only polygonal extents are supported");
  	this.extentPoly = mask;
  	setExtent(mask.getEnvelopeInternal());
  	extentLocator = new IndexedPointInAreaLocator(mask);
  }
  
  public Geometry createPoints(int n)
  {
  	Coordinate[] pts = new Coordinate[n];
  	int i = 0;
  	while (i < n) {
  		Coordinate p = createRandomCoord(getEnvelope());
  		if (extentLocator != null && ! isInExtent(p))
  			continue;
  		pts[i++] = p;
  	}
  	return geomFact.createMultiPoint(pts);
  }
  
  protected boolean isInExtent(Coordinate p)
  {
  	if (extentLocator != null) 
  		return extentLocator.locate(p) != Location.EXTERIOR;
  	return extentEnv.contains(p);
  }
  
  protected Coordinate createCoord(double x, double y)
  {
  	Coordinate pt = new Coordinate(x, y);
    precModel.makePrecise(pt);
    return pt;
  }
  
  protected Coordinate createRandomCoord(Envelope env)
  {
    double x = env.getMinX() + env.getWidth() * Math.random();
    double y = env.getMinY() + env.getHeight() * Math.random();
    return createCoord(x, y);
  }

}
