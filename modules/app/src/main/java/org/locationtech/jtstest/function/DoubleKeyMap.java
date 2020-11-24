/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import java.util.*;

public class DoubleKeyMap <K extends Comparable<K>,L extends Comparable<L>,V>
{
	private final Map<K,Map<L,V>> topMap = new TreeMap<>();
	
	public void put(K key1, L key2, V value)
	{
		Map<L,V> keyMap =  topMap.get(key1);
		if (keyMap == null)
			keyMap = createKeyMap(key1);
		keyMap.put(key2, value);
	}
	
	private Map<L,V> createKeyMap(K key1)
	{
		Map<L,V> map = new TreeMap<>();
		topMap.put(key1, map);
		return map;
	}
	
	public V get(K key1, L key2)
	{
		Map<L,V> keyMap =  topMap.get(key1);
		if (keyMap == null) return null;
		return keyMap.get(key2);
	}
	
	public Set<K> keySet()
	{
		return topMap.keySet();
	}
	public Set<L> keySet(K key)
	{
		Map<L,V> keyMap =  topMap.get(key);
		if (keyMap == null) return Collections.emptySet();
		return keyMap.keySet();
	}
	
	public Collection<V> values(K key1)
	{
		Map<L,V> keyMap =  topMap.get(key1);
		if (keyMap == null) return Collections.emptyList();
		return keyMap.values();
	}
}
