package com.vividsolutions.jtstest.testbuilder.controller;

import com.vividsolutions.jtstest.function.GeometryFunction;
import com.vividsolutions.jtstest.testbuilder.model.FunctionParameters;

public class FunctionInvocation {

  private GeometryFunction function;
  private Object[] param;

  public FunctionInvocation(GeometryFunction function, Object[] functionParams) {
    this.function = function;
    this.param = functionParams;
  }

  public String getSignature() {
    if (function == null)
      return null;
    return function.getCategory() 
        + "." + function.getName()
        + "(" + FunctionParameters.toString(param) + ")";
  }

  public GeometryFunction getFunction() {
    return function;
  }

  public Object[] getParameters() {
    return param;
  }
}
