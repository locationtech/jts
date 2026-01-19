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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.AffineTransformationFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.shape.CubicBezierCurve;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jtstest.geomfunction.Metadata;

public class CreateShapeFunctions {

  
	private static final int DEFAULT_POINTSIZE = 100;
	
        public static Geometry fontGlyphSerif(Geometry g, String text)
        {
                return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, DEFAULT_POINTSIZE));
        }
                
        public static Geometry fontGlyphSerifPointSize(Geometry g, String text, 
            @Metadata(title="Point size")
            int pointSize)
        {
                return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, pointSize));
        }
                
        public static Geometry fontGlyph(Geometry g, String text,
            @Metadata(title="Font name")
            String fontName)
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

  public static Geometry gridPoints(Geometry g, int nCells)
  {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    
    int nCellsOnSideY = (int) Math.sqrt(nCells);
    int nCellsOnSideX = nCells / nCellsOnSideY;
    
    double cellSizeX = env.getWidth() / (nCellsOnSideX - 1);
    double cellSizeY = env.getHeight() / (nCellsOnSideY - 1);
    
    CoordinateList pts = new CoordinateList(); 

    for (int i = 0; i < nCellsOnSideX; i++) {
      for (int j = 0; j < nCellsOnSideY; j++) {
        double x = env.getMinX() + i * cellSizeX;
        double y = env.getMinY() + j * cellSizeY;
      
        pts.add( new Coordinate(x, y) );
      }
    }
    return geomFact.createMultiPointFromCoords(pts.toCoordinateArray());
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

	
	public static Geometry supercircle(Geometry g,
	    @Metadata(title="Point count")
	    int nPts, 
	    @Metadata(title="Power")
	    double pow)
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
  
  public static Geometry ellipseRotate(Geometry g, int nPts, 
      @Metadata(title="Angle")
      double ang)
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
  
  public static Geometry sineStar(Geometry g,
      @Metadata(title="Arm count")
      int nArms, 
      @Metadata(title="Point count")
      int nPts)
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
  
  public static Geometry pointFieldCentroidStar(Geometry ptsGeom)
  {
    Coordinate[] pts = ptsGeom.getCoordinates();
    Geometry centroid = ptsGeom.getCentroid();
    return pointFieldStar(ptsGeom, centroid);
  }
  
  public static Geometry pointFieldStar(Geometry ptsGeom, Geometry centrePt)
  {
    Coordinate[] pts = ptsGeom.getCoordinates();
    Coordinate centre = centrePt.getCoordinate();
    
    List<OrderedPoint> orderedPts = new ArrayList<OrderedPoint>();
    for (Coordinate p : pts) {
      double ang = Angle.angle(centre, p);
      orderedPts.add(new OrderedPoint(p, ang));
    }
    Collections.sort(orderedPts);
    int n = pts.length+1;
    Coordinate[] ring = new Coordinate[n];
    int i = 0;
    for (OrderedPoint op : orderedPts) {
      ring[i++] = op.pt;
    }
    // close ring
    ring[n-1] = ring[0].copy();
    return ptsGeom.getFactory().createPolygon(ring);
  }
  
  private static class OrderedPoint implements Comparable {
    Coordinate pt;
    double index;
    
    public OrderedPoint(Coordinate p, double index) {
      this.pt = p;
      this.index = index;
    }
    
    @Override
    public int compareTo(Object o) {
      OrderedPoint other = (OrderedPoint) o;
      return Double.compare(index,  other.index);
    }
  }
  
  @Metadata(description="Construct a spiral")
  public static Geometry spiral(Geometry geom, 
      @Metadata(title="Num Cycles")
      int nCycles, 
      @Metadata(title="Quadrant Segs")
      int quadrantSegs) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(geom);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(geom);

    double width = Math.min(env.getHeight(), env.getWidth())/2;
    double pitch = width / nCycles;
    
    Coordinate centre = env.centre();
    
    CoordinateList inside = new CoordinateList();
    CoordinateList outside = new CoordinateList();
    for (int i = 1; i <= nCycles; i++) {
      Coordinate[] inCycle = genSpiralCycle(centre, i * pitch - pitch/2, (i+1) * pitch - pitch/2, quadrantSegs);
      inside.add(inCycle, false);
      Coordinate[] outCycle = genSpiralCycle(centre, i * pitch, (i+1) * pitch, quadrantSegs);
      outside.add(outCycle, false);
    }
    CoordinateList all = new CoordinateList();
    all.add(inside.toCoordinateArray(), false);
    Coordinate[] outsidePts = outside.toCoordinateArray();
    CoordinateArrays.reverse(outsidePts);
    all.add(outsidePts, false);
    all.closeRing();
    return geomFact.createPolygon(all.toCoordinateArray());
  }

  private static Coordinate[] genSpiralCycle(Coordinate centre, 
      double radiusStart, double radiusEnd, int quadrantSegs) {
    int nPts = quadrantSegs * 4 + 1;
    Coordinate[] pts = new Coordinate[nPts];
    double angInc = 2 * Math.PI / (nPts - 1);
    double radiusInc = (radiusEnd - radiusStart) / (nPts - 1);
    for (int i = 0; i < nPts; i++) {
      double radius = radiusStart + i * radiusInc;
      double x = radius * Math.cos(i *angInc);
      double y = radius * Math.sin(i *angInc);
      Coordinate pt = new Coordinate(centre.getX() + x, centre.getY() + y);
      pts[i] = pt;
    }
    return pts;
  }
  
  @Metadata(description="Construct a geometry using cubic Bezier curves")
  public static Geometry bezierCurve(Geometry geom, 
      @Metadata(title="Alpha (curveness)")
      double alpha) {
    return CubicBezierCurve.bezierCurve(geom, alpha);
  }
  
  @Metadata(description="Construct a geometry using cubic Bezier curves with a skew")
  public static Geometry bezierCurveSkew(Geometry geom, 
      @Metadata(title="Alpha (curveness)")
      double alpha,
    @Metadata(title="Skew factor")
    double skew) {
    return CubicBezierCurve.bezierCurve(geom, alpha, skew);
  }
  
  @Metadata(description="Construct a geometry using cubic Bezier curves with control points")
  public static Geometry bezierCurveControl(Geometry geom, Geometry controlPoints) {
    return CubicBezierCurve.bezierCurve(geom, controlPoints);
  }
  
  @Metadata(description="Get the generated control points for a Bezier curve")
  public static Geometry bezierControl(Geometry geom, 
      @Metadata(title="Alpha (curveness)")
      double alpha) {
    return CubicBezierCurve.controlPoints(geom, alpha);
  }
  
  @Metadata(description="Get the generated control points for a Bezier curve with a skew")
  public static Geometry bezierControlSkew(Geometry geom, 
      @Metadata(title="Alpha (curveness)")
      double alpha,
    @Metadata(title="Skew factor")
    double skew) {
    return CubicBezierCurve.controlPoints(geom, alpha, skew);
  }
  
  public static Geometry nGon(Geometry g, 
      @Metadata(title="Num sides")
      int sides) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    Coordinate centre = env.centre();
    double radius = Math.max(env.getHeight(), env.getWidth()) / 2;
    CoordinateList pts = new CoordinateList();
    double angInc = 2 * Math.PI / sides;
    for (int i = 0; i < sides; i++) {
      double x = centre.getX() + radius * Math.cos(i * angInc);
      double y = centre.getY() + radius * Math.sin(i * angInc);
      pts.add(new Coordinate(x, y));
    }
    pts.closeRing();
    return FunctionsUtil.getFactoryOrDefault(g).createPolygon(pts.toCoordinateArray());
  }
  
  public static Geometry diagonals(Geometry g) {
    Coordinate[] pts = g.getCoordinates();
    int n = pts.length - 1;
    
    List<Geometry> chords = new ArrayList<Geometry>();
    
    for (int i = 0; i < n; i++) {
      for (int j = i + 2; j < n; j++) {
        if (i == j) continue;
        if (i == 0 && j == n-1) continue;
        
        Coordinate[] chordPts = new Coordinate[] {
            pts[i].copy(), pts[j].copy() };
        LineString chord = g.getFactory().createLineString(chordPts);
        //-- only keep internal chords
        if (g.covers(chord)) {
          chords.add(chord);
        }
      }
    }
    return g.getFactory().buildGeometry(chords);
  }
  
  public static Geometry diagonalPartition(Geometry g) {
    Geometry chords = diagonals(g);
    Geometry noded = NodingFunctions.MCIndexNoding(g, chords);
    return PolygonizeFunctions.polygonize(noded);
  }
}
