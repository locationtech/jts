package com.vividsolutions.jtstest.function;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.awt.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryCreationFunctions {

	public static Geometry randomPointsInGrid(Geometry g, int nPts) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

		int nCell = (int) Math.sqrt(nPts) + 1;

		double xLen = env.getWidth() / nCell;
		double yLen = env.getHeight() / nCell;

		List pts = new ArrayList();

		for (int i = 0; i < nCell; i++) {
			for (int j = 0; j < nCell; j++) {
				double x = env.getMinX() + i * xLen + xLen * Math.random();
				double y = env.getMinY() + j * yLen + yLen * Math.random();
				pts.add(geomFact.createPoint(new Coordinate(x, y)));
			}
		}
		return geomFact.buildGeometry(pts);
	}

	public static Geometry randomPoints(Geometry g, int nPts) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
		double xLen = env.getWidth();
		double yLen = env.getHeight();

		List pts = new ArrayList();

		for (int i = 0; i < nPts; i++) {
			double x = env.getMinX() + xLen * Math.random();
			double y = env.getMinY() + yLen * Math.random();
			pts.add(geomFact.createPoint(new Coordinate(x, y)));
		}
		return geomFact.buildGeometry(pts);
	}

	public static Geometry randomRadialPoints(Geometry g, int nPts) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
		double xLen = env.getWidth();
		double yLen = env.getHeight();
		double rMax = Math.min(xLen, yLen) / 2.0;
		
		double centreX = env.getMinX() + xLen/2;
		double centreY = env.getMinY() + yLen/2;
		
		List pts = new ArrayList();

		for (int i = 0; i < nPts; i++) {
			double rand = Math.random();
			double r = rMax * rand * rand;
			double ang = 2 * Math.PI * Math.random();
			double x = centreX + r * Math.cos(ang);
			double y = centreY + r * Math.sin(ang);
			pts.add(geomFact.createPoint(new Coordinate(x, y)));
		}
		return geomFact.buildGeometry(pts);
	}

	public static Geometry randomSegments(Geometry g, int nPts) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
		double xLen = env.getWidth();
		double yLen = env.getHeight();

		List lines = new ArrayList();

		for (int i = 0; i < nPts; i++) {
			double x0 = env.getMinX() + xLen * Math.random();
			double y0 = env.getMinY() + yLen * Math.random();
			double x1 = env.getMinX() + xLen * Math.random();
			double y1 = env.getMinY() + yLen * Math.random();
			lines.add(geomFact.createLineString(new Coordinate[] {
					new Coordinate(x0, y0), new Coordinate(x1, y1) }));
		}
		return geomFact.buildGeometry(lines);
	}

	public static Geometry randomSegmentsInGrid(Geometry g, int nPts) {
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

		int nCell = (int) Math.sqrt(nPts) + 1;

		double xLen = env.getWidth() / nCell;
		double yLen = env.getHeight() / nCell;

		List lines = new ArrayList();

		for (int i = 0; i < nCell; i++) {
			for (int j = 0; j < nCell; j++) {
				double x0 = env.getMinX() + i * xLen + xLen * Math.random();
				double y0 = env.getMinY() + j * yLen + yLen * Math.random();
				double x1 = env.getMinX() + i * xLen + xLen * Math.random();
				double y1 = env.getMinY() + j * yLen + yLen * Math.random();
				lines.add(geomFact.createLineString(new Coordinate[] {
						new Coordinate(x0, y0), new Coordinate(x1, y1) }));
			}
		}
		return geomFact.buildGeometry(lines);
	}

	private static final int DEFAULT_POINTSIZE = 100;
	
	public static Geometry fontGlyphSerif(Geometry g, String text)
	{
		return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, DEFAULT_POINTSIZE));
	}
		
	public static Geometry fontGlyphSanSerif(Geometry g, String text)
	{
		return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SANSERIF, Font.PLAIN, DEFAULT_POINTSIZE));
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
		List geoms = new ArrayList(); 
	
		Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
		GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
		
		int nCellsOnSide = (int) Math.sqrt(nCells) + 1;
		double delX = env.getWidth() / nCellsOnSide;
		double delY = env.getHeight() / nCellsOnSide;
		
		for (int i = 0; i < nCellsOnSide; i++) {
			for (int j = 0; j < nCellsOnSide; j++) {
				double x = env.getMinX() + i * delX;
				double y = env.getMinY() + j * delY;
			
				Envelope cellEnv = new Envelope(x, x + delX, y, y + delY);
				geoms.add(geomFact.toGeometry(cellEnv));
			}
		}
		return geomFact.buildGeometry(geoms);
	}
}
