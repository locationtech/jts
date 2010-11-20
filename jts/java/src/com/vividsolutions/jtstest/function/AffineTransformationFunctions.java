package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class AffineTransformationFunctions 
{
	public static Geometry transformByVectors(Geometry g, Geometry control)
	{
		int nControl = control.getNumGeometries();
		Coordinate src[] = new Coordinate[nControl];
		Coordinate dest[] = new Coordinate[nControl];
		for (int i = 0; i < nControl; i++) {
			Geometry contComp = control.getGeometryN(i);
			Coordinate[] pts = contComp.getCoordinates();
			src[i] = pts[0];
			dest[i] = pts[1];
		}
		AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(src, dest);
		System.out.println(trans);
    return trans.transform(g);    
	}
  
	public static Geometry transformByBaseline(Geometry g, Geometry destBaseline)
	{
		Envelope env = g.getEnvelopeInternal();
		Coordinate src0 = new Coordinate(env.getMinX(), env.getMinY());
		Coordinate src1 = new Coordinate(env.getMaxX(), env.getMinY());
		
		Coordinate[] destPts = destBaseline.getCoordinates();
		Coordinate dest0 = destPts[0];
		Coordinate dest1 = destPts[1];
		AffineTransformation trans = AffineTransformationFactory.createFromBaseLines(src0, src1, dest0, dest1);
    return trans.transform(g);    
	}
	
  private static Coordinate envelopeCentre(Geometry g)
  {
    return g.getEnvelopeInternal().centre();
  }
  
  private static Coordinate envelopeLowerLeft(Geometry g)
  {
    Envelope env = g.getEnvelopeInternal();
    return new Coordinate(env.getMinX(), env.getMinY());
  }
  
  public static Geometry scale(Geometry g, double scale)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.scaleInstance(scale, scale, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  public static Geometry reflectInX(Geometry g)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.scaleInstance(1, -1, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  public static Geometry reflectInY(Geometry g)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.scaleInstance(-1, 1, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  public static Geometry rotateByPiMultiple(Geometry g, double multipleOfPi)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.rotationInstance(multipleOfPi * Math.PI, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  public static Geometry rotate(Geometry g, double angle)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.rotationInstance(angle, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  public static Geometry translateCentreToOrigin(Geometry g)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.translationInstance(-centre.x, -centre.y);
    return trans.transform(g);    
  }
  public static Geometry translateToOrigin(Geometry g)
  {
    Coordinate lowerLeft = envelopeLowerLeft(g);
    AffineTransformation trans = AffineTransformation.translationInstance(-lowerLeft.x, -lowerLeft.y);
    return trans.transform(g);    
  }
}
