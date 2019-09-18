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
