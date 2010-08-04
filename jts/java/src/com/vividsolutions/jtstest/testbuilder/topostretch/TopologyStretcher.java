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
	private double stretchDistance = 0.1;
	
	private Geometry[] inputGeoms;
	private List linestrings;
	
	public TopologyStretcher(Geometry g)
	{
		inputGeoms = new Geometry[1];
		inputGeoms[0] = g;
	}
	
	public TopologyStretcher(Geometry g1, Geometry g2)
	{
		inputGeoms = new Geometry[2];
		inputGeoms[0] = g1;
		inputGeoms[1] = g2;
	}
	
	public Geometry[] stretch(double nearnessTol, double stretchDistance)
	{
		this.stretchDistance = stretchDistance;
		linestrings = extractLineStrings(inputGeoms);
		
		List nearVerts = NearVertexFinder.findNear(linestrings, nearnessTol);
		
		Map coordinateMoves = getCoordinateMovesMap(nearVerts);
		
		Geometry[] strGeoms = new Geometry[inputGeoms.length];
		for (int i = 0; i < inputGeoms.length; i++) {
			Geometry geom = (Geometry) inputGeoms[i];
			if (geom != null) {
				Geometry stretchedGeom = GeometryVerticesMover.move(geom, coordinateMoves);
				strGeoms[i] = stretchedGeom;
			}
		}
		return strGeoms;
	}
	
	private List extractLineStrings(Geometry[] geom)
	{
		List lines = new ArrayList();
		LinearComponentExtracter lineExtracter = new LinearComponentExtracter(lines);
		for (int i = 0; i < geom.length; i++ ) {
			if (geom[i] != null)
				geom[i].apply(lineExtracter);
		}
		return lines;
	}
	
	private Map getCoordinateMovesMap(List nearVerts)
	{
		Map moves = new TreeMap();
		for (Iterator i = nearVerts.iterator(); i.hasNext(); ) {
			NearVertex nv = (NearVertex) i.next();
			// TODO: check if move would invalidate topology.  If yes, don't move
			moves.put(nv.getVertexCoordinate(), nv.getStretchedVertex(stretchDistance));
		}
		return moves;
	}
	
	
}
