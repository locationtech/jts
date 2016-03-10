/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.distance3d;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 * A CoordinateSequence wrapper which 
 * projects 3D coordinates into one of the
 * three Cartesian axis planes,
 * using the standard orthonormal projection
 * (i.e. simply selecting the appropriate ordinates into the XY ordinates).
 * The projected data is represented as 2D coordinates.
 * 
 * @author mdavis
 *
 */
public class AxisPlaneCoordinateSequence implements CoordinateSequence {

	/**
	 * Creates a wrapper projecting to the XY plane.
	 * 
	 * @param seq the sequence to be projected
	 * @return a sequence which projects coordinates
	 */
	public static CoordinateSequence projectToXY(CoordinateSequence seq)
	{
		/**
		 * This is just a no-op, but return a wrapper
		 * to allow better testing
		 */
		return new AxisPlaneCoordinateSequence(seq, XY_INDEX);
	}
	
	/**
	 * Creates a wrapper projecting to the XZ plane.
	 * 
	 * @param seq the sequence to be projected
	 * @return a sequence which projects coordinates
	 */
	public static CoordinateSequence projectToXZ(CoordinateSequence seq)
	{
		return new AxisPlaneCoordinateSequence(seq, XZ_INDEX);
	}
	
	/**
	 * Creates a wrapper projecting to the YZ plane.
	 * 
	 * @param seq the sequence to be projected
	 * @return a sequence which projects coordinates
	 */
	public static CoordinateSequence projectToYZ(CoordinateSequence seq)
	{
		return new AxisPlaneCoordinateSequence(seq, YZ_INDEX);
	}
	
	private static final int[] XY_INDEX = new int[] { 0,1 };
	private static final int[] XZ_INDEX = new int[] { 0,2 };
	private static final int[] YZ_INDEX = new int[] { 1,2 };
	
	private CoordinateSequence seq;
	private int[] indexMap;
	
	private AxisPlaneCoordinateSequence(CoordinateSequence seq, int[] indexMap) {
		this.seq = seq;
		this.indexMap = indexMap;
	}

	public int getDimension() {
		return 2;
	}

	public Coordinate getCoordinate(int i) {
		return getCoordinateCopy(i);
	}

	public Coordinate getCoordinateCopy(int i) {
		return new Coordinate(getX(i), getY(i), getZ(i));
	}

	public void getCoordinate(int index, Coordinate coord) {
		coord.x = getOrdinate(index, X);
		coord.y = getOrdinate(index, Y);
		coord.z = getOrdinate(index, Z);
	}

	public double getX(int index) {
		return getOrdinate(index, X);
	}

	public double getY(int index) {
		return getOrdinate(index, Y);
	}

	public double getZ(int index) {
		return getOrdinate(index, Z);
	}

	public double getOrdinate(int index, int ordinateIndex) {
		// Z ord is always 0
		if (ordinateIndex > 1) return 0;
		return seq.getOrdinate(index, indexMap[ordinateIndex]);
	}

	public int size() {
		return seq.size();
	}

	public void setOrdinate(int index, int ordinateIndex, double value) {
		throw new UnsupportedOperationException();
	}

	public Coordinate[] toCoordinateArray() {
		throw new UnsupportedOperationException();
	}

	public Envelope expandEnvelope(Envelope env) {
		throw new UnsupportedOperationException();
	}

	public Object clone()
	{
		throw new UnsupportedOperationException();		
	}

}
