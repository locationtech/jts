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

package org.locationtech.jtstest.testbuilder.model;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.topostretch.TopologyStretcher;


public class GeometryStretcherView 
{
  /**
   * The maximum number of vertices which can be shown.
   * This is chosen to ensure reasonable performance for rendering.
   */
  private static final int MAX_VERTICES_IN_MASK = 500;
  
  /**
   * The nearness tolerance in view pixels.
   * This is chosen to be as large as possible
   * (which minimizes change to geometries)
   * but small enough that points which appear
   * to be coincident on the screen at a given zoom level
   * will be magnified.
   * 
   */
  public static final  double NEARNESS_TOL_IN_VIEW = 1;
  
	private GeometryEditModel geomModel;
	private Geometry[] stretchGeom = new Geometry[2];
	private List[] stretchCoords;
  private boolean isViewPerformant = true;
  private Envelope maskEnv = null;
	private double stretchSize = 5.0;
  private double nearnessTol = 0.5;
	
	public GeometryStretcherView(GeometryEditModel geomEditModel)
	{
		this.geomModel = geomEditModel;
	}
	
	public GeometryContainer getContainer(int i)
	{
		return new StretchedGeometryContainer(i);
	}
	
  /**
   * Sets the amount by which vertices will be stretched
   * (in geometry units).
   * 
   * @param stretchSize
   */
	public void setStretchSize(double stretchSize)
	{
		this.stretchSize = stretchSize;
	}
	
  public void setNearnessTolerance(double nearnessTol)
  {
    this.nearnessTol = nearnessTol;
  }
	public void setEnvelope(Envelope maskEnv)
	{
    this.maskEnv = maskEnv;
		// clear cache
		stretchGeom = null;
	}
	
  public boolean isViewPerformant()
  {
    updateCache();
    return isViewPerformant;

  }
	public Geometry getStretchedGeometry(int index)
	{
		updateCache();
		return stretchGeom[index];
	}
	
	public List getStretchedVertices(int index)
	{
		updateCache();
		return stretchCoords[index];
	}
	
	private synchronized void updateCache()
	{
		if (! isCacheValid()) {
			Geometry g0 = geomModel.getGeometry(0);
			Geometry g1 = geomModel.getGeometry(1);
    
			TopologyStretcher stretcher = new TopologyStretcher(g0, g1);
      
      // check if view is valid (performant enough)  to render
      if (maskEnv != null) {
        isViewPerformant = stretcher.numVerticesInMask(maskEnv) < MAX_VERTICES_IN_MASK;
      }
      if (! isViewPerformant)
        return;

			stretchGeom = stretcher.stretch(nearnessTol, stretchSize, maskEnv);
			stretchCoords = stretcher.getModifiedCoordinates();
		}
	}
	
	private boolean isCacheValid()
	{
		if (stretchGeom == null) {
			stretchGeom = new Geometry[2];
			return false;
		}
		// don't bother checking this any more, since stretchView is always created new
		//if (geomModel.getGeometry(0) != stretchGeom[0]) return false;
		//if (geomModel.getGeometry(1) != stretchGeom[1]) return false;
		return true;
	}
	
	private class StretchedGeometryContainer implements GeometryContainer
	{
	  private int index;
	  
	  public StretchedGeometryContainer(int index) 
	  {
	    this.index = index;
	  }

	  public Geometry getGeometry()
	  {
	    return getStretchedGeometry(index);
	  }

	}
}
