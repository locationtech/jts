package com.vividsolutions.jtstest.testbuilder.model;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.geom.*;

public class IndexedGeometryContainer
implements GeometryContainer
{
  private GeometryEditModel geomModel;
  private int index;
  
  public IndexedGeometryContainer(GeometryEditModel geomModel, int index) {
    this.geomModel = geomModel;
    this.index = index;
  }

  public Geometry getGeometry()
  {
    return geomModel.getGeometry(index);
  }

}
