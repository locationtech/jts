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

package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

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
  
  public static Geometry viewport(Geometry g, Geometry gViewport)
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
