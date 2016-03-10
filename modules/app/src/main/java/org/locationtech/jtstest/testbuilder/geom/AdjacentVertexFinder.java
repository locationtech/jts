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

package org.locationtech.jtstest.testbuilder.geom;

import java.util.*;

import org.locationtech.jts.geom.*;


/**
 * Locates all vertices in a geometry which are adjacent 
 * to a given vertex.
 * 
 * @author mbdavis
 *
 */
public class AdjacentVertexFinder 
{
  public static Coordinate[] findVertices(Geometry geom, Coordinate testPt)
  {
  	AdjacentVertexFinder finder = new AdjacentVertexFinder(geom);
    return finder.getVertices(testPt);
  }

  private Geometry geom;
  private Coordinate vertexPt;
  private int vertexIndex = -1;
  
  public AdjacentVertexFinder(Geometry geom) {
    this.geom = geom;
  }
  
  public Coordinate[] getVertices(Coordinate testPt)
  {
  	AdjacentVertexFilter filter = new AdjacentVertexFilter(testPt);
    geom.apply(filter);
    return filter.getVertices();
  }
  
  public int getIndex() { return vertexIndex; }
  
  static class AdjacentVertexFilter implements CoordinateSequenceFilter
  {
    private Coordinate basePt;
    private List adjVerts = new ArrayList();
    
    public AdjacentVertexFilter(Coordinate basePt)
    {
      this.basePt = basePt;
    }

    public void filter(CoordinateSequence seq, int i)
    {
      Coordinate p = seq.getCoordinate(i);
      if (! p.equals2D(basePt))
      	return;

      if (i > 0) {
      	adjVerts.add(seq.getCoordinate(i - 1));
      }
      if (i < seq.size() - 1) {
      	adjVerts.add(seq.getCoordinate(i + 1));
      }
    }
    
    public Coordinate[] getVertices() 
    {
      return CoordinateArrays.toCoordinateArray(adjVerts);
    }
    
    public boolean isDone() { return false; }

    public boolean isGeometryChanged() { return false; }
  }

}
