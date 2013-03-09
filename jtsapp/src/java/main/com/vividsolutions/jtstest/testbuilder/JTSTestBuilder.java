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
package com.vividsolutions.jtstest.testbuilder;

import java.awt.Dimension;
import java.awt.Toolkit;

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
  public static JTSTestBuilder instance()
  {
  	return app;
  }
  
  public static TestBuilderModel model() { return instance().tbModel; }

  public static Geometry getGeometryA() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(0);
  }

  public static Geometry getGeometryB() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(1);
  }

// MD - no longer used
//  private static final String OPT_GEOMOP = "geomop";
  private static final String OPT_GEOMFUNC = "geomfunc";

  private static GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private static CommandLine commandLine = createCmdLine();
  public static JTSTestBuilder app;
  
  private static CommandLine createCmdLine() {
    commandLine = new CommandLine('-');

//    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMOP, 1));

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
    	
    	// set the look and feel, using user-defined LAF if present
    	// eg. Metal - -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel
      String laf = System.getProperty("swing.defaultlaf");
      if (laf == null) {
        laf = UIManager.getSystemLookAndFeelClassName();
      }
      UIManager.setLookAndFeel(laf);

      app = new JTSTestBuilder();
      app.initFrame();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void readArgs(String[] args) throws ParseException,
			ClassNotFoundException {
		commandLine.parse(args);

		/*
		 * String geomOpClassname = null; // parse geomop argument, if any if
		 * (args.length > 0) { if (args[0].equalsIgnoreCase("-" + OPT_GEOMOP) &&
		 * args.length == 2) { geomOpClassname = args[1]; geometryOp =
		 * GeometryOperationLoader.createGeometryOperation(JTSTestBuilder.class.getClassLoader(),
		 * geomOpClassname); } else { System.out.println("Expecting argument
		 * '-geomop <geometryOperation class>', found: " + args[0]); } if
		 * (geometryOp == null) { System.exit(0); } System.out.println("Using
		 * Geometry Operation: " + geomOpClassname); }
		 * 
		 * if (commandLine.hasOption(OPT_GEOMOP)) { String geomOpClassname =
		 * commandLine.getOption(OPT_GEOMOP).getArg(0); geometryOp =
		 * GeometryOperationLoader.createGeometryOperation(TopologyTestApp.class.getClassLoader(),
		 * geomOpClassname); if (geometryOp == null) { System.exit(0); }
		 * System.out.println("Using Geometry Operation: " + geomOpClassname); }
		 */

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