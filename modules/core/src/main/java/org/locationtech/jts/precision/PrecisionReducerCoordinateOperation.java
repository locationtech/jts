/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryEditor;

public class PrecisionReducerCoordinateOperation extends
		GeometryEditor.CoordinateOperation 
{
  private PrecisionModel targetPM;
  private boolean removeCollapsed = true;

	public PrecisionReducerCoordinateOperation(PrecisionModel targetPM, boolean removeCollapsed)
	{
		this.targetPM = targetPM;
		this.removeCollapsed = removeCollapsed;
	}
	
	public Coordinate[] edit(Coordinate[] coordinates, Geometry geom) {
		if (coordinates.length == 0)
			return null;

		Coordinate[] reducedCoords = new Coordinate[coordinates.length];
		// copy coordinates and reduce
		for (int i = 0; i < coordinates.length; i++) {
			Coordinate coord = new Coordinate(coordinates[i]);
			targetPM.makePrecise(coord);
			reducedCoords[i] = coord;
		}
		// remove repeated points, to simplify returned geometry as much as possible
		CoordinateList noRepeatedCoordList = new CoordinateList(reducedCoords,
				false);
		Coordinate[] noRepeatedCoords = noRepeatedCoordList.toCoordinateArray();

		/**
		 * Check to see if the removal of repeated points collapsed the coordinate
		 * List to an invalid length for the type of the parent geometry. It is not
		 * necessary to check for Point collapses, since the coordinate list can
		 * never collapse to less than one point. If the length is invalid, return
		 * the full-length coordinate array first computed, or null if collapses are
		 * being removed. (This may create an invalid geometry - the client must
		 * handle this.)
		 */
		int minLength = 0;
		if (geom instanceof LineString)
			minLength = 2;
		if (geom instanceof LinearRing)
			minLength = 4;

		Coordinate[] collapsedCoords = reducedCoords;
		if (removeCollapsed)
			collapsedCoords = null;

		// return null or orginal length coordinate array
		if (noRepeatedCoords.length < minLength) {
			return collapsedCoords;
		}

		// ok to return shorter coordinate array
		return noRepeatedCoords;
	}
}
