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
