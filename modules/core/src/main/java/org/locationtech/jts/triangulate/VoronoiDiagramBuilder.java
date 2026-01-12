/*
 * Copyright (c) 2026 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.snap.SnappingPointIndex;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;


/**
 * A utility class which creates Voronoi Diagrams
 * from collections of points.
 * The diagram is returned as a {@link GeometryCollection} of {@link Polygon}s
 * representing the faces of the Voronoi diagram.
 * The faces are clipped to the larger of:
 * <ul>
 * <li> an envelope supplied by {@link #setClipEnvelope(Envelope)}
 * <li> an envelope determined by the input sites
 * </ul>
 * The <tt>userData</tt> attribute of each face <tt>Polygon</tt> is set to 
 * the <tt>Coordinate</tt>  of the corresponding input site.
 * This allows using a <tt>Map</tt> to link faces to data associated with sites.
 * 
 * @author Martin Davis
 *
 */
public class VoronoiDiagramBuilder 
{
  /**
   * A very small factor which detects short Voronoi cell segments 
   * which might be caused by nearly-cocircular site circumcentres.
   */
	private static final double SHORT_SEG_TOLERANCE_FACTOR = 1.0e-10;
	
  private Collection siteCoords;
	private double tolerance = 0.0;
	private QuadEdgeSubdivision subdiv = null;
	private Envelope clipEnv = null;
	private Envelope diagramEnv = null; 
	
	/**
	 * Creates a new Voronoi diagram builder.
	 *
	 */
	public VoronoiDiagramBuilder()
	{
	}
	
	/**
	 * Sets the sites (point or vertices) which will be diagrammed.
	 * All vertices of the given geometry will be used as sites.
	 * 
	 * @param geom the geometry from which the sites will be extracted.
	 */
	public void setSites(Geometry geom)
	{
		// remove any duplicate points (they will cause the triangulation to fail)
		siteCoords = DelaunayTriangulationBuilder.extractUniqueCoordinates(geom);
	}
	
	/**
	 * Sets the sites (point or vertices) which will be diagrammed
	 * from a collection of {@link Coordinate}s.
	 * 
	 * @param coords a collection of Coordinates.
	 */
	public void setSites(Collection coords)
	{
		// remove any duplicate points (they will cause the triangulation to fail)
		siteCoords = DelaunayTriangulationBuilder.unique(CoordinateArrays.toCoordinateArray(coords));
	}
	
	/**
	 * Sets the envelope to clip the diagram to.
	 * The diagram will be clipped to the larger
	 * of this envelope or an envelope surrounding the sites.
	 * 
	 * @param clipEnv the clip envelope.
	 */
	public void setClipEnvelope(Envelope clipEnv)
	{
		this.clipEnv = clipEnv;
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
		
		diagramEnv = clipEnv;
		if (diagramEnv == null) {
		  /** 
		   * If no user-provided clip envelope, 
		   * use one which encloses all the sites,
		   * with a 50% buffer around the edges.
		   */
  		diagramEnv = DelaunayTriangulationBuilder.envelope(siteCoords);
  		// add a 50% buffer around the sites envelope
  		double expandBy = diagramEnv.getDiameter();
  		diagramEnv.expandBy(expandBy);
		}

		List vertices = DelaunayTriangulationBuilder.toVertices(siteCoords);
		subdiv = new QuadEdgeSubdivision(diagramEnv, tolerance);
		IncrementalDelaunayTriangulator triangulator = new IncrementalDelaunayTriangulator(subdiv);
		/**
		 * Avoid creating very narrow triangles along triangulation boundary.
		 * These otherwise can cause malformed Voronoi cells.
		 */
		triangulator.forceConvex(false);
		triangulator.insertSites(vertices);
	}
  
	/**
	 * Gets the {@link QuadEdgeSubdivision} which models the computed diagram.
	 * 
	 * @return the subdivision containing the triangulation
	 */
	public QuadEdgeSubdivision getSubdivision()
	{
		create();
		return subdiv;
	}
	
	/**
	 * Gets the faces of the computed diagram as a {@link GeometryCollection} 
	 * of {@link Polygon}s, clipped as specified.
	 * <p>
	 * The <tt>userData</tt> attribute of each face <tt>Polygon</tt> is set to 
	 * the <tt>Coordinate</tt>  of the corresponding input site.
	 * This allows using a <tt>Map</tt> to link faces to data associated with sites.
	 * 
	 * @param geomFact the geometry factory to use to create the output
	 * @return a <tt>GeometryCollection</tt> containing the face <tt>Polygon</tt>s of the diagram
	 */
	public Geometry getDiagram(GeometryFactory geomFact)
	{
		create();
		Geometry polys = subdiv.getVoronoiDiagram(geomFact);
		
    //System.out.println(polys);
		//System.out.println( subdiv.getTriangles(true, geomFact) );
		
		/*
		if (! subdiv.isFrameDelaunay()) {
      throw new IllegalStateException("Triangulation frame is not Delaunay");
    }
		//*/

    Geometry polysClean = clean(polys);
    //Geometry polysClean = polys;   //  TESTING ONLY
    
		//-- clip cell polygons to diagram boundary
		return clipGeometryCollection(polysClean, diagramEnv);
	}
	
