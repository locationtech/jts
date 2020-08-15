package org.locationtech.jtstest.util;

import java.io.IOException;

public class CommandRunnerTester {
  public static void main(String[] args) {
    CommandRunnerTester tester = new CommandRunnerTester();
    tester.run();
  }

  private void run() {
    //String cmd = "xdir.exe foo";
    String cmd = "D:\\proj\\jts\\git\\jts-md\\bin\\jtsop";
    
    
    CommandRunner runner = new CommandRunner();
    int exitval = 0;
    try {
      exitval = runner.exec(cmd);
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println(exitval);
    
    System.out.println("==== Stdout ===");
    System.out.println(runner.getStdout());
    
    System.out.println("==== Stderr ===");
    System.out.println(runner.getStderr());
  }
}
