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

package org.locationtech.jts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for processing {@link Collection}s.
 *
 * @version 1.7
 */
public class CollectionUtil 
{

  public interface Function {
    Object execute(Object obj);
  }

  /**
   * Executes a function on each item in a {@link Collection}
   * and returns the results in a new {@link List}
   *
   * @param coll the collection to process
   * @param func the Function to execute
   * @return a list of the transformed objects
   */
  public static List transform(Collection coll, Function func)
  {
    List result = new ArrayList();
    for (Iterator i = coll.iterator(); i.hasNext(); ) {
      result.add(func.execute(i.next()));
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
  public static void apply(Collection coll, Function func)
  {
    for (Iterator i = coll.iterator(); i.hasNext(); ) {
      func.execute(i.next());
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
  public static List select(Collection collection, Function func) {
    List result = new ArrayList();
    for (Iterator i = collection.iterator(); i.hasNext();) {
      Object item = i.next();
      if (Boolean.TRUE.equals(func.execute(item))) {
        result.add(item);
      }
    }
    return result;
  }
}
