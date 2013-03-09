package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.model.GeometryContainer;
import com.vividsolutions.jtstest.testbuilder.model.Layer;
import com.vividsolutions.jtstest.testbuilder.ui.style.Style;

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
      
      render(g, viewport, geom, layer.getStyles());
      
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
  
  private void render(Graphics2D g, Viewport viewport, Geometry geometry, Style style)
  throws Exception
  {
    // cull non-visible geometries
  	// for maximum rendering speed this needs to be checked for each component
    if (! viewport.intersectsInModel(geometry.getEnvelopeInternal())) 
      return;
    
    if (geometry instanceof GeometryCollection) {
    	renderGeometryCollection(g, viewport, (GeometryCollection) geometry, style);
      return;
    }
    
    style.paint(geometry, viewport, g);
  }

  private void renderGeometryCollection(Graphics2D g, Viewport viewport, 
      GeometryCollection gc,
      Style style
      ) 
  throws Exception
  {
    /**
     * Render each element separately.
     * Otherwise it is not possible to render both filled and non-filled
     * (1D) elements correctly.
     * This also allows cancellation.
     */
    for (int i = 0; i < gc.getNumGeometries(); i++) {
    	render(g, viewport, gc.getGeometryN(i), style);
      if (isCancelled) return;
    }
  }

	public void cancel()
	{
		isCancelled = true;
	}

}
