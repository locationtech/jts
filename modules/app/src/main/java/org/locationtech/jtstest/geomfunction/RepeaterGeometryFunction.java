/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.geomfunction;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.function.FunctionsUtil;
import org.locationtech.jtstest.util.ClassUtil;

/**
 * Repeats a function a given number of times.
 * If the function has a single numeric argument, 
 * the argument will be multiplied by the repeat counter for every call, 
 * and the function results will be accumulated 
 * into a collection to provide the final result.
 * 
 * @author Martin Davis
 *
 */
public class RepeaterGeometryFunction implements GeometryFunction {

  private GeometryFunction fun;
  private int count;
  private boolean hasRepeatableArg;

  public RepeaterGeometryFunction(GeometryFunction fun, int count) {
    this.fun = fun;
    this.count = count;
    hasRepeatableArg = hasRepeatableArg(fun);
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
  
  public boolean isRequiredB() {
    return fun.isRequiredB();
  }
  
  public Object invoke(Geometry geom, Object[] args) {
    
    if (! isRepeatable(fun)) {
      throw new IllegalArgumentException("Cannot repeat function whose argumnent is not a double");
    }
    
    //TODO: handle repeating methods with integer arg
    int repeatArgIndex = repeatableArgIndex(fun);
    double argStart = 0;
    if (repeatArgIndex < args.length)
      argStart = ClassUtil.toDouble(args[repeatArgIndex]);
    return invokeRepeated(geom, args, argStart);
  }

  public static boolean isRepeatable(GeometryFunction fun) {
    if (! (fun.getReturnType() ==  Geometry.class )) return false;
    
    Class[] paramType = fun.getParameterTypes();
    int repeatArgIndex = repeatableArgIndex(fun);
    
    // allow no repeatable arg
    if (paramType.length < repeatArgIndex + 1) return true;
    
    Class type = paramType[repeatArgIndex];
    if (! ClassUtil.isDouble(type)) return false;
    
    return true;
  }

  public static boolean hasRepeatableArg(GeometryFunction fun) {
    Class[] paramType = fun.getParameterTypes();
    int numParam = paramType.length;
    int numGeomParam = fun.isBinary() ? 1 : 0;
    return numParam > numGeomParam;
  }
  
  public static int repeatableArgIndex(GeometryFunction fun) {
    if (fun.isBinary()) return 1;
    return 0;
  }
  
  private Object invokeRepeated(Geometry geom, Object[] args, double argStart) {
    List results = new ArrayList();
    int repeatArgIndex = repeatableArgIndex(fun);
    for (int i = 1; i <= count; i++) {
      double val = argStart * i;
      Geometry result = (Geometry) fun.invoke(geom, copyArgs(args, repeatArgIndex, val));
      if (result == null) continue;
      //System.out.println("Repeat: " + i);
      if (hasRepeatableArg || i == 1) {
        FunctionsUtil.showIndicator(result);
        results.add(result);
      }
    }
    return geom.getFactory().buildGeometry(results);
  }

  private Object[] copyArgs(Object[] args, int replaceIndex, double val) {
    Object[] newArgs = args.clone();
    // only copy arg if there is a repeatable arg
    if (newArgs.length > replaceIndex)
      newArgs[replaceIndex] = val;
    return newArgs;
  }

}
