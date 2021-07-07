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
package org.locationtech.jtstest.testbuilder.io;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.util.io.SVGWriter;

/**
 * @version 1.7
 */
public class HtmlSvgTestWriter {

  public static String writeTestSVG(TestCaseList testCaseList) {
    HtmlSvgTestWriter writer = new HtmlSvgTestWriter();
    return writer.write(testCaseList);
  }

  private SVGWriter svgWriter = new SVGWriter();
  private int viewSize = 200;
  
  public HtmlSvgTestWriter() {}

  public String write(TestCaseList tcList) {
    StringBuilder sb = new StringBuilder();
    appendln(sb, "<html>");
    writeStyles(sb);
    appendln(sb, "<body>");
    sb.append(defsMarkers());
    for (int i = 0; i < tcList.size(); i++) {
      Testable tc = tcList.get(i);
      writeTest(i + 1, tc, sb);
    }
    appendln(sb, "</body></html>");    
    return sb.toString();
  }

  private static void writeStyles(StringBuilder sb) {
    String styleA = HtmlUtil.styleClass(".geomA", style("vertexA", "#bbbbff", "#0000ff"));
        
    String styleB = HtmlUtil.styleClass(".geomB", style("vertexB", "#ffbbbb", "#ff0000"));
    
    sb.append(HtmlUtil.elem("head",
        HtmlUtil.elem("style", styleA, styleB)));
  }
  
  private static String style(String vertexMarker, String clrFill, String clrStroke) {
    return  String.format(
        "marker-end: url(#%s); marker-mid: url(#%s); fill:%s; fill-opacity:0.5; stroke:%s; stroke-width:1; stroke-opacity:1; stroke-miterlimit:4; stroke-linejoin:miter; stroke-linecap:square;"
        , vertexMarker, vertexMarker, clrFill, clrStroke);
  }
  private static void appendln(StringBuilder sb, String s) {
    sb.append(s);
    sb.append("\n");
  }

  private static String defsMarkers() {
    return "<svg xmlns='http://www.w3.org/2000/svg'>"
        + "\n<defs>"
        + "\n<marker id='vertexA' markerWidth='3' markerHeight='3' refX='1.5' refY='1.5'>"
        +   "<circle cx='1.5' cy='1.5' r='1.5' stroke='none' fill='#0000af'/>"
        + "\n<marker id='vertexB' markerWidth='3' markerHeight='3' refX='1.5' refY='1.5'>"
        +   "<circle cx='1.5' cy='1.5' r='1.5' stroke='none' fill='#af0000'/>"
        + "\n</marker>"
        + "\n</defs>"
        + "\n</svg>";
  }
  
  private void writeTest(int i, Testable tc, StringBuilder sb) {
    writeTitle(i, tc, sb);
    sb.append("<table><tr>");
    sb.append("<td>");
    sb.append(writeSvg(tc));
    sb.append("</td>");
    sb.append("<td>");
    writeGeomText(sb , tc.getGeometry(0), "#0000a0");
    appendln(sb,"<p>");
    writeGeomText(sb , tc.getGeometry(1), "#a00000");
    sb.append("</td>");
    sb.append("</tr></table>\n");
    sb.append("<hr />\n");
 }

  private void writeGeomText(StringBuilder sb, Geometry geom, String clr) {
    if (geom == null) return;
    String attr = String.format("style='color:%s'", clr);
    sb.append( HtmlUtil.elemAttr("div", attr, HtmlUtil.elem("code", geom.toString())));
  }

  private void writeTitle(int i, Testable tc, StringBuilder sb) {
    String desc = tc.getDescription();
    String title = desc == null ? "" : ": " + desc;
    sb.append(String.format(
        "\n<h2>Case %d%s</h2>\n", i, title));
  }

  private String writeSvg(Testable testable) {
      Geometry ga = testable.getGeometry(0);
      Geometry gb = testable.getGeometry(1);
      if (ga == null && gb == null) return "";
      return write(ga, gb, testable.getName(), testable.getDescription() );
  }
  
  public String write(Geometry ga, Geometry gb, String name, String description) {
      StringBuilder sb = new StringBuilder();
      
      Envelope env = sceneEnv(ga, gb);
      Coordinate centre = env.centre();
      
      String wh = "width='" + viewSize + "' height='" + viewSize + "'";
      String viewBox = env.getMinX() + " " + env.getMinY() + " " + env.getWidth() + " " + env.getHeight();
      // transform to flip the Y axis to match SVG
      String trans = String.format("translate(0 %f) scale( 1 -1 ) translate(0 %f)", centre.y, -centre.y);
      
      sb.append("<svg " + wh + " viewBox='" + viewBox + "'  version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>\n");
      String nameStr =name == null ? "" : name;
      String descStr = description == null ? "" : description;
      sb.append("  <desc>" + descStr + "</desc>\n");
      sb.append("  <g transform='" + trans + "'>\n\n");

      writeGeometryWithClass(sb, ga, "geomA");
      writeGeometryWithClass(sb, gb, "geomB");

      sb.append("  </g>\n");
      sb.append("</svg>\n");
      return sb.toString();
  }

  private static Envelope sceneEnv(Geometry ga, Geometry gb) {
    Envelope env = new Envelope();
    if (ga != null) env.expandToInclude(GeometryUtil.totalEnvelope(ga));
    if (gb != null) env.expandToInclude(GeometryUtil.totalEnvelope(gb));
    double envDiam = env.getDiameter();
    env.expandBy(envDiam * 0.02);
    return env;
  }
  
  private void writeGeometryWithClass(StringBuilder sb, Geometry g, String className ) {
    if (g == null) return;
    String gstyle = String.format("<g class='%s' >", className);
    appendln(sb, gstyle);
    appendln(sb, svgWriter.write(g));
    appendln(sb, "</g>");
  }

}
