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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testbuilder.ui.style.LayerStyle;

public class Layer 
{
  private String name = "";
  private GeometryContainer geomCont;
  private boolean isEnabled = true;
  
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
    return (BasicStyle) layerStyle.getGeomStyle();
  }
  
  public void setGeometryStyle(BasicStyle style)
  {
    layerStyle = new LayerStyle(style);
  }
  
  public Geometry getGeometry()
  {
    if (geomCont == null) return null;
    return geomCont.getGeometry();
  }

  

}
