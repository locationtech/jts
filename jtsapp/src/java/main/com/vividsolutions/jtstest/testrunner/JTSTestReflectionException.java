package com.vividsolutions.jtstest.testrunner;

/**
 * An Exception which indicates a problem during reflection
 *
 * @author Martin Davis
 * @version 1.7
 */
public class JTSTestReflectionException
    extends Exception
{
  public JTSTestReflectionException(String message) {
    super(message);
  }
  
  public JTSTestReflectionException(String opName, Object[] args) {
    super(createMessage(opName, args));
  }
  
  private static String createMessage(String opName, Object[] args) {
		String msg = "Could not find Geometry method: " + opName + "(";
		for (int j = 0; j < args.length; j++) {
			if (j > 0) {
				msg += ", ";
			}
			msg += args[j].getClass().getName();
		}
		msg += ")";
		return msg;
	}

}