/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
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
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jtstest.geomfunction.GeometryFunctionInvocation;
import org.locationtech.jtstest.testbuilder.CommandPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.util.CommandRunner;
import org.locationtech.jtstest.util.io.MultiFormatReader;

public class CommandController {

  public static CommandPanel ui() {
    return JTSTestBuilder.frame().getCommandPanel();
  }
  
  public static void execCommand(String name, String cmdIn, boolean useStdin, boolean isStdinWKT) {
    String cmd = expandCommand(cmdIn);
    
    String stdin = null;
    if (useStdin) {
      if (isStdinWKT) {
        stdin = valueWKT(getGeometry(0));
      }
      else {
        stdin = valueWKB(getGeometry(0));
      }
    }
    //System.out.println(cmd);
    int returnCode = -1;
    String errMsg = "";
    Geometry result = null;
    CommandRunner runner = new CommandRunner();
    try {
       returnCode = runner.exec(cmd, stdin);
       errMsg = runner.getStderr();
    } catch (Exception e) {
      errMsg = e.getClass().getName() + " : " + e.getMessage();
      //showError(e);
    }
    boolean isSuccess = returnCode == 0 && errMsg.length() == 0;
    
    if (isSuccess) {
      /**
       * Save successful command in history
       * (although the result parsing may still fail)
       */
      ui().saveCommand(cmdIn);
      String resultStr = runner.getStdout();
      ui().setOutput(limitLength(resultStr, 200));
      result = loadResult( name, resultStr );
    }
    else {
      if (errMsg.length() == 0)
        errMsg = "Return code = " + returnCode;
      //JTSTestBuilder.controller().clearResult();
      ui().setError(errMsg);
    }
    logCommand(name, cmdIn, result, errMsg);

  }
  private static void logCommand(String name, String cmd, Geometry geom, String errMsg) {
    String cmdLog = name + ": " + limitLength( cmd, 200);
    if (geom != null) {
      String geomLog = GeometryFunctionInvocation.toString(geom);
      cmdLog += "\n ==> " + geomLog;
    }
    if (errMsg.length() > 0) {
      String errLog = limitLength( errMsg, 200);
      cmdLog += "\n ERROR: " + errLog;
    }
    
    JTSTestBuilder.controller().displayInfo(cmdLog, false);
  }
  public static final String VAR_A = "#a#";
  public static final String VAR_A_WKB = "#awkb#";
  public static final String VAR_B = "#b#";
  public static final String VAR_B_WKB = "#bwkb#";
  
  private static String expandCommand(String cmdSrc) {
    String cmdLine = removeNewline(cmdSrc);
    
    String cmd = cmdLine;
    
    if (cmdLine.contains(VAR_A)) {
      cmd = cmd.replace(VAR_A, valueWKT(getGeometry(0)));
    }
    if (cmdLine.contains(VAR_A_WKB)) {
      cmd = cmd.replace(VAR_A_WKB, valueWKB(getGeometry(0)));;
    }
    if (cmdLine.contains(VAR_B_WKB)) {
      cmd = cmd.replace(VAR_B_WKB, valueWKB(getGeometry(1)));;
    }
    if (cmdLine.contains(VAR_B_WKB)) {
      cmd = cmd.replace(VAR_B_WKB, valueWKB(getGeometry(1)));;
    }
    return cmd;
  }

  private static Geometry getGeometry(int i) {
    return JTSTestBuilderController.model().getCurrentCase().getGeometry(i);
  }
  
  private static String valueWKT(Geometry geom) {
    if (geom == null) return "";
    return geom.toString();
  }

  private static String valueWKB(Geometry geom) {
    if (geom == null) return "";
    WKBWriter wkbWriter = new WKBWriter();
    return WKBWriter.toHex(wkbWriter.write(geom));
  }

  private static String removeNewline(String s) {
    return s.replace('\n', ' ');
  }
  
  private static String limitLength(String s, int n) {
    if (s.length() <= n) return s;
    return s.substring(0, n) + "...";
  }
  
  private static Geometry loadResult(String name, String output) {
    JTSTestBuilder.frame().showResultWKTTab();
    MultiFormatReader reader = new MultiFormatReader(new GeometryFactory());
    reader.setStrict(false);
    Geometry result = null;
    try {
      result = reader.read(output);
      JTSTestBuilder.controller().setResult(name, result);
    } catch (ParseException | IOException e) {
      showError(name, e);
    }
    return result;
  }
  
  private static void showError(String name, Exception e) {
    //String msg = e.getClass().getName() + " : " + e.getMessage();
    JTSTestBuilder.controller().setResult(name, e);
  }
  
  // NOT USED
  
  /**
   * Executes a command and returns the contents of stdout as a string.
   * The command should be a single line, otherwise things seem to hang.
   * 
   * @param cmd command to execute (should be a single line)
   * @return text of stdout
   * @throws IOException
   * @throws InterruptedException
   */
  private static String exec(String cmd) throws IOException, InterruptedException {
    // ensure cmd is single line (seems to hang otherwise
    
    boolean isWindows = System.getProperty("os.name")
        .toLowerCase().startsWith("windows");
    // -- Linux --
    // Run a shell command
    // Process process = Runtime.getRuntime().exec("ls /home/foo/");
    // Run a shell script
    // Process process = Runtime.getRuntime().exec("path/to/hello.sh");

    // -- Windows --
    // Run a command
    //Process process = Runtime.getRuntime().exec("cmd /c dir C:\\Users\\foo");
    
    /**
     * Use array form of exec args, because that doesn't do weird things with quotes
     */
    String[] osCmd = new String[3];
    if (isWindows) {
      osCmd[0] = "cmd";
      osCmd[1] = "/c";     
    }
    else {  // assume *nix
      osCmd[0] = "sh";
      osCmd[1] = "-c";
    }
    osCmd[2] = cmd;
    
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
