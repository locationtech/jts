/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
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
