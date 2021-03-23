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

public class SelecterGeometryFunction implements GeometryFunction {
  private GeometryFunction fun;
  private double selectVal;
  
  public SelecterGeometryFunction(GeometryFunction fun, double selectVal) {
    this.fun = fun;
    this.selectVal = selectVal;
  }
  
  public String getCategory() {
    return fun.getCategory();
  }

  public String getName() {
    return fun.getName() + "?";
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
    return Geometry.class;
  }

  public String getSignature() {
    return fun.getSignature();
  }

  public boolean isBinary() {
    return fun.isBinary();
  }
  
  public boolean isRequiredB() {
    return fun.isRequiredB();
  }

  @Override
  public Object invoke(Geometry geom, Object[] args) {
    Object result = fun.invoke(geom, args);
    double val = 0;
    if (result instanceof Boolean) {
      val = ((Boolean) result) ? 1 : 0;
    }
    if (selectVal == val) return geom;
    return null;
  }


}
