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
package com.vividsolutions.jtstest.testbuilder;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;

import com.vividsolutions.jtstest.command.*;
import com.vividsolutions.jtstest.function.*;
import com.vividsolutions.jtstest.testbuilder.model.*;
import com.vividsolutions.jts.geom.*;

/**
 * A Swing application which supports 
 * creating geometries and running JTS operations.
 * <p>
 * <b>Command Line Options</b>
 * <table border='1'>
 * <tr>
 * <td><tt>-geomfunc <i>{ &lt;classname&gt; }</i></tt> </td>
 * <td>Specifies classes whose <tt>public static<tt> methods will be loaded as geometry functions</td>
 * </tr>
 * </table>
 * 
 * @version 1.7
 */
public class JTSTestBuilder
{
  private static final String PROP_SWING_DEFAULTLAF = "swing.defaultlaf";

  private static final String OPT_GEOMFUNC = "geomfunc";
  
  public static JTSTestBuilder instance()
  {
  	return app;
  }
  
  public static TestBuilderModel model() { return instance().tbModel; }

  private static GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private static CommandLine commandLine = createCmdLine();
  public static JTSTestBuilder app;
  
  private static CommandLine createCmdLine() {
    commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE));
    return commandLine;
  }

  public static GeometryFunctionRegistry getFunctionRegistry()
  {
    return funcRegistry;
  }

  public static PrecisionModel getPrecisionModel() 
  { 
    return model().getPrecisionModel();
  }
  
  public static GeometryFactory getGeometryFactory() 
  { 
    return model().getGeometryFactory();
  }
  
  private TestBuilderModel tbModel = new TestBuilderModel();
  
  boolean packFrame = false;

  /**Construct the application*/
  public JTSTestBuilder() {
  }
  
  private void initFrame()
  {
    JTSTestBuilderFrame frame = new JTSTestBuilderFrame();
    frame.setModel(model());
    
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    } else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation(
        (screenSize.width - frameSize.width) / 2,
        (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  
  /**Main method*/
  public static void main(String[] args)
  {
    try {
    	readArgs(args);
    	setLookAndFeel();
      app = new JTSTestBuilder();
      app.initFrame();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the look and feel, using user-defined LAF if 
   * provided as a system property.
   * 
   * e.g. Metal: -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel
   * 
   * @throws InterruptedException
   * @throws InvocationTargetException
   */
  private static void setLookAndFeel() throws InterruptedException, InvocationTargetException
  {
    /**
     * Invoke on Swing thread to pass Java security requirements
     */
    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
      public void run()
      {
        try {
          String laf = System.getProperty(PROP_SWING_DEFAULTLAF);
          if (laf == null) {
            laf = UIManager.getSystemLookAndFeelClassName();
          }
          javax.swing.UIManager.setLookAndFeel(laf);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private static void readArgs(String[] args) throws ParseException,
			ClassNotFoundException {
		commandLine.parse(args);

		if (commandLine.hasOption(OPT_GEOMFUNC)) {
			Option opt = commandLine.getOption(OPT_GEOMFUNC);
			for (int i = 0; i < opt.getNumArgs(); i++) {
				String geomFuncClassname = opt.getArg(i);
				try {
					funcRegistry.add(geomFuncClassname);
					System.out.println("Added Geometry Functions from: "
							+ geomFuncClassname);
				} catch (ClassNotFoundException ex) {
					System.out.println("Unable to load function class: "
							+ geomFuncClassname);
				}
			}
		}
	}

}