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
package org.locationtech.jtstest.testbuilder;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jtstest.cmd.CommandOptions;
import org.locationtech.jtstest.command.CommandLine;
import org.locationtech.jtstest.command.Option;
import org.locationtech.jtstest.command.OptionSpec;
import org.locationtech.jtstest.command.ParseException;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;


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

  private static final JTSTestBuilderController CONTROLLER = new JTSTestBuilderController();
  
  public static JTSTestBuilder instance()
  {
  	return app;
  }
  public static JTSTestBuilderController controller() {
    return CONTROLLER;
  }
  public static JTSTestBuilderFrame frame() {
    return JTSTestBuilderFrame.instance();
  }
  
  public static TestBuilderModel model() { return instance().tbModel; }

  private static GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private static CommandLine commandLine = createCmdLine();
  public static JTSTestBuilder app;

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
    /**
     * Allow this to work even if TestBuilder is not initialized
     */
    if (instance() == null) 
      return new GeometryFactory();
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

  private static CommandLine createCmdLine() {
    commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE));
    return commandLine;
  }
  
  private static void readArgs(String[] args) throws ParseException,
			ClassNotFoundException {
		commandLine.parse(args);

		if (commandLine.hasOption(CommandOptions.GEOMFUNC)) {
			Option opt = commandLine.getOption(CommandOptions.GEOMFUNC);
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
