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
package org.locationtech.jtstest.geomop;

import java.lang.reflect.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.function.*;
import org.locationtech.jtstest.testrunner.*;


/**
 * Invokes a function from registry 
 * or a Geometry method determined by a named operation with a list of arguments,
 * the first of which is a {@link Geometry}.
 * This class allows overriding Geometry methods
 * or augmenting them
 * with functions defined in a {@link GeometryFunctionRegistry}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometryFunctionOperation
    implements GeometryOperation
{

  private GeometryFunctionRegistry registry = null;
  private GeometryOperation defaultOp = new GeometryMethodOperation();
  private ArgumentConverter argConverter = new ArgumentConverter();

  public GeometryFunctionOperation() {
  }

  public GeometryFunctionOperation(GeometryFunctionRegistry registry) {
  	this.registry = registry;
  }

  public Class getReturnType(String opName)
  {
  	GeometryFunction func = registry.find(opName);
  	if (func == null)
  		return defaultOp.getReturnType(opName);
  	return func.getReturnType();
  }
  
  public Result invoke(String opName, Geometry geometry, Object[] args)
      throws Exception
  {
  	GeometryFunction func = registry.find(opName, args.length);
  	if (func == null)
      return defaultOp.invoke(opName, geometry, args);
  	
    return invoke(func, geometry, args);
  }

  private Result invoke(GeometryFunction func, Geometry geometry, Object[] args)
      throws Exception {
    Object[] actualArgs = argConverter.convert(func.getParameterTypes(), args);

    if (func.getReturnType() == boolean.class) {
      return new BooleanResult((Boolean) func.invoke(geometry, actualArgs));
    }
    if (Geometry.class.isAssignableFrom(func.getReturnType())) {
      return new GeometryResult((Geometry) func.invoke(geometry, actualArgs));
    }
    if (func.getReturnType() == double.class) {
      return new DoubleResult((Double) func.invoke(geometry, actualArgs));
    }
    if (func.getReturnType() == int.class) {
      return new IntegerResult((Integer) func.invoke(geometry, actualArgs));
    }
    throw new JTSTestReflectionException("Unsupported result type: "
        + func.getReturnType());
  }


}