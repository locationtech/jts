/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.triangulate;

import java.util.*;

import org.locationtech.jts.geom.*;

/**
 * Creates a map between the vertex {@link Coordinate}s of a 
 * set of {@link Geometry}s,
 * and the parent geometry, and transfers the source geometry
 * data objects to geometry components tagged with the coordinates.
 * <p>
 * This class can be used in conjunction with {@link VoronoiDiagramBuilder}
 * to transfer data objects from the input site geometries
 * to the constructed Voronoi polygons.
 * 
 * @author Martin Davis
 * @see VoronoiDiagramBuilder
 *
 */
public class VertexTaggedGeometryDataMapper 
{
	private Map coordDataMap = new TreeMap();
	
	public VertexTaggedGeometryDataMapper()
	{
		
	}
	
	public void loadSourceGeometries(Collection geoms)
	{
		for (Iterator i = geoms.iterator(); i.hasNext(); ) {
			Geometry geom = (Geometry) i.next();
			loadVertices(geom.getCoordinates(), geom.getUserData());
		}
	}
	
	public void loadSourceGeometries(Geometry geomColl)
	{
		for (int i = 0; i < geomColl.getNumGeometries(); i++) {
			Geometry geom = geomColl.getGeometryN(i);
			loadVertices(geom.getCoordinates(), geom.getUserData());
		}
	}
	
	private void loadVertices(Coordinate[] pts, Object data)
	{
		for (int i = 0; i < pts.length; i++) {
			coordDataMap.put(pts[i], data);
		}
	}
	
	public List getCoordinates()
	{
		return new ArrayList(coordDataMap.keySet());
	}
	
	/**
	 * Input is assumed to be a multiGeometry
	 * in which every component has its userData
	 * set to be a Coordinate which is the key to the output data.
	 * The Coordinate is used to determine
	 * the output data object to be written back into the component. 
	 * 
	 * @param targetGeom
	 */
	public void transferData(Geometry targetGeom)
	{
		for (int i = 0; i < targetGeom.getNumGeometries(); i++) {
			Geometry geom = targetGeom.getGeometryN(i);
			Coordinate vertexKey = (Coordinate) geom.getUserData();
			if (vertexKey == null) continue;
			geom.setUserData(coordDataMap.get(vertexKey));
		}
	}
}
