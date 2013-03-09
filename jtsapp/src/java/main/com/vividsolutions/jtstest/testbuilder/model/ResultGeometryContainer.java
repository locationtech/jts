package com.vividsolutions.jtstest.testbuilder.model;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.geom.*;

public class ResultGeometryContainer
implements GeometryContainer
{
  private GeometryEditModel geomModel;
  
  public ResultGeometryContainer(GeometryEditModel geomModel) {
    this.geomModel = geomModel;
   }

  public Geometry getGeometry()
  {
    return geomModel.getResult();
  }
}
