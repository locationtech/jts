/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.util.io.MultiFormatReader;

public class CommandController {

  public static void execCommand(String cmd) {
    //System.out.println(cmd);
    String output;
    try {
      JTSTestBuilderController.frame().showResultWKTTab();
      output = CommandController.exec(cmd);
      loadResult(output);
    } catch (Exception e) {
      showError(e);
    }
  }
   
  private static void loadResult(String output) {
    MultiFormatReader reader = new MultiFormatReader(new GeometryFactory());
    try {
      Geometry result = reader.read(output);
      
      JTSTestBuilder.controller().setResult(result);
      
    } catch (ParseException | IOException e) {
      showError(e);
    }
  }
  
  private static void showError(Exception e) {
    //String msg = e.getClass().getName() + " : " + e.getMessage();
    JTSTestBuilder.controller().setResult(e);
  }
  
  public static String exec(String cmd) throws IOException, InterruptedException {
    boolean isWindows = System.getProperty("os.name")
        .toLowerCase().startsWith("windows");
    
    String osShellPrefix = isWindows ? "cmd /c" : "";
    String osCmd = osShellPrefix + " " + cmd;

      // -- Linux --
      
      // Run a shell command
      // Process process = Runtime.getRuntime().exec("ls /home/foo/");

      // Run a shell script
      // Process process = Runtime.getRuntime().exec("path/to/hello.sh");

      // -- Windows --
      
      // Run a command
      //Process process = Runtime.getRuntime().exec("cmd /c dir C:\\Users\\foo");

      //Run a bat file
      Process process = Runtime.getRuntime().exec( osCmd );

      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      int exitVal = process.waitFor();
      if (exitVal != 0) {
        // TODO: handle error
        return null;
      }
      return output.toString();
  }
}
