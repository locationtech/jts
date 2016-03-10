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
