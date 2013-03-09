/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.geomop;

import java.lang.reflect.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testrunner.*;
import com.vividsolutions.jtstest.function.*;

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