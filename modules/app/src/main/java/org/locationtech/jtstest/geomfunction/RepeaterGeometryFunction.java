package org.locationtech.jtstest.geomfunction;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.util.ClassUtil;

public class RepeaterGeometryFunction implements GeometryFunction {

  private GeometryFunction fun;
  private int count;

  public RepeaterGeometryFunction(GeometryFunction fun, int count) {
    this.fun = fun;
    this.count = count;
  }
  
  public String getCategory() {
    return fun.getCategory();
  }

  public String getName() {
    return fun.getName();
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

  public Object invoke(Geometry geom, Object[] args) {
    
    if (! isRepeatable(fun)) {
      throw new IllegalArgumentException("Cannot repeat function whose argumnent is not a double");
    }
    
    //TODO: handle repeating methods with integer arg
    Double argBase = ClassUtil.toDouble(args[0]);
    return invokeRepeated(geom, args, argBase);
  }

  public static boolean isRepeatable(GeometryFunction fun) {
    if (! (fun.getReturnType() ==  Geometry.class )) return false;
    
    Class[] paramType = fun.getParameterTypes();
    // nothing to change by repeating
    if (paramType.length < 1) return false;
    if (! ClassUtil.isDouble(paramType[0])) return false;
    
    /*
    Double argBase = ClassUtil.toDouble(args[0]);
    if (argBase == null) return false;
    */
    
    return true;
  }

  private Object invokeRepeated(Geometry geom, Object[] args, double argBase) {
    Geometry[] results = new Geometry[count];
    for (int i = 1; i <= count; i++) {
      double val = argBase * i;
      Geometry result = (Geometry) fun.invoke(geom, copyArgs(args, val));
      results[i-1] = result;
    }
    return geom.getFactory().createGeometryCollection(results);
  }

  private Object[] copyArgs(Object[] args, double val) {
    Object[] newArgs = new Object[args.length];
    args[0] = val;
    for (int i = 1; i < args.length; i++) {
      newArgs[i] = args[i];
    }
    return args;
  }

  public boolean isBinary() {
    return fun.isBinary();
  }

}
