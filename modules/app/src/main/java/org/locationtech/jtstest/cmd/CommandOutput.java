/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.cmd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CommandOutput {
  
  private StringBuilder outputBuffer = new StringBuilder();
  private boolean isCapture = false;
  private String outputFilename;
  private PrintWriter outWriter;
  
  public CommandOutput() {
    outWriter = new PrintWriter(System.out, true);
  }
  
  public CommandOutput(boolean isCapture) {
    this.isCapture = true;
  }
  
  public CommandOutput(String outputFile) {
    this.outputFilename = outputFile;
    File file = new File(outputFile);
    FileWriter fw = null;
    try {
      fw = new FileWriter(file);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    outWriter = new PrintWriter(fw, true);
  }

  public void println() {
    if (isCapture ) {
      outputBuffer.append("\n");
    }
    else {
      outWriter.println();
    }
  }
  
  public void logln(Object o) {
    if (isCapture ) {
      outputBuffer.append(o);
      outputBuffer.append("\n");
    }
    else {
      System.out.println(o);
    }
  }
  
  public void println(Object o) {
    if (isCapture ) {
      outputBuffer.append(o);
      outputBuffer.append("\n");
    }
    else {
      outWriter.println(o);
    }
  }
  
  public void print(String s) {
    if (isCapture ) {
      outputBuffer.append(s);
    }
    else {
      outWriter.print(s);
    }
  }
  
  public String getOutput() {
    return outputBuffer.toString();
  }

}
