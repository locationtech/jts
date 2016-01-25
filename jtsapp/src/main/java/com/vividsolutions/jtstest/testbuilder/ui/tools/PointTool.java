/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import com.vividsolutions.jtstest.testbuilder.model.*;


/**
 * @version 1.7
 */
public class PointTool extends AbstractDrawTool 
{
    private static PointTool singleton = null;

    public static PointTool getInstance() {
        if (singleton == null)
            singleton = new PointTool();
        return singleton;
    }

    private PointTool() 
    {
    	setClickCountToFinishGesture(1);
    	setDrawBandLines(false);
    }
    
    protected int getGeometryType()
    {
    	return GeometryType.POINT;
    }
 }
