

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.Image;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Assert;

import com.vividsolutions.jtstest.test.TestCaseList;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testbuilder.BusyDialog;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;
import com.vividsolutions.jtstest.testrunner.BooleanResult;
import com.vividsolutions.jtstest.testrunner.Test;
import com.vividsolutions.jtstest.util.FileUtil;
import com.vividsolutions.jtstest.util.StringUtil;

/**
 *  Creates an .PNG file for a test case.
 *
 * @version 1.7
 */
public class PNGWriter {
  private final static int IMAGE_WIDTH = 200;
  private final static int IMAGE_HEIGHT = 200;
  private final static int STACK_TRACE_DEPTH = 1;

  private GeometryEditPanel geometryEditPanel = new GeometryEditPanel();
  private JFrame frame = new JFrame();
  private File outputDirectory;

  public PNGWriter() {
    geometryEditPanel.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    geometryEditPanel.setGridEnabled(false);
    geometryEditPanel.setBorder(BorderFactory.createEmptyBorder());
    frame.getContentPane().add(geometryEditPanel);
  }

  public void write(File outputDirectory, TestCaseEdit testCase, PrecisionModel precisionModel) throws IOException {
    Assert.isTrue(outputDirectory.isDirectory());
    this.outputDirectory = outputDirectory;
    createPNGFile("geoms", testCase.getGeometry(0),
        testCase.getGeometry(1), testCase.getResult(),
        IMAGE_WIDTH, IMAGE_HEIGHT);
  }

  private void createPNGFile(String filenameNoPath, Geometry a,
      Geometry b,
      Geometry result,
      int imageWidth, int imageHeight) throws FileNotFoundException,
      IOException {
    TestBuilderModel tbModel = new TestBuilderModel();
    TestCaseEdit tc = new TestCaseEdit(new Geometry[]{ a, b });
    tc.setResult(result);
    tbModel.getGeometryEditModel().setTestCase(tc);
    geometryEditPanel.setModel(tbModel);
    geometryEditPanel.zoomToFullExtent();
    geometryEditPanel.setShowingResult(result != null);
    geometryEditPanel.setShowingGeometryA(a != null);
    geometryEditPanel.setShowingGeometryB(b != null);
    String filenameWithPath = outputDirectory.getPath() + "\\" + filenameNoPath;
    Image image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
    geometryEditPanel.paint(image.getGraphics());
    
    ImageIO.write((RenderedImage) image, "png", 
        new File(filenameWithPath + ".png"));
  }


}

