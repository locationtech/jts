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
