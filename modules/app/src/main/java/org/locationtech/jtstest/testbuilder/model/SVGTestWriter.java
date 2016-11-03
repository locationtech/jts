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
package org.locationtech.jtstest.testbuilder.model;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.util.StringUtil;
import org.locationtech.jtstest.util.io.SVGWriter;



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
