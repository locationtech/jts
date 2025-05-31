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

package org.locationtech.jtstest.testbuilder.model;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testbuilder.ui.style.LayerStyle;

public class Layer 
{
  private String name = "";
  private GeometryContainer geomCont;
  private boolean isEnabled = true;
  private boolean isModifiable = true;
  
  private LayerStyle layerStyle;
  private BasicStyle initStyle = null;
    
  public Layer(String name) {
    this.name = name;
  }

  public Layer(String name, boolean isModifiable) {
    this.name = name;
    this.isModifiable = isModifiable;
  }

  public Layer(String name, GeometryContainer source, BasicStyle style) {
    this.name = name;
    setSource(source);
    setGeometryStyle(style);
  }

  public Layer(Layer layer) {
    this.name = layer.name + "Copy";
    this.layerStyle = layer.layerStyle.copy();
    this.isEnabled = layer.isEnabled;
    this.geomCont = new StaticGeometryContainer(layer.getGeometry());
  }

  public String getName() { return name; }
  
  public void setName(String name) { 
    this.name = name; 
  }
  
  public boolean isModifiable() {
    return isModifiable;
  }
  
  public String getNameInfo() {
    if (geomCont.getGeometry() == null) return getName();
    return getName()
      + "   " + GeometryUtil.structureSummary(geomCont.getGeometry()) 
      + "  --  " + GeometryUtil.metricsSummary(geomCont.getGeometry()); 
  }
  
  public String getNameSummary() {
    if (geomCont.getGeometry() == null) return getName();
    return getName()
      + "   " + GeometryUtil.structureSummary(geomCont.getGeometry()); 
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
    return (BasicStyle) layerStyle.getGeomStyle();
  }
  
  public void setGeometryStyle(BasicStyle style)
  {
    layerStyle = new LayerStyle(style);
    if (initStyle == null) initStyle = style.copy();;
  }
  
  public Geometry getGeometry()
  {
    if (geomCont == null) return null;
    return geomCont.getGeometry();
  }

  public void setGeometry(Geometry geom)
  {
    this.geomCont = new StaticGeometryContainer(geom);
  }

  public Envelope getEnvelope() {
    if (hasGeometry()) return getGeometry().getEnvelopeInternal();
    return new Envelope();
  }
  
  public boolean hasGeometry() {
    if (geomCont == null) return false;
    return null != geomCont.getGeometry();

  }
  public void resetStyle() {
    if (initStyle == null) return;
    setGeometryStyle(initStyle.copy());
  }

}
