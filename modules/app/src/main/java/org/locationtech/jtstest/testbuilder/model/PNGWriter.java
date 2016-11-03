

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
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.BusyDialog;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testrunner.BooleanResult;
import org.locationtech.jtstest.testrunner.Test;
import org.locationtech.jtstest.util.FileUtil;
import org.locationtech.jtstest.util.StringUtil;


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

