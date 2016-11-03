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
package org.locationtech.jtstest.testrunner;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.util.StringUtil;


/**
 * Parameters passed to a main method (also known as "command-line arguments").
 * Arguments are recognized only if they are of the form -key or -key:value
 *
 * @version 1.7
 */
public class Parameters {
    private static List arguments = null;
    private static List requiredKeys = new Vector();
    private static List allowedKeys = new Vector();
    private static Parameters instance = null;

    /**
     * Sets the command-line arguments. reqdKeys may be set to null if there
     * are no required command-line arguments. Same with optionalKeys.
     */
    public static void setParameters(String[] args, String[] reqdKeys, String[] optionalKeys) {
        arguments = Arrays.asList(args);
        if (reqdKeys != null)
            requiredKeys = StringUtil.toLowerCase(Arrays.asList(reqdKeys));
        if (reqdKeys != null)
            allowedKeys.addAll(StringUtil.toLowerCase(Arrays.asList(reqdKeys)));
        if (optionalKeys != null)
            allowedKeys.addAll(StringUtil.toLowerCase(Arrays.asList(optionalKeys)));
    }

    /**
     * Returns the singleton. Be sure to call #setParameters first.
     */
    public static Parameters getInstance() {
        Assert.isTrue(arguments != null);
        if (instance == null)
            instance = new Parameters();
        return instance;
    }
    ////////////////////////////////////////////////////////////////////////////////
    private Hashtable hashtable = new Hashtable();

    /**
     * Creates a Parameters object for the given main-method arguments.
     */
    private Parameters() {
        for (Iterator i = arguments.iterator(); i.hasNext();) {
            String arg = (String) i.next();
            arg = arg.toLowerCase();
            if (!arg.startsWith("-"))
                throw new IllegalArgumentException(
                    "Command-line argument does not start with '-': " + arg);
            int colonIndex = arg.indexOf(":");
            String key;
            String value;
            if (colonIndex >= 0) {
                key = arg.substring(1, colonIndex);
                value = arg.substring(colonIndex + 1);
            } else {
                key = arg.substring(1);
                value = "";
            }
            if (!allowedKeys.contains(key))
                throw new IllegalArgumentException(
                    "Unrecognized command-line argument: "
                        + arg.substring(1)
                        + ". Valid arguments are: "
                        + StringUtil.toCommaDelimitedString(allowedKeys));
            hashtable.put(key, value);
        }
        for (Iterator i = requiredKeys.iterator(); i.hasNext();) {
            String requiredKey = (String) i.next();
            if (!hashtable.containsKey(requiredKey))
                throw new IllegalArgumentException(
                    "Required command-line argument is missing: " + requiredKey);
        }
    }

    /**
     * Returns true if key is one of the parameters. Case-insensitive.
     */
    public boolean contains(String key) {
        Assert.isTrue(allowedKeys.contains(key.toLowerCase()));
        return hashtable.containsKey(key.toLowerCase());
    }

    /**
     * Returns the value of the specified parameter, or null if there is no such key. Case-insensitive.
     */
    public String get(String key) {
        Assert.isTrue(allowedKeys.contains(key.toLowerCase()));
        return (String) hashtable.get(key.toLowerCase());
    }
}
