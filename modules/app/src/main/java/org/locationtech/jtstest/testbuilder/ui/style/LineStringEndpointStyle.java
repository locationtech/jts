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

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


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
