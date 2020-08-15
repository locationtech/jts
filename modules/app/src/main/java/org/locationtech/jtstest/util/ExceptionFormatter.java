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

package org.locationtech.jtstest.util;

public class ExceptionFormatter {

  public static String getFullString(Throwable ex)
  {
    return ex.getClass().getName() + " : " + ex.toString();
  }

  public static String condense(String str) {
    final int N_START = 10;
    final int N_END = 10;
    int len = str.length();
    if (len <= N_START + N_END + 10) return str;
    return str.substring(0, N_START)
        + "..."
        + str.substring(len - N_START, len);
  }
}
