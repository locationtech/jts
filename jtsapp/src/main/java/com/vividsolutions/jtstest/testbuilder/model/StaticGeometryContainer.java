package com.vividsolutions.jtstest.testbuilder.model;

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
