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
package org.locationtech.jts.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;


/**
 * A utility class which creates Conforming Delaunay Trianglulations
 * from collections of points and linear constraints, and extract the resulting 
 * triangulation edges or triangles as geometries. 
 * 
 * @author Martin Davis
 *
 */
public class ConformingDelaunayTriangulationBuilder 
{
	private Collection siteCoords;
	private Geometry constraintLines;
	private double tolerance = 0.0;
	private QuadEdgeSubdivision subdiv = null;

	private Map constraintVertexMap = new TreeMap();
	
	public ConformingDelaunayTriangulationBuilder()
	{
	}
	
	/**
	 * Sets the sites (point or vertices) which will be triangulated.
	 * All vertices of the given geometry will be used as sites.
	 * The site vertices do not have to contain the constraint
	 * vertices as well; any site vertices which are 
	 * identical to a constraint vertex will be removed
	 * from the site vertex set.
	 * 
	 * @param geom the geometry from which the sites will be extracted.
	 */
	public void setSites(Geometry geom)
	{
		siteCoords = DelaunayTriangulationBuilder.extractUniqueCoordinates(geom);
	}

	/**
	 * Sets the linear constraints to be conformed to.
	 * All linear components in the input will be used as constraints.
	 * The constraint vertices do not have to be disjoint from 
	 * the site vertices.
   * The constraints must not contain duplicate segments (up to orientation).
	 * 
	 * @param constraintLines the lines to constraint to
	 */
	public void setConstraints(Geometry constraintLines)
	{
		this.constraintLines = constraintLines;
	}
	
	/**
	 * Sets the snapping tolerance which will be used
	 * to improved the robustness of the triangulation computation.
	 * A tolerance of 0.0 specifies that no snapping will take place.
	 * 
	 * @param tolerance the tolerance distance to use
	 */
	public void setTolerance(double tolerance)
	{
		this.tolerance = tolerance;
	}
	
	
	private void create()
	{
		if (subdiv != null) return;

		Envelope siteEnv = DelaunayTriangulationBuilder.envelope(siteCoords);
		
		List segments = new ArrayList();
		if (constraintLines != null) {
			siteEnv.expandToInclude(constraintLines.getEnvelopeInternal());
			createVertices(constraintLines);
			segments = createConstraintSegments(constraintLines);
		}
    List sites = createSiteVertices(siteCoords);

		ConformingDelaunayTriangulator cdt = new ConformingDelaunayTriangulator(sites, tolerance);
		
		cdt.setConstraints(segments, new ArrayList(constraintVertexMap.values()));
		
		cdt.formInitialDelaunay();
		cdt.enforceConstraints();
		subdiv = cdt.getSubdivision();
	}
	
	private List createSiteVertices(Collection coords)
	{
		List verts = new ArrayList();
		for (Iterator i = coords.iterator(); i.hasNext(); ) {
			Coordinate coord = (Coordinate) i.next();
			if (constraintVertexMap.containsKey(coord)) 
			  continue;
			verts.add(new ConstraintVertex(coord));
		}
		return verts;
	}

	private void createVertices(Geometry geom)
	{
		Coordinate[] coords = geom.getCoordinates();
		for (int i = 0; i < coords.length; i++) {
			Vertex v = new ConstraintVertex(coords[i]);
			constraintVertexMap.put(coords[i], v);
		}
	}
	
	private static List createConstraintSegments(Geometry geom)
	{
		List lines = LinearComponentExtracter.getLines(geom);
		List constraintSegs = new ArrayList();
		for (Iterator i = lines.iterator(); i.hasNext(); ) {
			LineString line = (LineString) i.next();
			createConstraintSegments(line, constraintSegs);
		}
		return constraintSegs;
	}
	
	private static void createConstraintSegments(LineString line, List constraintSegs)
	{
		Coordinate[] coords = line.getCoordinates();
		for (int i = 1; i < coords.length; i++) {
			constraintSegs.add(new Segment(coords[i-1], coords[i]));
		}
	}

	/**
	 * Gets the QuadEdgeSubdivision which models the computed triangulation.
	 * 
	 * @return the subdivision containing the triangulation
	 */
	public QuadEdgeSubdivision getSubdivision()
	{
		create();
		return subdiv;
	}
	
	/**
	 * Gets the edges of the computed triangulation as a {@link MultiLineString}.
	 * 
	 * @param geomFact the geometry factory to use to create the output
	 * @return the edges of the triangulation
	 */
	public Geometry getEdges(GeometryFactory geomFact)
	{
		create();
		return subdiv.getEdges(geomFact);
	}
	
	/**
	 * Gets the faces of the computed triangulation as a {@link GeometryCollection} 
	 * of {@link Polygon}.
	 * 
	 * @param geomFact the geometry factory to use to create the output
	 * @return the faces of the triangulation
	 */
	public Geometry getTriangles(GeometryFactory geomFact)
	{
		create();
		return subdiv.getTriangles(geomFact);
	}

}


