package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;

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
