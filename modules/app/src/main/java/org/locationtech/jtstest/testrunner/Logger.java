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


/**
 * @version 1.7
 */
public interface Logger {

    /**
     * Writes an error message
     *
     * @param msg			the msg to be written
     *
     */
    public void writeErr(String msg);

    /**
     * Writes a status message
     *
     * @param msg			the msg to be written
     *
     */
    public void writeMsg(String msg);

    /**
     * Writes a warning message
     *
     * @param msg			the msg to be written
     *
     */
    public void writeWarn(String msg);
}
