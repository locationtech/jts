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

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.awt.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;
import org.locationtech.jts.util.GeometricShapeFactory;

public class CreateShapeFunctions {

  
	private static final int DEFAULT_POINTSIZE = 100;
	
        public static Geometry fontGlyphSerif(Geometry g, String text)
        {
                return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, DEFAULT_POINTSIZE));
        }
                
        public static Geometry fontGlyphSerifPointSize(Geometry g, String text, int pointSize)
        {
                return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, pointSize));
        }
                
        public static Geometry fontGlyph(Geometry g, String text, String fontName)
        {
                return fontGlyph(g, text, new Font(fontName, Font.PLAIN, DEFAULT_POINTSIZE));
        }
                
	public static Geometry fontGlyphSansSerif(Geometry g, String text)
	{
		return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SANSSERIF, Font.PLAIN, DEFAULT_POINTSIZE));
	}
		
	public static Geometry fontGlyphMonospaced(Geometry g, String text)
	{
		return fontGlyph(g, text, new Font(FontGlyphReader.FONT_MONOSPACED, Font.PLAIN, DEFAULT_POINTSIZE));
	}
		
	private static Geometry fontGlyph(Geometry g, String text, Font font) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

		Geometry textGeom = FontGlyphReader.read(text, font, geomFact);
		Envelope envText = textGeom.getEnvelopeInternal();
		
		if (g != null) {
			// transform to baseline
			Coordinate baseText0 = new Coordinate(envText.getMinX(), envText.getMinY());
			Coordinate baseText1 = new Coordinate(envText.getMaxX(), envText.getMinY());
			Coordinate baseGeom0 = new Coordinate(env.getMinX(), env.getMinY());
			Coordinate baseGeom1 = new Coordinate(env.getMaxX(), env.getMinY());
			AffineTransformation trans = AffineTransformationFactory.createFromBaseLines(baseText0, baseText1, baseGeom0, baseGeom1);
			return trans.transform(textGeom);
		}
		return textGeom;
	}
	
	public static Geometry grid(Geometry g, int nCells)
	{
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
		
		int nCellsOnSideY = (int) Math.sqrt(nCells);
		int nCellsOnSideX = nCells / nCellsOnSideY;
		
		// alternate: make square cells, with varying grid width/height
		//double extent = env.minExtent();
		//double nCellsOnSide = Math.max(nCellsOnSideY, nCellsOnSideX);
		
		double cellSizeX = env.getWidth() / nCellsOnSideX;
		double cellSizeY = env.getHeight() / nCellsOnSideY;
		
		List geoms = new ArrayList(); 

		for (int i = 0; i < nCellsOnSideX; i++) {
			for (int j = 0; j < nCellsOnSideY; j++) {
				double x = env.getMinX() + i * cellSizeX;
				double y = env.getMinY() + j * cellSizeY;
				double x2 = env.getMinX() + (i + 1) * cellSizeX;
				double y2 = env.getMinY() + (j + 1) * cellSizeY;
			
				Envelope cellEnv = new Envelope(x, x2, y, y2);
				geoms.add(geomFact.toGeometry(cellEnv));
			}
		}
		return geomFact.buildGeometry(geoms);
	}
	
	public static Geometry supercircle3(Geometry g, int nPts)
	{
		return supercircle(g, nPts, 3);
	}

	public static Geometry squircle(Geometry g, int nPts)
	{
		return supercircle(g, nPts, 4);
	}
	
	public static Geometry supercircle5(Geometry g, int nPts)
	{
		return supercircle(g, nPts, 5);
	}

	public static Geometry supercirclePoint5(Geometry g, int nPts)
	{
		return supercircle(g, nPts, 0.5);
	}

	
	public static Geometry supercircle(Geometry g, int nPts, double pow)
	{
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setNumPoints(nPts);
		if (g != null)
			gsf.setEnvelope(g.getEnvelopeInternal());
		else
			gsf.setEnvelope(new Envelope(0, 1, 0, 1));
		return gsf.createSupercircle(pow);
	}
	
  public static Geometry ellipse(Geometry g, int nPts)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setNumPoints(nPts);
    if (g != null)
      gsf.setEnvelope(g.getEnvelopeInternal());
    else
      gsf.setEnvelope(new Envelope(0, 1, 0, 1));
    return gsf.createCircle();
  }
  public static Geometry ellipseRotate(Geometry g, int nPts, double ang)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setNumPoints(nPts);
    gsf.setRotation(ang);
    if (g != null)
      gsf.setEnvelope(g.getEnvelopeInternal());
    else
      gsf.setEnvelope(new Envelope(0, 1, 0, 1));
    return gsf.createCircle();
  }
  
  public static Geometry sineStar(Geometry g, int nArms, int nPts)
  {
	Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
	GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
	
	double size = Math.min(env.getHeight(),  env.getWidth());
    SineStarFactory shape = new SineStarFactory(geomFact);
    shape.setCentre(env.centre());
    shape.setSize(size);
    shape.setNumPoints(nPts);
    shape.setNumArms(nArms);
    shape.setArmLengthRatio(0.5);
    return shape.createSineStar();
  }
  
  public static Geometry comb(Geometry g, int nArms)
  {
	Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
	GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
	
	int npts = 4 * (nArms - 1) + 2 + 2 + 1;
	Coordinate[] pts = new Coordinate[npts];
	double armWidth = env.getWidth() / (2 * nArms - 1);
	double armLen = env.getHeight() - armWidth;
	
	double xBase = env.getMinX();
	double yBase = env.getMinY();
	
	int ipts = 0;
	for (int i = 0; i < nArms; i++) {
		double x1 = xBase + i * 2 * armWidth;
		double y1 = yBase + armLen + armWidth;
		pts[ipts++] = new Coordinate(x1, y1);
		pts[ipts++] = new Coordinate(x1 + armWidth, y1);
		if (i < nArms - 1) {
			pts[ipts++] = new Coordinate(x1 + armWidth, yBase + armWidth);
			pts[ipts++] = new Coordinate(x1 + 2 * armWidth, yBase + armWidth);
		}
	}
	pts[ipts++] = new Coordinate(env.getMaxX(), yBase);
	pts[ipts++] = new Coordinate(xBase, yBase);
	pts[ipts++] = new Coordinate(pts[0]);
	
	return geomFact.createPolygon(pts);
  }
  

}
