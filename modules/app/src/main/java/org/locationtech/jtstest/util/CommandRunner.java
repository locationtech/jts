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
package org.locationtech.jtstest.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Runs an OS command, capturing stdout and stderr
 * 
 * @author Martin Davis
 *
 */
public class CommandRunner {

  private String stdout;
  private String stderr;

  public int exec(String cmd) throws IOException, InterruptedException {
    return exec(cmd, null);
  }
  
  /**
   * Executes a command and returns the contents of stdout as a string.
   * The command should be a single line, otherwise things seem to hang.
   * 
   * @param cmd command to execute (should be a single line)
   * @param stdin 
   * @return text of stdout
   * @throws IOException
   * @throws InterruptedException
   */
  public int exec(String cmd, String stdinData) throws IOException, InterruptedException {
    // ensure cmd is single line (seems to hang otherwise
    
    String[] osCmd = cmdArray(cmd);
    
    Process process = Runtime.getRuntime().exec( osCmd );
    
    /**
     * Always write something to stdin, otherwise process might hang
     */
    byte[] stdinBytes = new byte[0];
    if (stdinData != null) {
      stdinBytes = stdinData.getBytes();
    }
    OutputStream stdinOS = process.getOutputStream();
    stdinOS.write(stdinBytes);
    stdinOS.flush();
    stdinOS.close();
    
    StreamGrabber stdoutReader = 
        new StreamGrabber(process.getInputStream());
    Thread ot = new Thread(stdoutReader);
    ot.start();
    //Executors.newSingleThreadExecutor().submit(stdoutReader);
    
    StreamGrabber stderrReader = 
        new StreamGrabber(process.getErrorStream());
    Thread et = new Thread(stderrReader);
    et.start();
    
    int exitVal = process.waitFor();
    ot.join();
    et.join();
    
    stdout = stdoutReader.getOutput();
    stderr = stderrReader.getOutput();
    return exitVal;
  }

  public String getStdout() {
    return stdout;
  }
  
  public String getStderr() {
    return stderr;
  }
  
  private String[] cmdArray(String cmd) {
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
    return osCmd;
  }
}
