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

package org.locationtech.jtstest.testbuilder.topostretch;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

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
	private List[] modifiedCoords;
	
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
	
  public int numVerticesInMask(Envelope mask)
  {
    VertexInMaskCountCoordinateFilter filter = new VertexInMaskCountCoordinateFilter(mask);
    if (inputGeoms[0] != null) inputGeoms[0].apply(filter);
    if (inputGeoms[1] != null) inputGeoms[1].apply(filter);
    return filter.getCount();
  }
  
  public Geometry[] stretch(double nearnessTol, double stretchDistance)
  {
    return stretch(nearnessTol, stretchDistance, null);
  }
  
  public Geometry[] stretch(double nearnessTol, double stretchDistance, Envelope mask)
  {
		this.stretchDistance = stretchDistance;
		linestrings = extractLineStrings(inputGeoms, mask);
		
		List nearVerts = StretchedVertexFinder.findNear(linestrings, nearnessTol, mask);
		
		Map coordinateMoves = getCoordinateMoves(nearVerts);
		
		Geometry[] strGeoms = new Geometry[inputGeoms.length];
		modifiedCoords = new List[inputGeoms.length];
		
		for (int i = 0; i < inputGeoms.length; i++) {
			Geometry geom = (Geometry) inputGeoms[i];
			if (geom != null) {
				GeometryVerticesMover mover = new GeometryVerticesMover(geom, coordinateMoves);
				Geometry stretchedGeom = mover.move();
				strGeoms[i] = stretchedGeom;
				modifiedCoords[i] = mover.getModifiedCoordinates();
			}
		}
		return strGeoms;
	}
	
	/**
	 * Gets the {@link Coordinate}s in each stretched geometry which were modified  (if any).
	 * 
	 * @return lists of Coordinates, one for each input geometry
	 */
	public List[] getModifiedCoordinates()
	{
		return modifiedCoords;
	}
	
	private List extractLineStrings(Geometry[] geom, Envelope mask)
	{
		List lines = new ArrayList();
		LinearComponentExtracter lineExtracter = new LinearComponentExtracter(lines);
		for (int i = 0; i < geom.length; i++ ) {
      if (geom[i] == null) continue;
      
      if (mask != null && ! mask.intersects(geom[i].getEnvelopeInternal()))
        continue;
      
			geom[i].apply(lineExtracter);
		}
    if (mask != null) {
      List masked = new ArrayList();
      for (Iterator i = lines.iterator(); i.hasNext(); ) {
        LineString line = (LineString) i.next();
        if (mask.intersects(line.getEnvelopeInternal()))
          masked.add(line);
      }
      return masked;
    }
		return lines;
	}
	
	private Map getCoordinateMoves(List nearVerts)
	{
		Map moves = new TreeMap();
		for (Iterator i = nearVerts.iterator(); i.hasNext(); ) {
			StretchedVertex nv = (StretchedVertex) i.next();
			// TODO: check if move would invalidate topology.  If yes, don't move
			Coordinate src = nv.getVertexCoordinate();
			Coordinate moved = nv.getStretchedVertex(stretchDistance);
			if (! moved.equals2D(src))
				moves.put(src, moved);
		}
		return moves;
	}
	
	private static class VertexInMaskCountCoordinateFilter
  implements CoordinateFilter
  {
    private Envelope mask;
    private int count = 0;
    
    public VertexInMaskCountCoordinateFilter(Envelope mask)
    {
      this.mask = mask;
    }
    
    public void filter(Coordinate coord)
    {
      if (mask.contains(coord))
        count++;
    }
    
    public int getCount() { return count; }
  }
}
