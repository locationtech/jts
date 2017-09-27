package org.locationtech.jtstest.geomfunction;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.function.FunctionsUtil;
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
    return fun.getName() + repeatAnnotation();
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

  private String repeatAnnotation() {
    return "*" + count;
  }

  public boolean isBinary() {
    return fun.isBinary();
  }
  
  public Object invoke(Geometry geom, Object[] args) {
    
    if (! isRepeatable(fun)) {
      throw new IllegalArgumentException("Cannot repeat function whose argumnent is not a double");
    }
    
    //TODO: handle repeating methods with integer arg
    int repeatArgIndex = repeatableArgIndex(fun);
    Double argStart = ClassUtil.toDouble(args[repeatArgIndex]);
    return invokeRepeated(geom, args, argStart);
  }

  public static boolean isRepeatable(GeometryFunction fun) {
    if (! (fun.getReturnType() ==  Geometry.class )) return false;
    
    Class[] paramType = fun.getParameterTypes();
    int repeatArgIndex = repeatableArgIndex(fun);
    // abort if no repeatable parameter
    if (paramType.length < repeatArgIndex + 1) return false;
    Class type = paramType[repeatArgIndex];
    if (! ClassUtil.isDouble(type)) return false;
    
    /*
    Double argBase = ClassUtil.toDouble(args[0]);
    if (argBase == null) return false;
    */
    
    return true;
  }

  public static int repeatableArgIndex(GeometryFunction fun) {
    if (fun.isBinary()) return 1;
    return 0;
  }
  private Object invokeRepeated(Geometry geom, Object[] args, double argStart) {
    Geometry[] results = new Geometry[count];
    int repeatArgIndex = repeatableArgIndex(fun);
    for (int i = 1; i <= count; i++) {
      double val = argStart * i;
      Geometry result = (Geometry) fun.invoke(geom, copyArgs(args, repeatArgIndex, val));
      FunctionsUtil.showIndicator(result);
      results[i-1] = result;
    }
    return geom.getFactory().createGeometryCollection(results);
  }

  private Object[] copyArgs(Object[] args, int replaceIndex, double val) {
    Object[] newArgs = args.clone();
    newArgs[replaceIndex] = val;
    return newArgs;
  }

}
