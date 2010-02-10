package com.vividsolutions.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

/**
 * Stretches the vertices and segments of a @link Geometry}
 * to make the topology more visible.
 * 
 * @author Martin Davis
 *
 */
public class TopologyStretcher 
{
	private double closeTol = 1e-5;
	private double stretchDist = 0.1;
	
	private List inputGeoms;
	private List linestrings;
	
	public TopologyStretcher(Geometry g)
	{
		inputGeoms = new ArrayList();
		inputGeoms.add(g);
	}
	
	public TopologyStretcher(Geometry g1, Geometry g2)
	{
		inputGeoms = new ArrayList();
		inputGeoms.add(g1);
		inputGeoms.add(g2);
	}
	
	public List stretch(double stretchDist)
	{
		this.stretchDist = stretchDist;
		linestrings = extractLineStrings(inputGeoms);
		
		List nearVerts = NearVertexFinder.findNear(linestrings, closeTol);
		
		Map coordinateMoves = getCoordinateMovesMap(nearVerts);
		
		List strGeoms = new ArrayList();
		for (int i = 0; i < inputGeoms.size(); i++) {
			Geometry geom = (Geometry) inputGeoms.get(i);
			Geometry stretchedGeom = GeometryVerticesMover.move(geom, coordinateMoves);
			strGeoms.add(stretchedGeom);
		}
		return strGeoms;
	}
	
	private List extractLineStrings(List geoms)
	{
		List lines = new ArrayList();
		LinearComponentExtracter lineExtracter = new LinearComponentExtracter(lines);
		for (Iterator i = geoms.iterator(); i.hasNext(); ) {
			Geometry g = (Geometry) i.next();
			g.apply(lineExtracter);
		}
		return lines;
	}
	
	private Map getCoordinateMovesMap(List nearVerts)
	{
		Map moves = new TreeMap();
		for (Iterator i = nearVerts.iterator(); i.hasNext(); ) {
			NearVertex nv = (NearVertex) i.next();
			// TODO: check if move would invalidate topology.  If yes, don't move
			moves.put(nv.getVertexCoordinate(), nv.getStretchedVertex(stretchDist));
		}
		return moves;
	}
	
	
}
