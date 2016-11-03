

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

import java.util.*;
/**
 * Specifes the syntax for a single option on a
 * command line
 *
 * ToDo:
 * - add syntax pattern parsing
 * Syntax patterns are similar to Java type signatures
 *  F - float
 *  I - int
 *  L - long
 *  S - string
 *  B - boolean
 *  + - one or more
 * eg:  "FIS+" takes a double, int, and one or more Strings
 * @version 1.7
 */
public class OptionSpec {

  public final static int NARGS_ZERO_OR_MORE  = -1;
  public final static int NARGS_ONE_OR_MORE   = -2;
  public final static int NARGS_ZERO_OR_ONE   = -3;

  public final static String OPTION_FREE_ARGS = "**FREE_ARGS**";   // option name for free args

  String name;
  int nAllowedArgs = 0;     // number of arguments allowed
  String syntaxPattern;
  String argDoc = "";            // arg syntax description
  String doc = "";               // option description

  Vector options = new Vector();

  public OptionSpec(String optName)
  {
    name = optName;
    nAllowedArgs = 0;
  }

  public OptionSpec(String optName, int nAllowed)
  {
    this(optName);
    // check for invalid input
    if (nAllowedArgs >= NARGS_ZERO_OR_ONE)
      nAllowedArgs = nAllowed;
  }

  public OptionSpec(String optName, String _syntaxPattern) {
    this(optName);
    syntaxPattern = _syntaxPattern;
  }

  public void setDoc(String _argDoc, String docLine)
  {
    argDoc = _argDoc;
    doc = docLine;
  }
  public String getArgDesc() {    return argDoc;  }
  public String getDocDesc() {    return doc;  }

  public int getNumOptions() { return options.size(); }
  public Option getOption(int i)
  {
    if (options.size() > 0)
      return (Option) options.elementAt(i);
    return null;
  }


  public Iterator getOptions()
  {
      return options.iterator();
  }

  public boolean hasOption()
  {
    return options.size() > 0;
  }

  void addOption(Option opt)
  {
    options.addElement(opt);
  }


  String getName() { return name; }
  int getAllowedArgs() { return nAllowedArgs; }
  Option parse(String[] args)
    throws ParseException
  {
    checkNumArgs(args);
    return new Option(this, args);
  }

  void checkNumArgs(String[] args)
    throws ParseException
  {
    if (nAllowedArgs == NARGS_ZERO_OR_MORE) {
        // args must be ok
    }
    else if (nAllowedArgs == NARGS_ONE_OR_MORE) {
      if (args.length <= 0)
        throw new ParseException("option " + name + ": expected one or more args, found " + args.length);
    }
    else if (nAllowedArgs == NARGS_ZERO_OR_ONE) {
      if (args.length > 1)
        throw new ParseException("option " + name + ": expected zero or one arg, found " + args.length);
    }
    else if (args.length != nAllowedArgs)
      throw new ParseException("option " + name + ": expected "
                                     + nAllowedArgs + " args, found " + args.length);
  }

}
