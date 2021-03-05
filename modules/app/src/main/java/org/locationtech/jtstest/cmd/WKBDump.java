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
