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
package org.locationtech.jts.io.sde;

import org.locationtech.jts.geom.*;

import com.esri.sde.sdk.client.*;

/**
 * Reads a {@link Geometry} from an ESRI SDE Shape.
 * <p>
 * The SDE geometry model differs from the OGC model used by JTS.
 * In particular:
 * <ul>
 * <li>Simple lines are read as {@link LineString}s
 * <li>Inverted Polygons and Exverted Holes are read as is.
 * These create invalid JTS polygons, and must be
 * rectified if further operations are to be performed on them.
 * </ul>
 * <p>
 * This class allows specifying the maximum number of coordinate dimensions to read.
 * If dimensions higher than 2 are not required, it may be more efficient to not read them.
 * <p>
 * To use this class the ESRI SDE Java libraries must be present.
 * <p>
 * Currently reading measure (M) ordinates is not supported.
 * 
 * @author Martin Davis
 *
 */
public class SdeReader 
{
  private GeometryFactory geometryFactory;
  private PrecisionModel precisionModel;
  private CoordinateSequenceFactory coordSeqFact;
  private int maxDimensionToRead = 2;
  
  /**
   * Creates a reader that creates geometries using the default {@link GeometryFactory}.
   */
  public SdeReader() {
    this(new GeometryFactory());
  }
  
  /**
   * Creates a reader that creates geometries using the given {@link GeometryFactory}.
   * @param geometryFactory
   */
  public SdeReader(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    precisionModel = geometryFactory.getPrecisionModel();
    coordSeqFact = geometryFactory.getCoordinateSequenceFactory();
  }

	/**
	 * Gets the maximum number of coordinate dimensions which will be read.
	 * 
	 * @return the dimension which will be read
	 */
	public int getDimension() {
		return maxDimensionToRead;
	}

	/**
	 * Sets the maximum number of coordinate dimensions to read.
	 * If this is larger than the number of dimensions actually 
	 * present in the input geometry, the higher ordinates will not
	 * be read, and NaN will be returned as their value.
	 * <p>
	 * The default is to read only the X and Y ordinates (dimension = 2).
	 * 
	 * @param dimension the dimension to read
	 */
	public void setDimension(int dimension) {
		this.maxDimensionToRead = dimension;
	}

	/**
	 * Reads a {@link Geometry} from a given SDE shape.
	 * 
	 * @param shape the shape to read
	 * @return the geometry which represents the input shape
	 * 
	 * @throws SeException
	 */
  public Geometry read(SeShape shape)
  throws SeException
  {
  	switch (shape.getType()) {
  	case SeShape.TYPE_POINT:
  		return readPoint(shape);

  	case SeShape.TYPE_MULTI_POINT:
  		return readMultiPoint(shape);

  	case SeShape.TYPE_LINE:
  	case SeShape.TYPE_SIMPLE_LINE:
  		return readLine(shape);

  	case SeShape.TYPE_MULTI_LINE:
  	case SeShape.TYPE_MULTI_SIMPLE_LINE:
  		return readMultiLine(shape);

  	case SeShape.TYPE_POLYGON:
  		return readPolygon(shape);

  	case SeShape.TYPE_MULTI_POLYGON:
  		return readMultiPolygon(shape);

  	}
  	throw new IllegalArgumentException("Shapes of type " + shape.getType() + " are not supported");
  }
  
  private Point readPoint(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, false);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	return geometryFactory.createPoint(toCoordinates(sePts, 0, 1));
  }
  
  private MultiPoint readMultiPoint(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, false);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	return geometryFactory.createMultiPoint(toCoordinates(sePts, 0, sePts.length));
  }
  
  private LineString readLine(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, false);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	return geometryFactory.createLineString(toCoordinates(sePts, 0, sePts.length));
  }
  
  private MultiLineString readMultiLine(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, false);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	int[] partOffset = (int[]) list.get(1);
  	
  	LineString[] lines = new LineString[partOffset.length];
  	for (int i = 0; i < partOffset.length; i++) {
  		int end = sePts.length;
  		if (i < partOffset.length - 1) 
  			end = partOffset[i + 1];
  		lines[i] = geometryFactory.createLineString(toCoordinates(sePts, partOffset[i], end));
  	}
  	return geometryFactory.createMultiLineString(lines);
  }
  
  private Polygon readPolygon(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, true);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	// a polygon has only one part
  	int[] subPartOffset = (int[]) list.get(2);
  	
  	return readPolygon(sePts, subPartOffset, 0, subPartOffset.length);
  }
  
  private Polygon readPolygon(SDEPoint[] sePts, int[] subPartOffset, int subPartStart, int subPartEnd)
  throws SeException
  {  	
  	int numSubParts = subPartEnd - subPartStart;
  	int nHoles = numSubParts - 1;
  	if (nHoles < 0)
  		nHoles = 0;
  	LinearRing shell = null;
  	LinearRing[] holes = new LinearRing[nHoles];
  	int holeIndex = 0;
  	
  	for (int i = subPartStart; i < subPartEnd; i++) {
  		int end = sePts.length;
  		if (i < subPartEnd - 1) 
  			end = subPartOffset[i + 1];
  		LinearRing ring = geometryFactory.createLinearRing(toCoordinates(sePts, subPartOffset[i], end));
  		
  		if (shell == null) {
  			shell = ring;
  		}
  		else {
  			holes[holeIndex++] = ring;
  		}
  	}
  	return geometryFactory.createPolygon(shell, holes);
  }
  
  private MultiPolygon readMultiPolygon(SeShape shape)
  throws SeException
  {
  	java.util.List list = shape.getAllPoints(SeShape.TURN_RIGHT, true);  // get points and offsets
  	SDEPoint[] sePts = (SDEPoint[]) list.get(0);
  	int[] partOffset = (int[]) list.get(1);
  	int[] subPartOffset = (int[]) list.get(2);
  	
  	Polygon[] polys = new Polygon[partOffset.length];
  	for (int i = 0; i < partOffset.length; i++) {
  		int subPartEnd = subPartOffset.length;
  		if (i + 1 < partOffset.length) {
  			subPartEnd = partOffset[i + 1];
  		}
  		polys[i] = readPolygon(sePts, subPartOffset, partOffset[i], subPartEnd);
  	}
  	return geometryFactory.createMultiPolygon(polys);
  }
  
  private void readCoordinate(SDEPoint p, CoordinateSequence seq, int index)
  throws SeException
  {
  	seq.setOrdinate(index, 0, precisionModel.makePrecise(p.getX()));
  	seq.setOrdinate(index, 1, precisionModel.makePrecise(p.getY()));
  	// only read the Z dim if requested and present
  	if (maxDimensionToRead >= 3 && p.is3D()) {
  		seq.setOrdinate(index, 2, p.getZ());
  	}
  }
  
  private CoordinateSequence toCoordinates(SDEPoint[] sePts, int start, int end)
  throws SeException
  {
  	int size = end - start;
  	CoordinateSequence seq = coordSeqFact.create(size, maxDimensionToRead);
  	int index = 0;
  	for (int i = start; i < end; i++) {
  		readCoordinate(sePts[i], seq, index);
  		index++;
  	}
  	return seq;
  }
}
