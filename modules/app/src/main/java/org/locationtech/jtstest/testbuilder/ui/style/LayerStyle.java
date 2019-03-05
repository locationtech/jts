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

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JCheckBox;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;

public class LayerStyle implements Style  {

  private BasicStyle geomStyle;
  private StyleList decoratorStyle;
  private VertexStyle vertexStyle;

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

  public LayerStyle(BasicStyle geomStyle) {
    this.geomStyle = geomStyle;
    setGeometryStyle(geomStyle);
  }
  
  public LayerStyle(BasicStyle geomStyle, StyleList decoratorStyle) {
    this.geomStyle = geomStyle;
    this.decoratorStyle = decoratorStyle;
  }
  
  public BasicStyle getGeomStyle() {
    return geomStyle;
  }

  public StyleList getDecoratorStyle() {
    return decoratorStyle;
  }

  private void setGeometryStyle(BasicStyle style)
  {
    vertexStyle = new VertexStyle(style.getLineColor());
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
    
    decoratorStyle = styleList;
  }

  public void setVertices(boolean show) {
    decoratorStyle.setEnabled(vertexStyle, show);
  }
  
  public boolean isVertices() {
    return decoratorStyle.isEnabled(vertexStyle);
  }
  
  public int getVertexSize() {
    return vertexStyle.getSize();
  }
  public void setVertexSize(int size) {
    vertexStyle.setSize(size);
  }
  
  public Color getVertexColor() {
    return vertexStyle.getColor();
  }
  public void setVertexColor(Color color) {
    vertexStyle.setColor(color);
  }
  
  public void paint(Geometry geom, Viewport viewport, Graphics2D g) throws Exception {
    geomStyle.paint(geom, viewport, g);
    decoratorStyle.paint(geom, viewport, g);
  }





}
