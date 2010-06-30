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
package com.vividsolutions.jtstest.function;

import java.lang.reflect.*;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.util.*;

/**
 * A {@link GeometryFunction} which calls a static
 * {@link Method}.
 * 
 * @author Martin Davis
 *
 */
public class StaticMethodGeometryFunction
	extends BaseGeometryFunction
{
	private static final String FUNCTIONS_SUFFIX = "Functions";
	private static final String PARAMETERS_SUFFIX = "Parameters";
	private static final String DESCRIPTION_SUFFIX = "Description";
	
	public static StaticMethodGeometryFunction createFunction(Method method)
	{
		Assert.isTrue(Geometry.class.isAssignableFrom((method.getParameterTypes())[0]));
		
		Class clz = method.getDeclaringClass();
		
		String category = extractCategory(ClassUtil.getClassname(clz));
		String funcName = method.getName();
		String description = extractDescription(method);
		String[] paramNames = extractParamNames(method);
		Class[] paramTypes = extractParamTypes(method);
		Class returnType = method.getReturnType();
		
		return new StaticMethodGeometryFunction(category, funcName, 
				description,
				paramNames, paramTypes,
				returnType, method);    
	}
	
	private static String extractCategory(String className)
	{
		String trim = StringUtil.removeFromEnd(className, FUNCTIONS_SUFFIX);
		return trim;
	}
	
	/**
	 * Java doesn't permit accessing the original code parameter names, unfortunately.
	 * 
	 * @param method
	 * @return
	 */
	private static String[] extractParamNames(Method method)
	{
		// try to get names from predefined ones first
		String paramsName = method.getName() + PARAMETERS_SUFFIX;
		String[] codeName = ClassUtil.getStringArrayClassField(method.getDeclaringClass(), paramsName);
		if (codeName != null) return codeName;
		
		// Synthesize default names
		String[] name = new String[method.getParameterTypes().length - 1];
		// Skip first parameter - it is the target geometry
		for (int i = 1; i < name.length; i++)
			name[i] = "arg" + i;
		return name;
	}
	
	private static String extractDescription(Method method)
	{
		// try to get names from predefined ones first
		String paramsName = method.getName() + DESCRIPTION_SUFFIX;
		return ClassUtil.getStringClassField(method.getDeclaringClass(), paramsName);
	}
	
	private static Class[] extractParamTypes(Method method)
	{
		Class[] methodParamTypes = method.getParameterTypes();
		Class[] types = new Class[methodParamTypes.length - 1];
		for (int i = 1; i < methodParamTypes.length; i++)
			types[i-1] = methodParamTypes[i];
		return types;
	}

  private Method method;

	public StaticMethodGeometryFunction(
			String category,
			String name, 
			String description,
			String[] parameterNames, 
			Class[] parameterTypes, 
			Class returnType,
			Method method)
	{
		super(category, name, description, parameterNames, parameterTypes, returnType);
    this.method = method;
	}
	
  public Object invoke(Geometry g, Object[] arg) 
  {
    return invoke(method, null, createFullArgs(g, arg));
  }

  /**
   * Creates an arg array which includes the target geometry as the first argument
   * 
   * @param g
   * @param arg
   * @return
   */
  private static Object[] createFullArgs(Geometry g, Object[] arg)
  {
  	int fullArgLen = 1;
  	if (arg != null) 
  		fullArgLen = arg.length + 1;
  	Object[] fullArg = new Object[fullArgLen];
  	fullArg[0] = g;
  	for (int i = 1; i < fullArgLen; i++) {
  		fullArg[i] = arg[i-1];
  	}
  	return fullArg;
  }
  
  public static Object invoke(Method method, Object target, Object[] args)
  {
    Object result;
    try {
      result = method.invoke(target, args);
    }
    catch (InvocationTargetException ex) {
      Throwable t = ex.getCause();
      if (t instanceof RuntimeException)
      	throw (RuntimeException) t;
      throw new RuntimeException(invocationErrMsg(ex));
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
      throw new RuntimeException(ex.getMessage());
    }
    return result;
  }
  
  private static String invocationErrMsg(InvocationTargetException ex)
  {
    Throwable targetEx = ex.getTargetException();
    String msg = getClassname(targetEx.getClass())
    + ": " +
    targetEx.getMessage();
    return msg;
  }
  
  public static String getClassname(Class javaClass)
  {
    String jClassName = javaClass.getName();
    int lastDotPos = jClassName.lastIndexOf(".");
    return jClassName.substring(lastDotPos + 1, jClassName.length());
  }
}
