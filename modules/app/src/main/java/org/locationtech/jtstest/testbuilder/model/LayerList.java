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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jtstest.testbuilder.AppStrings;
import org.locationtech.jtstest.testbuilder.geom.GeometryElementLocater;
import org.locationtech.jtstest.testbuilder.geom.GeometryLocation;
import org.locationtech.jtstest.testbuilder.geom.SegmentExtracter;


public class LayerList 
{
  public static LayerList createFixed() {
    LayerList list = new LayerList();
    list.initFixed();
    return list;
  }
  
  public static LayerList create(LayerList... lists) {
    LayerList list = new LayerList();
    for (LayerList ll : lists) {
      list.add(ll);
    }
    return list;
  }
  
  public static final int LYR_A = 0;
  public static final int LYR_B = 1;
  public static final int LYR_RESULT = 2;
  
  private List<Layer> layers = new ArrayList<Layer>();
  
  public LayerList() 
  {
  }

  void initFixed() {
    layers.add(new Layer(AppStrings.GEOM_LABEL_A, false));
    layers.add(new Layer(AppStrings.GEOM_LABEL_B, false));
    layers.add(new Layer(AppStrings.GEOM_LABEL_RESULT, false));
  }
  
  public int size() { return layers.size(); }
  
  public Layer getLayer(int i)
  { 
    return layers.get(i);
  }
  
  /**
   * 
   * @param pt
   * @param tolerance
   * @return element found, or null
   */
  public Geometry getElement(Coordinate pt, double tolerance)
  {
    for (int i = 0; i < size(); i++) {

      Layer lyr = getLayer(i);
      Geometry geom = lyr.getGeometry();
      if (geom == null) continue;
      GeometryElementLocater locater = new GeometryElementLocater(geom);
      List locs = locater.getElements(pt, tolerance);
      if (locs.size() > 0) {
        GeometryLocation loc = (GeometryLocation) locs.get(0);
        return loc.getElement();
      }
    }
    return null;
  }
  
  public Geometry[] getElements(Geometry aoi)
  {
    return getElements(aoi, false);
  }
  
  public Geometry[] getElements(Geometry aoi, boolean isSegments)
  {
    Geometry comp[] = new Geometry[2];
    for (int i = 0; i < 2; i++) {
      Layer lyr = getLayer(i);
      Geometry geom = lyr.getGeometry();
      if (geom == null) continue;
      if (isSegments) {
        comp[i] = SegmentExtracter.extract(geom, aoi);
      }
      else {
        comp[i] = GeometryElementLocater.extractElements(geom, aoi);
      }
    }
    return comp;
  }
  
  /*
  private Geometry extractElements(Geometry parentGeom, Geometry aoi)
  {
    GeometryElementLocater locater = new GeometryElementLocater(parentGeom);
    List locs = locater.getElements(aoi);
    List geoms = extractLocationGeometry(locs);
    if (geoms.size() <= 0)
      return null;
    if (geoms.size() == 1) 
      return (Geometry) geoms.get(0);
    // if parent was a GC, ensure returning a GC
    if (parentGeom.getGeometryType().equals("GeometryCollection"))
      return parentGeom.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    // otherwise return MultiGeom
    return parentGeom.getFactory().buildGeometry(geoms);
  }
  
  private List extractLocationGeometry(List locs)
  {
    List geoms = new ArrayList();
    for (Iterator i = locs.iterator(); i.hasNext();) {
      GeometryLocation loc = (GeometryLocation) i.next();
      geoms.add(loc.getElement());
    }
    return geoms;
  }
  */

  public Layer add(Layer lyr, boolean atTop) {
    if (atTop) {
      layers.add(0, lyr);
    }
    else {
      layers.add(lyr);
    }
    return lyr;
  }

  public Layer copy(Layer focusLayer) {
    Layer lyr = new Layer(focusLayer);
    layers.add(lyr);
    return lyr;
  }

  public void remove(Layer lyr) {
    layers.remove(lyr);
  }

  public boolean contains(Layer lyr) {
    return layers.contains(lyr);
  }

  public boolean isTop(Layer lyr) {
    if (layers.isEmpty()) return false;
    return layers.get(0) == lyr;
  }

  public boolean isBottom(Layer lyr) {
    if (layers.isEmpty()) return false;
    return layers.get(layers.size() - 1) == lyr;
  }

  public void addTop(Layer lyr) {
    layers.add(0, lyr);
  }
  
  public void addBottom(Layer lyr) {
    layers.add(lyr);
  }
  
  public void add(LayerList lyrList) {
    layers.addAll(lyrList.layers);
  }
  
  public void moveUp(Layer lyr) {
    int i = layers.indexOf(lyr);
    if (i <= 0) return;
    Layer tmp = layers.get(i-1);
    layers.set(i-1, lyr);
    layers.set(i, tmp);
  }

  public void moveDown(Layer lyr) {
    int i = layers.indexOf(lyr);
    if (i < 0) return;
    if (i >= layers.size() - 1) return;
    Layer tmp = layers.get(i+1);
    layers.set(i+1, lyr);
    layers.set(i, tmp);
  }

  public Layer find(String name) {
    for (Layer lyr : layers) {
      if (lyr.getName().equals(name))
        return lyr;
    }
    return null;
  }
}
