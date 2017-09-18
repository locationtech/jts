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

package org.locationtech.jtstest.testbuilder.model;

import java.awt.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.geom.*;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.render.*;
import org.locationtech.jtstest.testbuilder.ui.style.*;



public class Layer 
{
  private String name = "";
  private GeometryContainer geomCont;
  private boolean isEnabled = true;
  
  private BasicStyle geomStyle = new BasicStyle();
  
  private StyleList.StyleFilter vertexFilter = new StyleList.StyleFilter() {
  	public boolean isFiltered(Style style) {
  		return ! DisplayParameters.isShowingVertices();
  	}
  };
  
  private StyleList.StyleFilter decorationFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingOrientation();
    }
  };
    
  private StyleList.StyleFilter structureFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingStructure();
    }
  };
    
  private StyleList.StyleFilter labelFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingLabel();
    }
  };
  private LayerStyle layerStyle;
    
  public Layer(String name) {
    this.name = name;
  }

  public String getName() { return name; }
  
  public String getNameInfo() {
    if (geomCont.getGeometry() == null) return getName();
    return getName()
      + "   " + GeometryUtil.structureSummary(geomCont.getGeometry()) 
      + "  --  " + GeometryUtil.metricsSummary(geomCont.getGeometry()); 
  }
  
  public void setEnabled(boolean isEnabled)
  {
    this.isEnabled = isEnabled;
  }
  
  public void setSource(GeometryContainer geomCont)
  {
    this.geomCont = geomCont;
  }
  
  public GeometryContainer getSource()
  {
    return geomCont;
  }
  
  public boolean isEnabled()
  {
  	return isEnabled;
  }
  public LayerStyle getLayerStyle()
  {
    return layerStyle;
  }
  public BasicStyle getGeometryStyle()
  {
    return geomStyle;
  }
  public void setGeometryStyle(BasicStyle style)
  {
    this.geomStyle = style;
    VertexStyle vertexStyle = new VertexStyle(style.getLineColor());
    ArrowLineStyle segArrowStyle = new ArrowLineStyle(ColorUtil.lighter(style.getLineColor(), 0.8));
    ArrowEndpointStyle lineArrowStyle = new ArrowEndpointStyle(ColorUtil.lighter(style.getLineColor(),0.5), false, true);
    CircleEndpointStyle lineCircleStyle = new CircleEndpointStyle(style.getLineColor(), 6, true, true);
    PolygonStructureStyle polyStyle = new PolygonStructureStyle(ColorUtil.opaque(style.getLineColor()));
    SegmentIndexStyle indexStyle = new SegmentIndexStyle(ColorUtil.opaque(style.getLineColor().darker()));
    DataLabelStyle dataLabelStyle = new DataLabelStyle(ColorUtil.opaque(style.getLineColor().darker()));
    
    // order is important here
    StyleList styleList = new StyleList();
    styleList.add(vertexStyle, vertexFilter);
    styleList.add(segArrowStyle, decorationFilter);
    styleList.add(lineArrowStyle, decorationFilter);
    styleList.add(lineCircleStyle, decorationFilter);
    //styleList.add(style);
    styleList.add(polyStyle, structureFilter);
    styleList.add(indexStyle, structureFilter);
    styleList.add(dataLabelStyle, labelFilter);
    
    layerStyle = new LayerStyle(style, styleList);
  }
  
  public Geometry getGeometry()
  {
    if (geomCont == null) return null;
    return geomCont.getGeometry();
  }

  

}
