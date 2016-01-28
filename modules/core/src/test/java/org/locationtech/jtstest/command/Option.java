

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2016 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 *
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
