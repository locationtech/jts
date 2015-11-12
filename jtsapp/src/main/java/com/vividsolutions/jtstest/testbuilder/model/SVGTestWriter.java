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
package com.vividsolutions.jtstest.testbuilder.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jtstest.test.TestCaseList;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.util.StringUtil;
import com.vividsolutions.jtstest.util.io.SVGWriter;


/**
 * @version 1.7
 */
public class SVGTestWriter {

  public static String getTestSVG(TestCaseList tcList) {
    StringBuffer java = new StringBuffer();
    for (int i = 0; i < tcList.getList().size(); i++) {
      java.append((new SVGTestWriter()).write((Testable) tcList.getList().get(i)));
    }
    return java.toString();
  }

  public static String getTestSVG(Testable test) {
    SVGTestWriter writer = new SVGTestWriter();
    return writer.write(test);
  }


    private SVGWriter writer = new SVGWriter();

    public SVGTestWriter() {}

    public String write(Testable testable) {
        StringBuffer text = new StringBuffer();
        
        Geometry ga = testable.getGeometry(0);
        Geometry gb = testable.getGeometry(1);
        
        Envelope env = new Envelope();
        if (ga != null) env.expandToInclude(ga.getEnvelopeInternal());
        if (gb != null) env.expandToInclude(gb.getEnvelopeInternal());
        
        String viewBox = env.getMinX() + " " + env.getMinY() + " " + env.getMaxX() + " " + env.getMaxY();
        
        text.append("<?xml version='1.0' standalone='no'?>\n");
        text.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>\n");
        text.append("<svg width='400' height='400' viewBox='" + viewBox + "'  version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>\n");
        String name = testable.getName() == null ? "" : testable.getName();
        String description = testable.getDescription() == null ? "" : testable.getDescription();
        //text.append("          \"" + name + "\",\n");
        text.append("  <desc>" + description + "</desc>\n");
        
        String a = writeGeometryStyled(ga, "#bbbbff", "#0000ff");
        String b = writeGeometryStyled(gb, "#ffbbbb", "#ff0000");
        text.append(a + "\n");
        text.append("\n");
        text.append(b + "\n");
        text.append("</svg>\n");
        return text.toString();
    }

    private String writeGeometryStyled(Geometry g, String fillClr, String strokeClr ) {
      String s = "<g fill='" + fillClr + "' stroke='" + strokeClr + "' >\n";
      s += write(g);
      s += "</g>";
      return s;
    }
    private String write(Geometry geometry) {
        if (geometry == null) {
            return "";
        }
        return writer.write(geometry);
    }
}
