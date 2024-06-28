/*
 * Copyright (c) 2021 Martin Davis
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

import org.locationtech.jts.geom.Geometry;

public class FilterGeometryFunction implements GeometryFunction {
  
  public static int OP_EQ = 1;
  public static int OP_NE = 2;
  public static int OP_GE = 3;
  public static int OP_GT = 4;
  public static int OP_LE = 5;
  public static int OP_LT = 6;
  
  private GeometryFunction fun;
  private double filterVal;
  private int filterOp;
  
  public FilterGeometryFunction(GeometryFunction fun, double filterVal) {
    this.fun = fun;
    this.filterVal = filterVal;
    filterOp = OP_EQ;
  }
  
  public FilterGeometryFunction(GeometryFunction fun, int op, double val) {
    this.fun = fun;
    this.filterOp = op;
    this.filterVal = val;
  }
  
  @Override
  public String getCategory() {
    return fun.getCategory();
  }

  @Override
  public String getName() {
    return fun.getName() + "?";
  }

  @Override
  public String getDescription() {
    return fun.getDescription();
  }

  @Override
  public String[] getParameterNames() {
    return fun.getParameterNames();
  }

  @Override
  public Class[] getParameterTypes() {
    return fun.getParameterTypes();
  }

  @Override
  public Class getReturnType() {
    return Geometry.class;
  }

  @Override
  public String getSignature() {
    return fun.getSignature();
  }

  @Override
  public boolean isBinary() {
    return fun.isBinary();
  }
  
  @Override
  public boolean isRequiredB() {
    return fun.isRequiredB();
  }

  @Override
  public Object invoke(Geometry geom, Object[] args) {
    Object result = fun.invoke(geom, args);
    double val = toDouble(result);
    if (isMatch(val)) return geom;
    return null;
  }

  private double toDouble(Object result) {
    if (result instanceof Boolean) return ((Boolean) result) ? 1 : 0;
    if (result instanceof Number) 
      return ((Number) result).doubleValue();
    return 0;
  }

  private boolean isMatch(double val) {
    if (OP_EQ == filterOp) return val == filterVal;
    if (OP_NE == filterOp) return val != filterVal;
    if (OP_GE == filterOp) return val >= filterVal;
    if (OP_GT == filterOp) return val > filterVal;
    if (OP_LE == filterOp) return val <= filterVal;
    if (OP_LT == filterOp) return val < filterVal;
    return false;
  }


}
