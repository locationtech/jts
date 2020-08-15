/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui.render;

import java.awt.Color;
import java.awt.Graphics2D;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryContainer;
import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testbuilder.ui.style.LayerStyle;
import org.locationtech.jtstest.testbuilder.ui.style.Style;
import org.locationtech.jtstest.util.HSBPalette;


public class LayerRenderer implements Renderer
{
  private Layer layer;
	private GeometryContainer geomCont;
	private Viewport viewport;
	private boolean isCancelled = false;

	public LayerRenderer(Layer layer, Viewport viewport)
	{
		this(layer, layer.getSource(), viewport);
	}
	
	public LayerRenderer(Layer layer, GeometryContainer geomCont, Viewport viewport)
	{
		this.layer = layer;
		this.geomCont = geomCont;
		this.viewport = viewport;
	}
	
  public void render(Graphics2D g)
  {
    if (! layer.isEnabled()) return;
    
    try {
    	Geometry geom = getGeometry();
      if (geom == null) return;
      
      render(g, viewport, geom, layer);
      
    } catch (Exception ex) {
      System.out.println(ex);
      // not much we can do about it - just carry on
    }
  }
  
  private Geometry getGeometry()
  {
    if (geomCont == null) {
    	return null;
    }
    Geometry geom = geomCont.getGeometry();
    return geom;
  }
  
  private void render(Graphics2D g, Viewport viewport, Geometry geometry, Layer layer)
  throws Exception
  {
    // cull non-visible geometries
    // for maximum rendering speed this needs to be checked for each component
    if (! viewport.intersectsInModel(geometry.getEnvelopeInternal())) 
      return;
    if (DisplayParameters.FILL_BASIC == DisplayParameters.fillType()) {
      renderGeom(g, viewport, geometry, layer.getLayerStyle());
    }
    else {
      renderCustomFill(g, viewport, geometry, layer);
    }
  }

  private void renderCustomFill(Graphics2D g, Viewport viewport, 
      Geometry gc, Layer layer )   throws Exception
  {
    int numGeom = gc.getNumGeometries();
    HSBPalette pal = customPalette(layer.getGeometryStyle().getFillColor(), numGeom);
    /**
     * Render each element separately.
     * Otherwise it is not possible to render both filled and non-filled
     * (1D) elements correctly.
     * This also allows varying styling and cancellation.
     */
    for (int i = 0; i < numGeom; i++) {
      if (isCancelled) return;
      BasicStyle customFill = paletteFill(i, pal, layer.getGeometryStyle());
      Style st = new LayerStyle(customFill, layer.getLayerStyle().getDecoratorStyle());
      renderGeom(g, viewport, gc.getGeometryN(i), st);
    }
  }

  private void renderGeom(Graphics2D g, Viewport viewport, Geometry geometry, Style style)
  throws Exception
  {
    if (! viewport.intersectsInModel(geometry.getEnvelopeInternal())) 
      return;
    if (! (geometry instanceof GeometryCollection)) {
      style.paint(geometry, viewport, g);
      return;
    }
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      if (isCancelled) return;
      renderGeom(g, viewport, geometry.getGeometryN(i), style);
    }  
  }
  
  private static final HSBPalette PAL_RAINBOW_INCREMENTAL = HSBPalette.createRainbowIncremental(0.396f, 0.4f, 1);

  private static HSBPalette customPalette(Color clrBase, int numHues) {
    HSBPalette pal = null;
    if (DisplayParameters.FILL_VARY == DisplayParameters.fillType()) {
      float hue = ColorUtil.getHue(clrBase);
      pal = new HSBPalette(5, hue, 0.1f,
          3, 0.3f, 0.7f,
          3, 0.8f, 0.9f
          );
    }
    else if (DisplayParameters.FILL_RAINBOW == DisplayParameters.fillType()) {
      return HSBPalette.createRainbow(numHues, 0.4f, 1);
    }
    else if (DisplayParameters.FILL_RAINBOW_RANDOM == DisplayParameters.fillType()) {
      return PAL_RAINBOW_INCREMENTAL;
    }
    return pal;
  }

  private static BasicStyle paletteFill(int i, HSBPalette pal, BasicStyle style) {
    Color clrBase = style.getFillColor();
    int alpha = clrBase.getAlpha();
    Color clr = pal.color(i, alpha);
    BasicStyle st = style.copy();
    st.setFillColor(clr);
    return st;
  }

	public void cancel()
	{
		isCancelled = true;
	}
}
