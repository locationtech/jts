package com.vividsolutions.jtstest.testbuilder.topostretch;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.geom.EnvelopeUtil;
import com.vividsolutions.jtstest.testbuilder.geom.GeometryContainer;
import com.vividsolutions.jtstest.testbuilder.model.GeometryEditModel;

public class GeometryStretcherView 
{
	private GeometryEditModel geomModel;
	private Geometry[] stretchCache = new Geometry[2];
	private double stretchDistance = 5.0;
	
	public GeometryStretcherView(GeometryEditModel geomEditModel)
	{
		this.geomModel = geomEditModel;
	}
	
	public GeometryContainer getContainer(int i)
	{
		return new StretchedGeometryContainer(i);
	}
	
	public void setStretchDistance(double stretchDistance)
	{
		this.stretchDistance = stretchDistance;
	}
	
	public void setEnvelope(Envelope viewEnv)
	{
		// clear cache
		stretchCache = null;
		// TODO: compute stretch distance from viewEnv
		double size = EnvelopeUtil.minExtent(viewEnv);
	}
	
	private Geometry getStretchedGeometry(int index)
	{
		updateCache();
		return stretchCache[index];
	}
	
	private synchronized void updateCache()
	{
		if (! isCacheValid()) {
			Geometry g0 = geomModel.getGeometry(0);
			Geometry g1 = geomModel.getGeometry(1);
			TopologyStretcher stretcher = new TopologyStretcher(g0, g1);
			// TODO: is the closeness tolerance right?
			stretchCache = stretcher.stretch(stretchDistance/20, stretchDistance);
		}
	}
	
	private boolean isCacheValid()
	{
		if (stretchCache == null) {
			stretchCache = new Geometry[2];
			return false;
		}
		if (geomModel.getGeometry(0) != stretchCache[0]) return false;
		if (geomModel.getGeometry(1) != stretchCache[1]) return false;
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
