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

package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;
import org.locationtech.jtstest.geomfunction.Metadata;

public class AffineTransformationFunctions 
{
  @Metadata(description="Transforms a geometry using 1, 2 or 3 control vectors")
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
	
  @Metadata(description="Transforms a geometry by mapping envelope baseline to target vector")
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
  
  public static Geometry transformToViewport(Geometry g, Geometry gViewport)
  {
    Envelope viewEnv = gViewport.getEnvelopeInternal();
    Envelope env = g.getEnvelopeInternal();
    AffineTransformation trans = viewportTrans(env, viewEnv);
    return trans.transform(g);
  }

  private static AffineTransformation viewportTrans(Envelope srcEnv, Envelope viewEnv) {
    // works even if W or H are zero, thanks to Java infinity value.
    double scaleW = viewEnv.getWidth() / srcEnv.getWidth();
    double scaleH = viewEnv.getHeight() / srcEnv.getHeight();
    // choose minimum scale to ensure source fits viewport
    double scale = Math.min(scaleW,  scaleH);
    
    Coordinate centre = srcEnv.centre();
    Coordinate viewCentre = viewEnv.centre();
    
    // isotropic scaling
    AffineTransformation trans = AffineTransformation.scaleInstance(scale, scale, centre.x, centre.y);
    // translate using envelope centres
    trans.translate(viewCentre.x - centre.x, viewCentre.y - centre.y);
    return trans;
  }
  
  public static Geometry scale(Geometry g, 
      @Metadata(title="Scale factor")
      double scale)
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
  
  @Metadata(description="Rotate a geometry by an multiple of Pi radians")
  public static Geometry rotateByPiMultiple(Geometry g,
      @Metadata(title="Angle (multiple of Pi)")
      double multipleOfPi)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.rotationInstance(multipleOfPi * Math.PI, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  @Metadata(description="Rotate a geometry around a point by an multiple of Pi radians")
  public static Geometry rotateByPiMultipleAroundPoint(Geometry g, Geometry pt, 
      @Metadata(title="Angle (multiple of Pi)")
      double multipleOfPi)
  {
    Coordinate loc;
    if (pt == null) {
      loc = new Coordinate(0,0);
    }
    else {
      loc = pt.getCoordinates()[0];
    }
    AffineTransformation trans = AffineTransformation.rotationInstance(multipleOfPi * Math.PI, loc.x, loc.y);
    return trans.transform(g);    
  }
  
  @Metadata(description="Rotate a geometry by an angle in radians")
  public static Geometry rotate(Geometry g, 
      @Metadata(title="Angle (radians)")
      double angle)
  {
    Coordinate centre = envelopeCentre(g);
    AffineTransformation trans = AffineTransformation.rotationInstance(angle, centre.x, centre.y);
    return trans.transform(g);    
  }
  
  @Metadata(description="Rotate a geometry around a point by an angle in radians")
  public static Geometry rotateAroundPoint(Geometry g, Geometry pt, 
      @Metadata(title="Angle (radians)")
      double angle)
  {
    Coordinate loc;
    if (pt == null) {
      loc = new Coordinate(0,0);
    }
    else {
      loc = pt.getCoordinates()[0];
    }
    AffineTransformation trans = AffineTransformation.rotationInstance(angle, loc.x, loc.y);
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
  @Metadata(description="Translates a geometry by an offset (dx,dy)")
  public static Geometry translate(Geometry g, 
      @Metadata(title="dX")
      double dx, 
      @Metadata(title="dY")
      double dy)
  {
    AffineTransformation trans = AffineTransformation.translationInstance(dx, dy);
    return trans.transform(g);    
  }
}
