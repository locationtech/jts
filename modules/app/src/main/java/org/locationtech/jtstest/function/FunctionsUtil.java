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

package org.locationtech.jtstest.function;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.ui.render.GeometryPainter;


public class FunctionsUtil {

	public static final Envelope DEFAULT_ENVELOPE = new Envelope(0, 100, 0, 100);
	
	public static Envelope getEnvelopeOrDefault(Geometry g)
	{
		if (g == null) return DEFAULT_ENVELOPE;
		return g.getEnvelopeInternal();
	}
	
  public static GeometryFactory getFactoryOrDefault(Geometry g)
  {
    if (g == null) return JTSTestBuilder.getGeometryFactory();
    return g.getFactory();
  }
  
  public static GeometryFactory getFactoryOrDefault(Geometry g1, Geometry g2)
  {
    if (g1 != null) return g1.getFactory();
    if (g2 != null) return g2.getFactory();
    return JTSTestBuilder.getGeometryFactory(); 
  }
  
  public static boolean isShowingIndicators() {
    return JTSTestBuilderFrame.isShowingIndicators();
  }
  
  public static void showIndicator(Geometry geom)
  {
    showIndicator(geom, AppConstants.INDICATOR_LINE_CLR);
  }
  
  public static void showIndicator(Geometry geom, Color lineClr)
  {
    JTSTestBuilder.controller().indicatorShow(geom, lineClr);
  }
  
  public static Geometry buildGeometry(List geoms, Geometry parentGeom)
  {
    if (geoms.size() <= 0)
      return null;
    if (geoms.size() == 1) 
      return (Geometry) geoms.get(0);
    // if parent was a GC, ensure returning a GC
    if (parentGeom != null && parentGeom.getGeometryType().equals("GeometryCollection"))
      return parentGeom.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    // otherwise return MultiGeom
    return getFactoryOrDefault(parentGeom).buildGeometry(geoms);
  }
  
  public static Geometry buildGeometry(Geometry[] geoms)
  {
    GeometryFactory gf = getFactory(geoms);
    
    List<Geometry> geomList = new ArrayList<Geometry>();
    for (Geometry geom : geoms) {
      if (geom != null) {
        geomList.add(geom);
        if (gf == null) 
          gf = geom.getFactory();
      }
    }
    return gf.buildGeometry(geomList);
  }
  
  public static Geometry buildGeometryCollection(Geometry[] geoms, Geometry nullGeom)
  {
    GeometryFactory gf = getFactory(geoms);
    
    Geometry[] geomArray = new Geometry[geoms.length];
    for (int i = 0; i < geoms.length; i++) {
      Geometry srcGeom = geoms[i] == null ? nullGeom : geoms[i];
      if (srcGeom != null) {
        geomArray[i] = srcGeom.copy();
      }
    }
    return gf.createGeometryCollection(geomArray);
  }

  private static GeometryFactory getFactory(Geometry[] geoms) {
    GeometryFactory gf = JTSTestBuilder.getGeometryFactory();
    if (geoms.length > 0) {
      gf = getFactoryOrDefault(geoms[0]);
    }
    return gf;
  }
  
  public static Geometry buildGeometry(Geometry a, Geometry b) {
    Geometry[] geoms = toGeometryArray(a, b);
    return getFactoryOrDefault(a, b).createGeometryCollection(geoms);  }

  public static Geometry[] toGeometryArray(Geometry a, Geometry b) {
    int size = 0;
    if (a != null) size++;
    if (b != null) size++;
    Geometry[] geoms = new Geometry[size];
    size = 0;
    if (a != null) geoms[size++] = a;
    if (b != null) geoms[size] = b;
    return geoms;
  }
  
  public static List<Geometry> elements(Geometry g)
  {
    List<Geometry> comp = new ArrayList<Geometry>();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      comp.add(g.getGeometryN(i));
    }
    return comp;
  }
}
