package org.locationtech.jtstest.geomfunction;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.function.FunctionsUtil;
import org.locationtech.jtstest.util.ClassUtil;

public class SpreaderGeometryFunction implements GeometryFunction {

  private GeometryFunction fun;

  public SpreaderGeometryFunction(GeometryFunction fun) {
    this.fun = fun;
  }
  
  public String getCategory() {
    return fun.getCategory();
  }

  public String getName() {
    return fun.getName() + "-Each";
  }

  public String getDescription() {
    return fun.getDescription();
  }

  public String[] getParameterNames() {
    return fun.getParameterNames();
  }

  public Class[] getParameterTypes() {
    return fun.getParameterTypes();
  }

  public Class getReturnType() {
    return fun.getReturnType();
  }

  public String getSignature() {
    return fun.getSignature();
  }

  public boolean isBinary() {
    return fun.isBinary();
  }
  
  public Object invoke(Geometry geom, Object[] args) {
    int nElt = geom.getNumGeometries();
    Geometry[] results = new Geometry[nElt];
    for (int i = 0; i < nElt; i++) {
      Geometry elt = geom.getGeometryN(i);
      Geometry result = (Geometry) fun.invoke(elt, args);
      //FunctionsUtil.showIndicator(result);
      results[i] = result;
    }
    return geom.getFactory().createGeometryCollection(results);
  }


}
