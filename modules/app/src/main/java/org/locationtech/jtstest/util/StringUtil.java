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
package org.locationtech.jtstest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.locationtech.jts.util.Assert;


/**
 *  Useful string utilities
 *
 *@author     jaquino
 *@created    June 22, 2001
 *
 * @version 1.7
 */
public class StringUtil 
{
    public final static String newLine = System.getProperty("line.separator");

    public static String removeFromEnd(String s, String strToRemove)
    {
    	if (s == null || strToRemove == null) return s;
    	if (s.length() < strToRemove.length()) return s;
    	int subLoc = s.length() - strToRemove.length();
    	if (s.substring(subLoc).equalsIgnoreCase(strToRemove))
    		return s.substring(0, subLoc);
    	return s;
    }
    
  	/**
  	 * Capitalizes the given string.
  	 * 
  	 * @param s the string to capitalize
  	 * @return the capitalized string
  	 */
  	public static String capitalize(String s)
  	{
  		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  	}
    /**
     *  Returns true if s can be converted to an int.
     */
    public static boolean isInteger(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }   

    /**
     *  Returns an throwable's stack trace
     */
    public static String getStackTrace(Throwable t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        t.printStackTrace(ps);
        return os.toString();
    }

    public static String getStackTrace(Throwable t, int depth) {
        String stackTrace = "";
        StringReader stringReader = new StringReader(getStackTrace(t));
        LineNumberReader lineNumberReader = new LineNumberReader(stringReader);
        for (int i = 0; i < depth; i++) {
            try {
                stackTrace += lineNumberReader.readLine() + newLine;
            } catch (IOException e) {
                Assert.shouldNeverReachHere();
            }
        }
        return stackTrace;
    }

