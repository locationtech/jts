package org.locationtech.jtstest.cmd;

public class CommandError extends RuntimeException {
  public CommandError(String msg) {
    super(msg);
  }
  public CommandError(String msg, String val) {
    super(msg + ": " + val);
  }
}
