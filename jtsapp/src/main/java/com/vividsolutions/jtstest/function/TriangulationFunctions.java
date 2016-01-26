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
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.*;
import com.vividsolutions.jts.triangulate.quadedge.LocateFailureException;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;
import com.vividsolutions.jtstest.util.*;

public class TriangulationFunctions 
{
	private static final double TRIANGULATION_TOLERANCE = 0.0;
	
  public static Geometry delaunayEdges(Geometry geom)
  {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    Geometry edges = builder.getEdges(geom.getFactory());
    return edges;
  }

  public static Geometry delaunayTriangles(Geometry geom)
  {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    Geometry tris = builder.getTriangles(geom.getFactory());
    return tris;
  }

  public static Geometry delaunayEdgesWithTolerance(Geometry geom, double tolerance)
  {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    Geometry edges = builder.getEdges(geom.getFactory());
    return edges;
  }


  public static Geometry delaunayTrianglesWithTolerance(Geometry geom, double tolerance)
  {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    Geometry tris = builder.getTriangles(geom.getFactory());
    return tris;
  }

  public static Geometry delaunayTrianglesWithToleranceNoError(Geometry geom, double tolerance)
  {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    try {
      Geometry tris = builder.getTriangles(geom.getFactory());
      return tris;
    }
    catch (LocateFailureException ex) {
      System.out.println(ex);
      // ignore this exception and drop thru
    }
    /**
     * Get the triangles created up until the error
     */
    Geometry tris = builder.getSubdivision().getTriangles(geom.getFactory());
    return tris;      
  }

  public static Geometry voronoiDiagram(Geometry sitesGeom, Geometry clipGeom)
  {
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sitesGeom);
    if (clipGeom != null)
    	builder.setClipEnvelope(clipGeom.getEnvelopeInternal());
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    Geometry diagram = builder.getDiagram(sitesGeom.getFactory()); 
    return diagram;
  }

  public static Geometry voronoiDiagramWithData(Geometry sitesGeom, Geometry clipGeom)
  {
  	GeometryDataUtil.setComponentDataToIndex(sitesGeom);
  	
  	VertexTaggedGeometryDataMapper mapper = new VertexTaggedGeometryDataMapper();
  	mapper.loadSourceGeometries(sitesGeom);
  	
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(mapper.getCoordinates());
    if (clipGeom != null)
    	builder.setClipEnvelope(clipGeom.getEnvelopeInternal());
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    Geometry diagram = builder.getDiagram(sitesGeom.getFactory()); 
    mapper.transferData(diagram);
    return diagram;
  }


  public static Geometry conformingDelaunayEdges(Geometry sites, Geometry constraints)
  {
    return conformingDelaunayEdgesWithTolerance(sites, constraints, TRIANGULATION_TOLERANCE);
  }

  public static Geometry conformingDelaunayEdgesWithTolerance(Geometry sites, Geometry constraints, double tol)
  {
    ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
    builder.setSites(sites);
    builder.setConstraints(constraints);
    builder.setTolerance(tol);
    
    GeometryFactory geomFact = sites != null ? sites.getFactory() : constraints.getFactory();
    Geometry tris = builder.getEdges(geomFact);
    return tris;
  }

  public static Geometry conformingDelaunayTriangles(Geometry sites, Geometry constraints)
  {
    return conformingDelaunayTrianglesWithTolerance(sites, constraints, TRIANGULATION_TOLERANCE);
  }
  
  public static Geometry conformingDelaunayTrianglesWithTolerance(Geometry sites, Geometry constraints, double tol)
  {
		ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
  	builder.setSites(sites);
  	builder.setConstraints(constraints);
  	builder.setTolerance(tol);
  	
  	GeometryFactory geomFact = sites != null ? sites.getFactory() : constraints.getFactory();
  	Geometry tris = builder.getTriangles(geomFact);
  	return tris;
  }

}
