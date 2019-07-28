package org.locationtech.jtstest.cmd;

public class CommandError extends RuntimeException {
  public CommandError(String msg) {
    super(msg);
  }
}
