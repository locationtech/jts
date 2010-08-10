package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.Geometry;

public class StaticGeometryContainer implements GeometryContainer {

	private Geometry geometry;
	
	public StaticGeometryContainer(Geometry geometry)
	{
		this.geometry = geometry;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

}
