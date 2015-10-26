package com.vividsolutions.jtslab.snapround;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jtslab.geom.util.GeometryEditorEx;
import com.vividsolutions.jtslab.geom.util.GeometryEditorEx.GeometryEditorOperation;

public class PolygonCleaner implements GeometryEditorOperation {

  public static Geometry clean(Geometry geom) {
    GeometryEditorEx editor = new GeometryEditorEx(new PolygonCleaner());
    return editor.edit(geom);
  }
  
  @Override
  public Geometry edit(Geometry geometry, GeometryFactory targetFactory) {
    if (geometry instanceof Polygonal) {
      return geometry.buffer(0);
    }
    return geometry;
  }

}
