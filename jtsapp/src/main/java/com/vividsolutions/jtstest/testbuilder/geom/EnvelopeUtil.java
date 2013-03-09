package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeUtil 
{
	public static double minExtent(Envelope env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w < h) return w;
		return h;
	}
	public static double maxExtent(Envelope env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w > h) return w;
		return h;
	}
}
