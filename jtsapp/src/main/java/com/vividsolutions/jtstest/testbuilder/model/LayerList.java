package com.vividsolutions.jtstest.testbuilder.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jtstest.testbuilder.geom.ComponentLocater;
import com.vividsolutions.jtstest.testbuilder.geom.GeometryLocation;

public class LayerList 
{
  public static final int LYR_A = 0;
  public static final int LYR_B = 1;
  public static final int LYR_RESULT = 2;
  
  private Layer[] layer = new Layer[3];
  
  public LayerList() 
  {
    layer[0] = new Layer("A");
    layer[1] = new Layer("B");
    layer[2] = new Layer("Result");
  }

  public int size() { return layer.length; }
  
  public Layer getLayer(int i)
  { 
    return layer[i];
  }
  
  /**
   * 
   * @param pt
   * @param tolerance
   * @return component found, or null
   */
  public Geometry getComponent(Coordinate pt, double tolerance)
  {
    for (int i = 0; i < size(); i++) {

      Layer lyr = getLayer(i);
      Geometry geom = lyr.getGeometry();
      if (geom == null) continue;
      ComponentLocater locater = new ComponentLocater(geom);
      List locs = locater.getComponents(pt, tolerance);
      if (locs.size() > 0) {
        GeometryLocation loc = (GeometryLocation) locs.get(0);
        return loc.getComponent();
      }
    }
    return null;
  }
  
  public Geometry[] getComponents(Geometry aoi)
  {
    Geometry comp[] = new Geometry[2];
    for (int i = 0; i < 2; i++) {
      Layer lyr = getLayer(i);
      Geometry geom = lyr.getGeometry();
      if (geom == null) continue;
      comp[i] = extractComponents(geom, aoi);
    }
    return comp;
  }
  
  private Geometry extractComponents(Geometry parentGeom, Geometry aoi)
  {
    ComponentLocater locater = new ComponentLocater(parentGeom);
    List locs = locater.getComponents(aoi);
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
      geoms.add(loc.getComponent());
    }
    return geoms;
  }
}
