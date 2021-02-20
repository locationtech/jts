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
      writeDocStart(sb);
      for (int i = 0; i < tcList.size(); i++) {
        Testable tc = tcList.get(i);
        writeTest(i, tc, sb);
      }
      writeDocEnd(sb);
      return sb.toString();
    }

    private void writeTest(int i, Testable tc, StringBuilder sb) {
      writeTitle(i, tc, sb);
      sb.append("<table><tr>");
      sb.append("<td>");
      sb.append(writeSvg(tc));
      sb.append("</td>");
      sb.append("<td>");
      sb.append(tc.getGeometry(0));
      sb.append("</td>");
      sb.append("</tr></table>\n");
      sb.append("<hr />\n");
   }

    private void writeTitle(int i, Testable tc, StringBuilder sb) {
      String desc = tc.getDescription();
      String title = desc == null ? "" : ": " + desc;
      sb.append(String.format(
          "\n<h2>Case %d%s</h2>\n", i, title));
    }
    
    private void writeDocStart(StringBuilder sb) {
      sb.append("<html>");
      sb.append("<body>");
    }

    private void writeDocEnd(StringBuilder sb) {
      sb.append("</body>");    
      sb.append("</html>");
    }

    private String writeSvg(Testable testable) {
        Geometry ga = testable.getGeometry(0);
        Geometry gb = testable.getGeometry(1);
        if (ga == null && gb == null) return "";
        return write(ga, gb, testable.getName(), testable.getDescription() );
    }
    
    public String write(Geometry ga, Geometry gb, String name, String description) {
        StringBuilder text = new StringBuilder();
        
        Envelope env = new Envelope();
        if (ga != null) env.expandToInclude(ga.getEnvelopeInternal());
        if (gb != null) env.expandToInclude(gb.getEnvelopeInternal());
        double envDiam = env.getDiameter();
        env.expandBy(envDiam * 0.02);
        Coordinate centre = env.centre();
        
        String wh = "width='" + viewSize + "' height='" + viewSize + "'";
        String viewBox = env.getMinX() + " " + env.getMinY() + " " + env.getWidth() + " " + env.getHeight();
        // transform to flip the Y axis to match SVG
        String trans = String.format("translate(0 %f) scale( 1 -1 ) translate(0 %f)", centre.y, -centre.y);
        
        text.append("<svg " + wh + " viewBox='" + viewBox + "'  version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>\n");
        String nameStr =name == null ? "" : name;
        String descStr = description == null ? "" : description;
        //text.append("          \"" + name + "\",\n");
        text.append("  <desc>" + descStr + "</desc>\n");
        text.append("  <g transform='" + trans + "'>\n\n");

        writeGeometryElement(ga, "#bbbbff", "#0000ff", text);
        writeGeometryElement(gb, "#ffbbbb", "#ff0000", text);

        text.append("  </g>\n");
        text.append("</svg>\n");
        return text.toString();
    }

    private void writeGeometryElement(Geometry g, String fillClr, String strokeClr, StringBuilder text) {
      if (g == null) return;
      writeGeometryStyled(g, fillClr, strokeClr, text);
      text.append("\n");
    }

    private void writeGeometryStyled(Geometry g, String fillClr, String strokeClr, StringBuilder text ) {
      String gstyle = "<g style='fill:" + fillClr + "; fill-opacity:0.5; stroke:" + strokeClr + "; stroke-width:1; stroke-opacity:1; stroke-miterlimit:4; stroke-linejoin:miter; stroke-linecap:square' >\n";
      text.append(gstyle);
      text.append(write(g));
      text.append("\n</g>\n");
    }
    
    private String write(Geometry geometry) {
        if (geometry == null) {
            return "";
        }
        return svgWriter.write(geometry);
    }


}
