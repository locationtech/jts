/*
 * Copyright (c) 2021 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.io;

public class HtmlUtil {

  public static String styleClass(String name, String style) {
    return String.format("%s {\n  %s\n}\n", name, style);
  }

  public static String elem(String name, String... elems) {
    StringBuilder sb = new StringBuilder();
    sb.append("<" + name + ">\n");
    for (String elem : elems) {
      sb.append(elem);
      sb.append("\n");
    }
    sb.append("</" + name + ">\n");
    return sb.toString();
  }

  public static String elemAttr(String name, String attr, String... elems) {
    StringBuilder sb = new StringBuilder();
    sb.append("<" + name + " " + attr + ">\n");
    for (String elem : elems) {
      sb.append(elem);
      sb.append("\n");
    }
    sb.append("</" + name + ">\n");
    return sb.toString();
  }

}
