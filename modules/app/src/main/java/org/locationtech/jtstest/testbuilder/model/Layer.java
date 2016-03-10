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
  
  private BasicStyle style = new BasicStyle();
  
  private StyleList styleList;
  
  private StyleList.StyleFilter vertexFilter = new StyleList.StyleFilter() {
  	public boolean isFiltered(Style style) {
  		return ! TestBuilderModel.isShowingVertices();
  	}
  };
  
  private StyleList.StyleFilter decorationFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! TestBuilderModel.isShowingOrientation();
    }
  };
    
  private StyleList.StyleFilter structureFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! TestBuilderModel.isShowingStructure();
    }
  };
    
  private StyleList.StyleFilter labelFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! TestBuilderModel.isShowingLabel();
    }
  };
    
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
  public StyleList getStyles()
  {
  	return styleList;
  }
  public void setStyle(BasicStyle style)
  {
    this.style = style;
    VertexStyle vertexStyle = new VertexStyle(style.getLineColor());
    ArrowLineStyle segArrowStyle = new ArrowLineStyle(ColorUtil.lighter(style.getLineColor(), 0.8));
    ArrowEndpointStyle lineArrowStyle = new ArrowEndpointStyle(ColorUtil.lighter(style.getLineColor(),0.2), false, true);
    CircleEndpointStyle lineCircleStyle = new CircleEndpointStyle(style.getLineColor(), 6, true, true);
    PolygonStructureStyle polyStyle = new PolygonStructureStyle(ColorUtil.opaque(style.getLineColor()));
    SegmentIndexStyle indexStyle = new SegmentIndexStyle(ColorUtil.opaque(style.getLineColor().darker()));
    DataLabelStyle dataLabelStyle = new DataLabelStyle(ColorUtil.opaque(style.getLineColor().darker()));
    
    // order is important here
    styleList = new StyleList();
    styleList.add(vertexStyle, vertexFilter);
    styleList.add(segArrowStyle, decorationFilter);
    styleList.add(lineArrowStyle, decorationFilter);
    styleList.add(lineCircleStyle, decorationFilter);
    styleList.add(style);
    styleList.add(polyStyle, structureFilter);
    styleList.add(indexStyle, structureFilter);
    styleList.add(dataLabelStyle, labelFilter);
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
      
    } 
    catch (Exception ex) {
      // not much we can do about an exception while rendering, so just carry on
      System.out.println("Exception in Layer.paint(): " + ex);
    }
  }
  

}