    /**
     *  Converts the milliseconds value into a String of the form "9d 22h 15m 8s".
     */
    public static String getTimeString(long milliseconds) {
        long remainder = milliseconds;
        long days = remainder / 86400000;
        remainder = remainder % 86400000;
        long hours = remainder / 3600000;
        remainder = remainder % 3600000;
        long minutes = remainder / 60000;
        remainder = remainder % 60000;
        long seconds = remainder / 1000;
        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    /**
     *  Returns true if substring is indeed a substring of string.
     *  Case-insensitive.
     */
    public static boolean containsIgnoreCase(String string, String substring) {
        return contains(string.toLowerCase(), substring.toLowerCase());
    }

    /**
     *  Returns true if substring is indeed a substring of string.
     */
    public static boolean contains(String string, String substring) {
        return string.indexOf(substring) > -1;
    }

    /**
     *  Returns a string with all occurrences of oldChar replaced by newStr
     */
    public static String replace(String str, char oldChar, String newStr) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == oldChar) {
                buf.append(newStr);
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     *  Returns a String of the given length consisting entirely of the given
     *  character
     */
    public static String stringOfChar(char ch, int count) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < count; i++) {
            buf.append(ch);
        }
        return buf.toString();
    }

    public static String indent(String original, int spaces) {
        String indent = stringOfChar(' ', spaces);
        String indented = indent + original;

        indented = replaceAll(indented, "\r\n", "<<<<.CRLF.>>>>");
        indented = replaceAll(indented, "\r", "<<<<.CR.>>>>");
        indented = replaceAll(indented, "\n", "<<<<.LF.>>>>");

        indented = replaceAll(indented, "<<<<.CRLF.>>>>", "\r\n" + indent);
        indented = replaceAll(indented, "<<<<.CR.>>>>", "\r" + indent);
        indented = replaceAll(indented, "<<<<.LF.>>>>", "\n" + indent);
        return indented;
    }

    /**
     *  Returns the elements of v in uppercase
     */
    public static Vector toUpperCase(Vector v) {
        Vector result = new Vector();
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            String s = e.nextElement().toString();
            result.add(s.toUpperCase());
        }
        return result;
    }

    /**
     *  Returns the elements of v in lowercase
     */
    public static Vector toLowerCase(List v) {
        Vector result = new Vector();
        for (Iterator i = v.iterator(); i.hasNext();) {
            String s = i.next().toString();
            result.add(s.toLowerCase());
        }
        return result;
    }

    /**
     *  Returns the elements of c separated by commas and enclosed in
     *  single-quotes
     */
    public static String toCommaDelimitedStringInQuotes(Collection c) {
        StringBuffer result = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext();) {
            Object o = i.next();
            result.append(",'" + o.toString() + "'");
        }
        return result.substring(1);
    }

    /**
     *  Returns the elements of c separated by commas. c must not be empty.
     */
    public static String toCommaDelimitedString(Collection c) {
        if (c.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuffer result = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext();) {
            Object o = i.next();
            result.append(", " + o.toString());
        }
        return result.substring(1);
    }

    /**
     *  Converts the comma-delimited string into a List of trimmed strings.
     */
    public static List fromCommaDelimitedString(String s) {
        ArrayList result = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(s, ",");
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken().toString().trim());
        }
        return result;
    }

    /**
     *  If s is null, returns "null"; otherwise, returns s.
     */
    public static String toStringNeverNull(Object o) {
        return o == null ? "null" : o.toString();
    }

    /**
     *  Replaces all instances of the String o with the String n in the
     *  StringBuffer orig if all is true, or only the first instance if all is
     *  false. Posted by Steve Chapel <schapel@breakthr.com> on UseNet
     */
    public static void replace(StringBuffer orig, String o, String n, boolean all) {
        if (orig == null || o == null || o.length() == 0 || n == null) {
            throw new IllegalArgumentException("Null or zero-length String");
        }
        int i = 0;
        while (i + o.length() <= orig.length()) {
            if (orig.substring(i, i + o.length()).equals(o)) {
                orig.replace(i, i + o.length(), n);
                if (!all) {
                    break;
                } else {
                    i += n.length();
                }
            } else {
                i++;
            }
        }
    }

    /**
     *  Returns original with all occurrences of oldSubstring replaced by
     *  newSubstring
     */
    public static String replaceAll(String original, String oldSubstring, String newSubstring) {
        return replace(original, oldSubstring, newSubstring, true);
    }

    /**
     *  Returns original with the first occurrenc of oldSubstring replaced by
     *  newSubstring
     */
    public static String replaceFirst(String original, String oldSubstring, String newSubstring) {
        return replace(original, oldSubstring, newSubstring, false);
    }

    /**
     *  Pads the String with the given character until it has the given length. If
     *  original is longer than the given length, returns original.
     */
    public static String leftPad(String original, int length, char padChar) {
        if (original.length() >= length) {
            return original;
        }
        return stringOfChar(padChar, length - original.length()) + original;
    }

    /**
     *  Pads the String with the given character until it has the given length. If
     *  original is longer than the given length, returns original.
     */
    public static String rightPad(String original, int length, char padChar) {
        if (original.length() >= length) {
            return original;
        }
        return original + stringOfChar(padChar, length - original.length());
    }

    /**
     *  Removes the HTML tags from the given String, inserting line breaks at
     *  appropriate places. Needs a little work.
     */
    public static String stripHTMLTags(String original) {
        //Strip the tags from the HTML description
        boolean skipping = false;
        boolean writing = false;
        StringBuffer buffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(original, "<>", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equalsIgnoreCase("<")) {
                skipping = true;
                writing = false;
                continue;
            }
            if (token.equalsIgnoreCase(">")) {
                skipping = false;
                continue;
            }
            if (!skipping) {
                if (token.trim().length() == 0) {
                    continue;
                }
                if (!writing) {
                    buffer.append("\n");
                }
                writing = true;
                buffer.append(token.trim());
            }
        }
        return buffer.toString();
    }

    /**
     *  Returns d as a string truncated to the specified number of decimal places
     */
    public static String format(double d, int decimals) {
        double factor = Math.pow(10, decimals);
        double digits = Math.round(factor * d);
        return ((int) Math.floor(digits / factor)) + "." + ((int) (digits % factor));
    }

    /**
     *  Line-wraps a string s by inserting CR-LF instead of the first space after the nth
     *  columns.
     */
    public static String wrap(String s, int n) {
        StringBuffer b = new StringBuffer();
        boolean wrapPending = false;
        for (int i = 0; i < s.length(); i++) {
            if (i % n == 0 && i > 0) {
                wrapPending = true;
            }
            char c = s.charAt(i);
            if (wrapPending && c == ' ') {
                b.append("\n");
                wrapPending = false;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     *  Removes vowels from the string. Case-insensitive.
     */
    public static String removeVowels(String s) {
        String result = s;
        result = replaceAll(s, "a", "");
        result = replaceAll(s, "e", "");
        result = replaceAll(s, "i", "");
        result = replaceAll(s, "o", "");
        result = replaceAll(s, "u", "");
        result = replaceAll(s, "A", "");
        result = replaceAll(s, "E", "");
        result = replaceAll(s, "I", "");
        result = replaceAll(s, "O", "");
        result = replaceAll(s, "U", "");
        return result;
    }

    /**
     *  Removes vowels from the string except those that start words.
     *  Case-insensitive.
     */
    public static String removeVowelsSkipStarts(String s) {
        String result = s;
        if (!s.startsWith(" ")) {
            result = result.substring(1);
        }
        result = encodeStartingVowels(result);
        result = removeVowels(result);
        result = decodeStartingVowels(result);
        if (!s.startsWith(" ")) {
            result = s.charAt(0) + result;
        }
        return result;
    }

    /**
     *  Replaces consecutive instances of characters with single instances.
     *  Case-insensitive.
     */
    public static String removeConsecutiveDuplicates(String s) {
        String previous = "??";
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            String c = s.charAt(i) + "";
            if (!previous.equalsIgnoreCase(c)) {
                result.append(c);
            }
            previous = c;
        }
        return result.toString();
    }

    /**
     *  Returns the position of the first occurrence of the given character found
     *  in s starting at start. Ignores text within pairs of parentheses. Returns
     *  -1 if no occurrence is found.
     */
    public static int indexOfIgnoreParentheses(char c, String s, int start) {
        int level = 0;
        for (int i = start; i < s.length(); i++) {
            char other = s.charAt(i);
            if (other == '(') {
                level++;
            } else if (other == ')') {
                level--;
            } else if (other == c && level == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     *  Returns original with occurrences of oldSubstring replaced by
     *  newSubstring. Set all to true to replace all occurrences, or false to
     *  replace the first occurrence only.
     */
    public static String replace(
        String original,
        String oldSubstring,
        String newSubstring,
        boolean all) {
        StringBuffer b = new StringBuffer(original);
        replace(b, oldSubstring, newSubstring, all);
        return b.toString();
    }

    /**
     *  Replaces vowels that start words with a special code
     */
    private static String encodeStartingVowels(String s) {
        String result = s;
        result = replaceAll(s, " a", "!~b");
        result = replaceAll(s, " e", "!~f");
        result = replaceAll(s, " i", "!~j");
        result = replaceAll(s, " o", "!~p");
        result = replaceAll(s, " u", "!~v");
        result = replaceAll(s, " A", "!~B");
        result = replaceAll(s, " E", "!~F");
        result = replaceAll(s, " I", "!~J");
        result = replaceAll(s, " O", "!~P");
        result = replaceAll(s, " U", "!~V");
        return result;
    }

    /**
     *  Decodes strings returned by #encodeStartingVowels
     */
    private static String decodeStartingVowels(String s) {
        String result = s;
        result = replaceAll(s, "!~b", " a");
        result = replaceAll(s, "!~f", " e");
        result = replaceAll(s, "!~j", " i");
        result = replaceAll(s, "!~p", " o");
        result = replaceAll(s, "!~v", " u");
        result = replaceAll(s, "!~B", " A");
        result = replaceAll(s, "!~F", " E");
        result = replaceAll(s, "!~J", " I");
        result = replaceAll(s, "!~P", " O");
        result = replaceAll(s, "!~V", " U");
        return result;
    }

    //From: Phil Hanna (pehanna@my-deja.com)
    //Subject: Re: special html characters and java???
    //Newsgroups: comp.lang.java.help
    //Date: 2000/09/16
    public static String escapeHTML(String s) {
        replace(s, "\r\n", "\n", true);
        replace(s, "\n\r", "\n", true);
        replace(s, "\r", "\n", true);
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<' :
                    sb.append("&lt;");
                    break;
                case '>' :
                    sb.append("&gt;");
                    break;
                case '&' :
                    sb.append("&amp;");
                    break;
                case '"' :
                    sb.append("&quot;");
                    break;
                case '\n' :
                    sb.append("<BR>");
                    break;
                default :
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    //Based on code from http://developer.java.sun.com/developer/qow/archive/104/index.html
    public static String currentMethodName() {
        StringWriter sw = new StringWriter();
        new Throwable().printStackTrace(new PrintWriter(sw));
        String callStack = sw.toString();
        int atPos = callStack.indexOf("at");
        atPos = callStack.indexOf("at", atPos + 1);
        int parenthesisPos = callStack.indexOf("(", atPos);
        return callStack.substring(atPos + 3, parenthesisPos);
    }

}
