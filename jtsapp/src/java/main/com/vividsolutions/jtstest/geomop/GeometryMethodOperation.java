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
import com.vividsolutions.jtstest.testrunner.BooleanResult;
import com.vividsolutions.jtstest.testrunner.DoubleResult;
import com.vividsolutions.jtstest.testrunner.GeometryResult;
import com.vividsolutions.jtstest.testrunner.IntegerResult;
import com.vividsolutions.jtstest.testrunner.JTSTestReflectionException;
import com.vividsolutions.jtstest.testrunner.Result;

/**
 * Invokes a named operation on a set of arguments,
 * the first of which is a {@link Geometry}.
 * This class provides operations which are the methods 
 * defined on the Geometry class.
 * Other {@link GeometryOperation} classes can delegate to
 * instances of this class to run standard Geometry methods.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometryMethodOperation
    implements GeometryOperation
{
  public static boolean isBooleanFunction(String name) {
		return getGeometryReturnType(name) == boolean.class;
	}

	public static boolean isIntegerFunction(String name) {
		return getGeometryReturnType(name) == int.class;
	}

	public static boolean isDoubleFunction(String name) {
		return getGeometryReturnType(name) == double.class;
	}

	public static boolean isGeometryFunction(String name) {
		return Geometry.class.isAssignableFrom(getGeometryReturnType(name));
	}


  public static Class getGeometryReturnType(String functionName) {
		Method[] methods = Geometry.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(functionName)) {
				Class returnClass = methods[i].getReturnType();
				/**
				 * Filter out only acceptable classes. (For instance, don't accept the
				 * relate()=>IntersectionMatrix method)
				 */
				if (returnClass == boolean.class
						|| Geometry.class.isAssignableFrom(returnClass)
						|| returnClass == double.class || returnClass == int.class) {
					return returnClass;
				}
			}
		}
		return null;
	}

  private Method[] geometryMethods = Geometry.class.getMethods();

  public GeometryMethodOperation() {
  }

  public Class getReturnType(String opName)
  {
  	return getGeometryReturnType(opName);
  }
  
  public Result invoke(String opName, Geometry geometry, Object[] args)
      throws Exception
  {
    Object[] actualArgs = new Object[args.length];
    Method geomMethod = getGeometryMethod(opName, args, actualArgs);
    if (geomMethod == null)
      throw new JTSTestReflectionException(opName, args);
    return invokeMethod(geomMethod, geometry, actualArgs);
  }

  private Method getGeometryMethod(String opName, Object[] args, Object[] actualArgs)
  {
    // could index methods by name for efficiency...
    for (int i = 0; i < geometryMethods.length; i++) {
      if (! geometryMethods[i].getName().equalsIgnoreCase(opName)) {
        continue;
      }
      if (convertArgs(geometryMethods[i].getParameterTypes(), args, actualArgs)) {
        return geometryMethods[i];
      }
    }
    return null;
  }

  private static int nonNullItemCount(Object[] obj)
  {
    int count = 0;
    for (int i = 0; i < obj.length; i++) {
      if (obj[i] != null)
        count++;
    }
    return count;
  }

  private Object[] convArg = new Object[1];

  private boolean convertArgs(Class[] parameterTypes, Object[] args, Object[] actualArgs)
  {
    if (parameterTypes.length != nonNullItemCount(args))
      return false;

    for (int i = 0; i < args.length; i++ ) {
      boolean isCompatible = convertArg(parameterTypes[i], args[i], convArg);
      if (! isCompatible)
        return false;
      actualArgs[i] = convArg[0];
    }
    return true;
  }

  private boolean convertArg(Class destClass, Object srcValue, Object[] convArg)
  {
    convArg[0] = null;
    if (srcValue instanceof String) {
      return convertArgFromString(destClass, (String) srcValue, convArg);
    }
    if (destClass.isAssignableFrom(srcValue.getClass())) {
      convArg[0] = srcValue;
      return true;
    }
    return false;
  }

  private boolean convertArgFromString(Class destClass, String srcStr, Object[] convArg)
  {
    convArg[0] = null;
    if (destClass == Boolean.class || destClass == boolean.class) {
      if (srcStr.equals("true")) {
        convArg[0] = new Boolean(true);
        return true;
      }
      else if (srcStr.equals("false")) {
        convArg[0] =  new Boolean(false);
        return true;
      }
      return false;
    }
    if (destClass == Integer.class || destClass == int.class) {
      // try as an int
      try {
        convArg[0] = new Integer(srcStr);
        return true;
      }
      catch (NumberFormatException e) {
        // eat this exception
      }
      return false;
    }

    if (destClass == Double.class || destClass == double.class) {
      // try as an int
      try {
        convArg[0] = new Double(srcStr);
        return true;
      }
      catch (NumberFormatException e) {
        // eat this exception
      }
      return false;
    }
    if (destClass == String.class) {
      convArg[0] = srcStr;
      return true;
    }
    return false;
  }


  private Result invokeMethod(Method method, Geometry geometry, Object[] args)
      throws Exception
  {
    try {
      if (method.getReturnType() == boolean.class) {
        return new BooleanResult((Boolean) method.invoke(geometry, args));
      }
      if (Geometry.class.isAssignableFrom(method.getReturnType())) {
        return new GeometryResult((Geometry) method.invoke(geometry, args));
      }
      if (method.getReturnType() == double.class) {
        return new DoubleResult((Double) method.invoke(geometry, args));
      }
      if (method.getReturnType() == int.class) {
        return new IntegerResult((Integer) method.invoke(geometry, args));
      }
    }
    catch (InvocationTargetException e) {
    	Throwable t = e.getTargetException();
    	if (t instanceof Exception)
    		throw (Exception) t;
    	throw (Error) t;
    }
    throw new JTSTestReflectionException("Unsupported result type: " + method.getReturnType());
  }

}