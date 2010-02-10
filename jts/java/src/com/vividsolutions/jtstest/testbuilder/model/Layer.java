package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.*;

import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.geom.*;
import com.vividsolutions.jtstest.testbuilder.ui.render.*;
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

  /*
  public GeometryCo getGeometryBuilder()
  {
    return geomCont;
  }
  */
  
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
  
}