  private static Geometry clipGeometryCollection(Geometry geom, Envelope clipEnv)
  {
    Geometry clipPoly = geom.getFactory().toGeometry(clipEnv);
    List<Geometry> clipped = new ArrayList<Geometry>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry g = geom.getGeometryN(i);
      Geometry result = null;
      // don't clip unless necessary
      if (clipEnv.contains(g.getEnvelopeInternal()))
          result = g;
      else if (clipEnv.intersects(g.getEnvelopeInternal())) {
        result = clipPoly.intersection(g);
        // keep vertex key info
        result.setUserData(g.getUserData());
      }

      if (result != null && ! result.isEmpty()) {
        clipped.add(result);
      }
    }
    return geom.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(clipped));
  }
  
	/**
	 * Cleans diagram polygons to fix invalid topology caused by robustness errors,
	 * 
	 * @param polys a GeometryCollection containing the raw polygons for the diagram
	 * @return the clean polygons
	 */
  private Geometry clean(Geometry polys) {
    /**
		 * Check for a diagram polygon with a very short edge which is invalid.
		 * This can indicate invalid diagram topology caused by nearly cocircular input points.
     * This is an efficient test which should not trigger on most typical datasets.
     * 
     * If found, snap the polygons to fix the topology.
		 * This is a heuristic fix, but should generally restore correct topology
		 * with very little effect on the diagram geometry.
		 * 
		 * See https://github.com/locationtech/jts/issues/1171
		 */
		double segmentLenTolerance = SHORT_SEG_TOLERANCE_FACTOR * diagramEnv.getDiameter();
		if (hasInvalidPolygonWithShortEdge(polys, segmentLenTolerance)) {
		  //System.out.println("SNAPPING!");
		  Geometry polysSnap = snap(polys, segmentLenTolerance);
		  return polysSnap;
		}
    return polys;
  }

  /**
   * Tests for a polygon with a very short edge which is invalid.
   * This check is efficient for valid input,
   * since that is unlikely to contain very short edges.
   * 
   * @param polys
   * @param segmentLenTolerance
   * @return true if a short edge in an invalid polygon is found
   */
  private static boolean hasInvalidPolygonWithShortEdge(Geometry polys, double segmentLenTolerance) {
    for (int i = 0; i < polys.getNumGeometries(); i++) {
      Polygon poly = (Polygon) polys.getGeometryN(i);
      if (hasShortSegment(poly, segmentLenTolerance)) {
        if (! poly.isValid())
          return true;
      }
    }
    return false;
  }
  
  /**
   * Tests if a polygon shell contains a short edge.
   * 
   * @param poly a polygon
   * @param segmentLenTolerance the minimum segment length
   * @return true if the polygon has a short edge
   */
  private static boolean hasShortSegment(Polygon poly, double segmentLenTolerance) {
    LinearRing ring = poly.getExteriorRing();
    Coordinate prev = ring.getCoordinateN(0);
    for (int i = 1; i < ring.getNumPoints(); i++) {
      Coordinate p = ring.getCoordinateN(i);
      if (p.distance(prev) < segmentLenTolerance)
        return true;
      prev = p;
    }
    return false;
  }

  /**
   * Snaps the vertices of a collection of polygons to eliminate short edges.
   * Using a snapping map for all vertices ensures that adjacent polygons 
   * match after snapping.
   * 
   * @param polys a GeometryCollection of single-ring polygons
   * @param snapTolerance the snapping tolerance
   * @return a GeometryCollection of snapped polygons
   */
  private static Geometry snap(Geometry polys, double snapTolerance) {
    SnappingPointIndex snapMap = new SnappingPointIndex(snapTolerance);
    List<Polygon> polysSnap = new ArrayList<Polygon>();
    for (int i = 0; i < polys.getNumGeometries(); i++) {
      Polygon polySnap = snapPolygon((Polygon) polys.getGeometryN(i), snapMap);
      polysSnap.add(polySnap);
    }
    GeometryFactory geomFact = polys.getFactory();
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polysSnap));  
  }

  private static Polygon snapPolygon(Polygon poly, SnappingPointIndex snapMap) {
    CoordinateList ptsSnap = new CoordinateList();
    //-- voronoi polygons do not contain holes
    Coordinate[] pts = poly.getExteriorRing().getCoordinates();
    for (Coordinate pt : pts) {
      Coordinate snapPt = snapMap.snap(pt);
      ptsSnap.add(snapPt.copy(), false);
    }
    Polygon polySnap = poly.getFactory().createPolygon(ptsSnap.toCoordinateArray());
    return polySnap;
  }

}
