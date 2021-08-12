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

public class CommandOutput {
  
  private StringBuilder output = new StringBuilder();
  private boolean isCapture = false;
  
  public CommandOutput() {
    
  }
  
  public CommandOutput(boolean isCapture) {
    this.isCapture = true;
  }
  
  public void println() {
    if (isCapture ) {
      output.append("\n");
    }
    else {
      System.out.println();
    }
  }
  
  public void println(Object o) {
    if (isCapture ) {
      output.append(o);
      output.append("\n");
    }
    else {
      System.out.println(o);
    }
  }
  
  public void print(String s) {
    if (isCapture ) {
      output.append(s);
    }
    else {
      System.out.print(s);
    }
  }
  
  public String getOutput() {
    return output.toString();
  }

}
