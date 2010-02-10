package com.vividsolutions.jts.sde;
/**
 * <p>Title: JTS - SDE Adaptation Project</p>
 * <p>Description: Converts JTS Geometries to SDE Shapes</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Vivid Solutions Inc.</p>
 * @author Georgi Kostadinov
 * @version 1.0
 */
import com.vividsolutions.jts.geom.*;
import com.esri.sde.sdk.client.*;

public class ShapeFactory {

  public static SeShape createShape(Geometry geometry) throws SeException {
    return new GeometryAdapter(geometry);
  }

  public static SeShape[] createShapeList(GeometryCollection geometryCollection) throws SeException {
    int numberOfGeometries = geometryCollection.getNumGeometries();
    SeShape[] shapes = new SeShape[numberOfGeometries];
    for (int i=0; i<numberOfGeometries; i++) {
      shapes[i] = createShape(geometryCollection.getGeometryN(i));
    }
    return shapes;
  }
}