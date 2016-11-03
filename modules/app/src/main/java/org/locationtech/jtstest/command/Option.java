

/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.command;

/**
 * The parameters for an instance of an option occurring in a command
 * @version 1.7
 */
public class Option {
  OptionSpec optSpec;
  String[] args;            // the actual option args found

  public Option(OptionSpec spec, String[] _args) {
    optSpec = spec;
    args = _args;
  }

  public String getName() { return optSpec.getName(); }
  public int getNumArgs() { return args.length; }
  public String getArg(int i)
  {
    return args[i];
  }
  public int getArgAsInt(int i)
  {
    return Integer.parseInt(args[i]);
  }

}
