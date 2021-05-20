/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.util;

import java.util.Arrays;

/**
 * An extendable array of primitive <code>int</code> values.
 * 
 * @author Martin Davis
 *
 */
public class IntArrayList {
  private int[] data;
  private int size = 0;

  /**
   * Constructs an empty list.
   */
  public IntArrayList() {
    this(10);
  }

  /**
   * Constructs an empty list with the specified initial capacity
   * 
   * @param initialCapacity the initial capacity of the list
   */
  public IntArrayList(int initialCapacity) {
    data = new int[initialCapacity];
  }

  /**
   * Returns the number of values in this list.
   * 
   * @return the number of values in the list
   */
  public int size() {
    return size;
  }

  /**
   * Increases the capacity of this list instance, if necessary, 
   * to ensure that it can hold at least the number of elements 
   * specified by the capacity argument.
   * 
   * @param capacity the desired capacity
   */
  public void ensureCapacity(final int capacity) {
    if (capacity <= data.length) return;
    int newLength  = Math.max(capacity, data.length * 2);
    //System.out.println("IntArrayList: copying " + size + " ints to new array of length " + capacity);
    data = Arrays.copyOf(data, newLength);
  }
  /**
   * Adds a value to the end of this list.
   * 
   * @param value the value to add
   */
  public void add(final int value) {
    ensureCapacity(size + 1);
    data[size] = value;
    ++size;
  }
  
  /**
   * Adds all values in an array to the end of this list.
   * 
   * @param values an array of values
   */
  public void addAll(final int[] values) {
    if (values == null) return;
    if (values.length == 0) return;
    ensureCapacity(size + values.length);
    System.arraycopy(values, 0, data, size, values.length);
    size += values.length;
   }
  
  /**
   * Returns a int array containing a copy of
   * the values in this list.
   * 
   * @return an array containing the values in this list
   */
  public int[] toArray() {
    int[] array = new int[size];
    System.arraycopy(data, 0, array, 0, size);
    return array;
  }
}
