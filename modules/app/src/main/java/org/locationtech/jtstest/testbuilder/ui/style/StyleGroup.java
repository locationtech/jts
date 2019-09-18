package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.ui.Viewport;

public class StyleGroup implements Style {
  private Style[] styles;

  public StyleGroup(Style ... styles) {
    this.styles = styles;
  }

  @Override
  public void paint(Geometry geom, Viewport viewport, Graphics2D g) throws Exception {
    for (Style style : styles) {
      style.paint(geom, viewport, g);
    }
  }

}
