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
  
  public void testFileNotFoundA() {
    runCmdError("-a", "foo.wkt");
  }
  
  public void testFileNotFoundB() {
    runCmdError("-b", "foo.wkt");
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
  public void runCmdError(String ... args)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
    try {
      JTSOpCmd.CmdArgs cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
    } 
    catch (CommandError e) {
      // expected result
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    assertTrue("Expected error but command completed successfully", false);
  }
}
