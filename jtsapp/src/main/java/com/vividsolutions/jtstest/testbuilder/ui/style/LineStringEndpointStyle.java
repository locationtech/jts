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

package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.ui.Viewport;

public abstract class LineStringEndpointStyle extends LineStringStyle  {
    private boolean start;

    public LineStringEndpointStyle(boolean start) {               
        this.start = start;
    }    

    protected void paintLineString(LineString lineString, int lineType, Viewport viewport, Graphics2D graphics
        ) throws Exception {
        if (lineString.isEmpty()) {
            return;
        }

        paint(start ? lineString.getCoordinateN(0)
                    : lineString.getCoordinateN(lineString.getNumPoints() - 1),
            start ? lineString.getCoordinateN(1)
                  : lineString.getCoordinateN(lineString.getNumPoints() - 2),
            viewport, graphics);
    }

    private void paint(Coordinate terminal, Coordinate next, Viewport viewport,
        Graphics2D graphics) throws Exception {
        paint(viewport.toView(new Point2D.Double(terminal.x, terminal.y)),
            viewport.toView(new Point2D.Double(next.x, next.y)), viewport,
            graphics);
    }

    protected abstract void paint(Point2D terminal, Point2D next,
        Viewport viewport, Graphics2D graphics) throws Exception;

    
}
