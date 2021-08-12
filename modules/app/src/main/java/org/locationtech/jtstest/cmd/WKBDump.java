/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.cmd;

import java.io.PrintWriter;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jtstest.util.io.WKBDumper;

/**
 * Dumps out WKB in a structured formatted way.
 * 
 * Usage:
 * WKBDump [ hex ]
 * 
 * @author mdavis
 *
 */
public class WKBDump {
  
  public static void main(String[] args)
  { 
    String hex = null;
    if (args.length >= 1) {
      hex = args[0];
    }
    
    if (hex != null) {
      byte[] wkb = WKBReader.hexToBytes(hex);
      PrintWriter writer = new PrintWriter(System.out);
      WKBDumper.dump(wkb, writer);
      writer.close();
    }
  }
}
