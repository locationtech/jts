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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.util.*;

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
		BaseGeometryFunction func = (BaseGeometryFunction) o;
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
