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

package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil 
{
	public static String IMAGE_FORMAT_NAME_PNG = "png";
	
  public static void saveImageToClipboard(Component comp, String formatName)
  throws IOException
  {
    Image image = new BufferedImage(
    		comp.getSize().width, 
    		comp.getSize().height, 
        BufferedImage.TYPE_4BYTE_ABGR);
    comp.paint(image.getGraphics());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write((RenderedImage) image, formatName, bos); 
    
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    ClipImage ci = new ClipImage(bos.toByteArray());
    clipboard.setContents(ci, null);
  }
  
  
  public static void writeImage(Component comp, String filename, String formatName)
  throws IOException
  {
    Image image = new BufferedImage(
    		comp.getSize().width, 
    		comp.getSize().height, 
        BufferedImage.TYPE_4BYTE_ABGR);
    comp.paint(image.getGraphics());
    
    ImageIO.write((RenderedImage) image, formatName, 
        new File(filename));
  }


}
