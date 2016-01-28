/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jtstest.testbuilder.controller;

import org.locationtech.jtstest.function.GeometryFunction;
import org.locationtech.jtstest.testbuilder.model.FunctionParameters;

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
