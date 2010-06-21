package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.*;

import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.geom.*;
import com.vividsolutions.jtstest.testbuilder.model.LayerList.LayerListRenderer;
import com.vividsolutions.jtstest.testbuilder.ui.render.*;
import com.vividsolutions.jtstest.testbuilder.ui.style.ArrowEndpointStyle;
import com.vividsolutions.jtstest.testbuilder.ui.style.BasicStyle;
import com.vividsolutions.jtstest.testbuilder.ui.style.CircleEndpointStyle;
import com.vividsolutions.jtstest.testbuilder.ui.style.LineOrientationStyle;
import com.vividsolutions.jtstest.testbuilder.ui.style.Style;
import com.vividsolutions.jtstest.testbuilder.ui.style.StyleList;
import com.vividsolutions.jtstest.testbuilder.ui.style.VertexStyle;
import com.vividsolutions.jts.geom.*;

public class Layer 
{
  private String name = "";
  private GeometryContainer geomCont;
  private boolean isEnabled = true;
  
  private BasicStyle style = new BasicStyle();
  private VertexStyle vertexStyle;
  private LineOrientationStyle segArrowStyle;
  private ArrowEndpointStyle lineArrowStyle;
  private CircleEndpointStyle lineCircleStyle;
  
  private StyleList styleList;
  
  private StyleList.StyleFilter vertexFilter = new StyleList.StyleFilter() {
  	public boolean isFiltered(Style style) {
  		return ! TestBuilderModel.isShowingVertices();
  	}
  };
  
  private StyleList.StyleFilter decorationFilter = new StyleList.StyleFilter() {
  	public boolean isFiltered(Style style) {
  		return ! TestBuilderModel.isShowingOrientations();
  	}
  };
  
  private static Color lighter(Color clr)
  {
    float[] hsb = new float[3];
    Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), hsb);
    hsb[1] *= 0.4;
    return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
  }
  
  public Layer(String name) {
    this.name = name;
  }

  public String getName() { return name; }
  
  public void setEnabled(boolean isEnabled)
  {
    this.isEnabled = isEnabled;
  }
  
  public void setGeometry(GeometryContainer geomCont)
  {
    this.geomCont = geomCont;
  }
  
  public void setStyle(BasicStyle style)
  {
    this.style = style;
    vertexStyle = new VertexStyle(style.getLineColor());
    segArrowStyle = new LineOrientationStyle(lighter(style.getLineColor()));
    lineArrowStyle = new ArrowEndpointStyle(lighter(style.getLineColor()), false, true);
    lineCircleStyle = new CircleEndpointStyle(lighter(style.getLineColor()), true, false);
    
    styleList = new StyleList();
    styleList.add(style);
    styleList.add(vertexStyle, vertexFilter);
    styleList.add(segArrowStyle, decorationFilter);
    styleList.add(lineArrowStyle, decorationFilter);
    styleList.add(lineCircleStyle, decorationFilter);
  }
  
  public Geometry getGeometry()
  {
    if (geomCont == null) return null;
    return geomCont.getGeometry();
  }

  public void paint(Graphics2D g, Viewport viewport)
  {
    if (! isEnabled) return;
    if (geomCont == null) return;
    
    try {
      Geometry geom = geomCont.getGeometry();
      if (geom == null) return;
      
      // cull non-visible geometries
      if (! viewport.intersectsInModel(geom.getEnvelopeInternal())) 
        return;
      
      GeometryPainter.paint(g, viewport, geom, styleList);
      
    } catch (Exception ex) {
      System.out.println(ex);
      // not much we can do about it - just carry on
    }
  }
  
  public Renderer getRenderer(Viewport viewport)
  {
  	return new LayerRenderer(viewport);
  }

  class LayerRenderer implements Renderer
  {
  	private Viewport viewport;
  	private boolean isCancelled = false;

  	public LayerRenderer(Viewport viewport)
  	{
  		this.viewport = viewport;
  	}
  	
    public void render(Graphics2D g)
    {
      if (! isEnabled) return;
      if (geomCont == null) return;
      
      try {
        Geometry geom = geomCont.getGeometry();
        if (geom == null) return;
        
        render(g, viewport, geom, styleList);
        
      } catch (Exception ex) {
        System.out.println(ex);
        // not much we can do about it - just carry on
      }
    	
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
       * (1D) elements correctly
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
}
