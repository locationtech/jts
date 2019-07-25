package org.locationtech.jtstest.cmd;

import junit.framework.TestCase;

public class JTSOpCmdTest extends TestCase {
  public JTSOpCmdTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {JTSOpCmdTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }
  
  public void testHelp() {
    runCmd("-help");
  }
  
  public void runCmd(String ... args)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
    try {
      JTSOpCmd.CmdArgs cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
