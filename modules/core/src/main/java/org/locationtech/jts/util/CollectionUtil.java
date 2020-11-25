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

package org.locationtech.jts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Utilities for processing {@link Collection}s.
 * @deprecated
 * @version 1.7
 */
public final class CollectionUtil
{
private CollectionUtil(){}


  /**
   * Executes a function on each item in a {@link Collection}
   * and returns the results in a new {@link List}
   *
   * @param coll the collection to process
   * @param func the Function to execute
   * @return a list of the transformed objects
   */
  public static <T,S>List<T> transform(Collection<S> coll, Function<S,T> func)
  {
    List<T> result = new ArrayList<>();
    for (S s : coll) {
      result.add(func.apply(s));
    }
    return result;
  }

  /**
   * Executes a function on each item in a Collection but does
   * not accumulate the result
   *
   * @param coll the collection to process
   * @param func the Function to execute
   */
  public static <T,S>void apply(Collection<S> coll, Function<S,T> func)
  {
    for (S s : coll) {
      func.apply(s);
    }
  }

  /**
   * Executes a {@link Function} on each item in a Collection
   * and collects all the entries for which the result
   * of the function is equal to {@link Boolean} <tt>true</tt>.
   *
   * @param collection the collection to process
   * @param func the Function to execute
   * @return a list of objects for which the function was true
   */
  public static <T>List<T> select(Collection<T> collection, Function<T,Boolean> func) {
    List<T> result = new ArrayList<>();
    for (T item : collection) {
      if (Boolean.TRUE.equals(func.apply(item))) {
        result.add(item);
      }
    }
    return result;
  }
}
