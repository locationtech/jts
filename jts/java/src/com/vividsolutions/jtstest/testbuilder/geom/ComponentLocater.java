package com.vividsolutions.jtstest.testbuilder.geom;

import java.util.*;

import com.vividsolutions.jts.geom.*;

/**
 * Locates the components of a Geometry
 * which lie in a target area.
 * 
 * @author Martin Davis
 * @see FacetLocater
 */
public class ComponentLocater {

  private Geometry parentGeom;
  private List components = new ArrayList();
  private Geometry aoi;

  public ComponentLocater(Geometry parentGeom) {
    this.parentGeom = parentGeom;
  }
  
  /**
   * 
   * @param queryPt
   * @param tolerance
   * @return a List of the component Geometrys
   */
  public List getComponents(Coordinate queryPt, double tolerance)
  {
    //Coordinate queryPt = queryPt;
    //this.tolerance = tolerance;
    aoi = createAOI(queryPt, tolerance);
    return getComponents(aoi);
  }

  public List getComponents(Geometry aoi)
  {
    //Coordinate queryPt = queryPt;
    //this.tolerance = tolerance;
    this.aoi = aoi;
    findComponents(new Stack(), parentGeom, components);
    return components;
  }

  private Geometry createAOI(Coordinate queryPt, double tolerance)
  {
    Envelope env = new Envelope(queryPt);
    env.expandBy(2 * tolerance);
    return parentGeom.getFactory().toGeometry(env);
  }
  
  private void findComponents(Stack path, Geometry geom, List components)
  {
    if (geom instanceof GeometryCollection) {
      for (int i = 0; i < geom.getNumGeometries(); i++ ) {
        Geometry subGeom = geom.getGeometryN(i);
  			path.push(new Integer(i));
        findComponents(path, subGeom, components);
        path.pop();
      }
      return;
    }
    // TODO: make this robust - do not use Geometry.intersects()
    // atomic component - check for match
    if (aoi.intersects(geom))
      components.add(new GeometryLocation(parentGeom, geom, 
      		FacetLocater.toIntArray(path)));
  }

}
