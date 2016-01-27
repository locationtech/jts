/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.function;

import java.util.*;

public class DoubleKeyMap 
{
	private Map topMap = new TreeMap();
	
	public void put(Object key1, Object key2, Object value)
	{
		Map keyMap = (Map) topMap.get(key1);
		if (keyMap == null)
			keyMap = createKeyMap(key1);
		keyMap.put(key2, value);
	}
	
	private Map createKeyMap(Object key1)
	{
		Map map = new TreeMap();
		topMap.put(key1, map);
		return map;
	}
	
	public Object get(Object key1, Object key2)
	{
		Map keyMap = (Map) topMap.get(key1);
		if (keyMap == null) return null;
		return keyMap.get(key2);
	}
	
	public Set keySet()
	{
		return topMap.keySet();
	}
	public Set keySet(Object key)
	{
		Map keyMap = (Map) topMap.get(key);
		if (keyMap == null) return new TreeSet();
		return keyMap.keySet();
	}
	
	public Collection values(Object key1)
	{
		Map keyMap = (Map) topMap.get(key1);
		if (keyMap == null) return new ArrayList();
		return keyMap.values();
	}
}
