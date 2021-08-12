/*
 * Copyright (c) 2018 Martin Davis
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
import org.locationtech.jts.geom.GeometryFactory;

public class SpreaderGeometryFunction implements GeometryFunction {

  private GeometryFunction fun;
  private boolean isEachA;
  private boolean isEachB;

  public SpreaderGeometryFunction(GeometryFunction fun, boolean eachA, boolean eachB) {
    this.fun = fun;
    this.isEachA = eachA;
    this.isEachB = eachB;
  }
  
  public String getCategory() {
    return fun.getCategory();
  }

  public String getName() {
    String name = fun.getName();
    if (isEachA) name += "*A";
    if (isEachB) name += "*B";
    return name;
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
  public boolean isRequiredB() {
    return fun.isRequiredB();
  }
  public Object invoke(Geometry geom, Object[] args) {
    List<Geometry> result = new ArrayList<Geometry>();
    if (isEachA) {
      invokeEachA(geom, args, result);
    }
    else {
      invokeB(geom, args, result);
    }
    return createResult(result, geom.getFactory());
  }
  
  private Object createResult(List<Geometry> result, GeometryFactory geometryFactory) {
    if (result.size() == 1) {
      return result.get(0);
    }
    Geometry[] resultGeoms = GeometryFactory.toGeometryArray(result);
    return geometryFactory.createGeometryCollection(resultGeoms);
  }

  private void invokeEachA(Geometry geom, Object[] args, List<Geometry> result) {
    int nElt = geom.getNumGeometries();
    for (int i = 0; i < nElt; i++) {
      Geometry geomN = geom.getGeometryN(i);
      invokeB(geomN, args, result);
    }
  }

  private void invokeB(Geometry geom, Object[] args, List<Geometry> result) {
    if (hasBGeom(args) && isEachB) {
      invokeEachB(geom, args, result);
      return;
    }
    invokeFun(geom, args, result);
  }

  private static boolean hasBGeom(Object[] args) {
    if (args.length <= 0) return false;
    return args[0] instanceof Geometry;
  }

  private void invokeEachB(Geometry geom, Object[] args, List<Geometry> result) {
    Object[] argsCopy = args.clone();
    Geometry geomB = (Geometry) args[0];
    int nElt = geomB.getNumGeometries();
    for (int i = 0; i < nElt; i++) {
      Geometry geomBN = geomB.getGeometryN(i);
      argsCopy[0] = geomBN;
      invokeFun(geom, argsCopy, result);
    }
  }

  private void invokeFun(Geometry geom, Object[] args, List<Geometry> result) {
    Geometry resultGeom = (Geometry) fun.invoke(geom, args);
    // don't keep null / empty geoms
    if (resultGeom == null || resultGeom.isEmpty()) return;
    //FunctionsUtil.showIndicator(resultGeom);
    result.add(resultGeom);
  }

  /*
  public Object OLDinvoke(Geometry geom, Object[] args) {
    return GeometryMapper.map(geom, new MapOp() {
      public Geometry map(Geometry g)
      {
        Geometry result = (Geometry) fun.invoke(g, args);
        if (result.isEmpty()) return null;
        return result;
      }
    });
  }
  */
  
}
