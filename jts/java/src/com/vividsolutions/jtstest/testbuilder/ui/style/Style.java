package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.Viewport;

public interface Style {
  void paint(Geometry geom, Viewport viewport, Graphics2D g)
  throws Exception;
}
