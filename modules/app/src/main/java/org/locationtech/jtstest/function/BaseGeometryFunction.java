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
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.util.*;


/**
 * A base for implementations of
 * {@link GeometryFunction} which provides most 
 * of the required structure.
 * Extenders must supply the behaviour for the 
 * actual function invocation.
 * 
 * @author Martin Davis
 *
 */
public abstract class BaseGeometryFunction 
implements GeometryFunction, Comparable
{
  public static boolean isBinaryGeomFunction(GeometryFunction func)
  {
    return func.getParameterTypes().length >= 1
        && func.getParameterTypes()[0] == Geometry.class;
  }
  
	protected String category  = null;
	protected String name;
	protected String description;
	protected String[] parameterNames;
	protected Class[] parameterTypes;
	protected Class returnType;
	
	public BaseGeometryFunction(
			String category,
			String name, 
			String[] parameterNames, 
			Class[] parameterTypes, 
			Class returnType)
	{
		this.category = category;
		this.name = name;
		this.parameterNames = parameterNames;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	
	public BaseGeometryFunction(
			String category,
			String name, 
			String description,
			String[] parameterNames, 
			Class[] parameterTypes, 
			Class returnType)
	{
		this.category = category;
		this.name = name;
		this.description = description;
		this.parameterNames = parameterNames;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String[] getParameterNames()
	{
		return parameterNames;
	}
	
	/**
	 * Gets the types of the other function arguments,
	 * if any.
	 * 
	 * @return the types
	 */
	public Class[] getParameterTypes()
	{
		return parameterTypes;
	}
	
	public Class getReturnType()
	{
		return returnType;
	}
	
  @Override
  public boolean isBinary() {
    return parameterTypes.length > 0 && parameterTypes[0] == Geometry.class;
  }

  @Override

	public String getSignature()
	{
		StringBuffer paramTypes = new StringBuffer();
		paramTypes.append("Geometry");
		for (int i = 0 ; i < parameterTypes.length; i++) {
			paramTypes.append(",");
			paramTypes.append(ClassUtil.getClassname(parameterTypes[i]));
		}
		return name + "(" + paramTypes + ")"
			+ " -> " 
			+ ClassUtil.getClassname(returnType);
	}
	
  protected static Double getDoubleOrNull(Object[] args, int index)
  {
  	if (args.length <= index) return null;
  	if (args[index] == null) return null;
  	return (Double) args[index];
  }
  
  protected static Integer getIntegerOrNull(Object[] args, int index)
  {
  	if (args.length <= index) return null;
  	if (args[index] == null) return null;
  	return (Integer) args[index];
  }
  
	public abstract Object invoke(Geometry geom, Object[] args);
	
	/**
	 * Two functions are the same if they have the 
	 * same signature (name, parameter types and return type).
	 * 
	 * @param obj
	 * @return true if this object is the same as the <tt>obj</tt> argument
	 */
	public boolean equals(Object obj)
	{
		if (! (obj instanceof GeometryFunction)) return false;
		GeometryFunction func = (GeometryFunction) obj;
		if (! name.equals(func.getName())) return false;
		if (! returnType.equals(func.getReturnType())) return false;
		
		Class[] funcParamTypes = func.getParameterTypes();
		if (parameterTypes.length != funcParamTypes.length) return false;
		for (int i = 0; i < parameterTypes.length; i++) {
			if (! parameterTypes[i].equals(funcParamTypes[i]))
				return false;
		}
		return true;
	}

	public int compareTo(Object o)
	{
		GeometryFunction func = (GeometryFunction) o;
		int cmp = name.compareTo(func.getName());
    if (cmp != 0)
      return cmp;
		return compareTo(returnType, func.getReturnType());
		//TODO: compare parameter lists as well
	}
	
	private static int compareTo(Class c1, Class c2)
	{
		return c1.getName().compareTo(c2.getName());
	}
}
