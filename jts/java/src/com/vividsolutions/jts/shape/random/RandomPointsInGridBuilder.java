package com.vividsolutions.jts.shape.random;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.math.MathUtil;
import com.vividsolutions.jts.shape.GeometricShapeBuilder;

/**
 * Creates random point sets 
 * where the points are constrained to lie in the cells of a grid. 
 * 
 * @author mbdavis
 *
 */
public class RandomPointsInGridBuilder 
extends GeometricShapeBuilder
{
	private boolean isConstrainedToCircle = false;
	private double gutterFraction = 0;
	
  /**
   * Create a builder which will create shapes using the default
   * {@link GeometryFactory}.
   */
  public RandomPointsInGridBuilder()
  {
    super(new GeometryFactory());
  }

  /**
   * Create a builder which will create shapes using the given
   * {@link GeometryFactory}.
   *
   * @param geomFact the factory to use
   */
  public RandomPointsInGridBuilder(GeometryFactory geomFact)
  {
  	super(geomFact);
  }

  public void setConstrainedToCircle(boolean isConstrainedToCircle)
  {
  	this.isConstrainedToCircle = isConstrainedToCircle;
  }
  
  public void setGutterFraction(double gutterFraction)
  {
  	this.gutterFraction = gutterFraction;
  }
  
  public Geometry getGeometry()
  {
    int nCells = (int) Math.sqrt(numPts) + 1;

    double gridDX = getExtent().getWidth() / nCells;
    double gridDY = getExtent().getHeight() / nCells;

    double gutterFrac = MathUtil.clamp(gutterFraction, 0.0, 1.0);
    double gutterOffsetX = gridDX * gutterFrac/2;
    double gutterOffsetY = gridDY * gutterFrac/2;
    double cellFrac = 1.0 - gutterFrac;
    double cellDX = cellFrac * gridDX;
    double cellDY = cellFrac * gridDY;
    	
    List pts = new ArrayList();

    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
      	double orgX = getExtent().getMinX() + i * gridDX + gutterOffsetX;
      	double orgY = getExtent().getMinY() + j * gridDY + gutterOffsetY;
        pts.add(randomPointInCell(orgX, orgY, cellDX, cellDY));
      }
    }
    return geomFactory.createMultiPoint(CoordinateArrays.toCoordinateArray(pts));
  }
  
  private Coordinate randomPointInCell(double orgX, double orgY, double xLen, double yLen)
  {
  	if (isConstrainedToCircle) {
  		randomPointInCircle(
  				orgX, 
  				orgY, 
  				xLen, yLen);
  	}
  	return randomPointInGridCell(orgX, orgY, xLen, yLen);
  }
  
  private Coordinate randomPointInGridCell(double orgX, double orgY, double xLen, double yLen)
  {
    double x = orgX + xLen * Math.random();
    double y = orgY + yLen * Math.random();
    return createCoord(x, y);
  }

  private static Coordinate randomPointInCircle(double orgX, double orgY, double width, double height)
  {
  	double centreX = orgX + width/2;
  	double centreY = orgX + height/2;
  		
  	double rndAng = 2 * Math.PI * Math.random();
  	double rndRadius = Math.random();
    // use square root of radius, since area is proportional to square of radius
    double rndRadius2 = Math.sqrt(rndRadius);
  	double rndX = width/2 * rndRadius2 * Math.cos(rndAng); 
  	double rndY = height/2 * rndRadius2 * Math.sin(rndAng); 
  	
    double x0 = centreX + rndX;
    double y0 = centreY + rndY;
    return new Coordinate(x0, y0);    
  }

}
