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

package org.locationtech.jtstest.util.io;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class extends a normal Element with a traceback to its
 * beginning and endling line number, if available and reported.
 * <p>
 * Each instance is created using a factory internal to the
 * LineNumberSAXBuilder class.
 *
 * @author Per Norrman
 *
 */
public class LineNumberElement extends Element
{
    private int _startLine;
    private int _endLine;

    public LineNumberElement(String name) {
        super(name);
    }

	public LineNumberElement()
	{
		super();
	}

	public LineNumberElement(String name, String uri)
	{
		super(name, uri);
	}

	public LineNumberElement(String name, String prefix, String uri)
	{
		super(name, prefix, uri);
	}

    public LineNumberElement(String name, Namespace namespace) {
        super(name, namespace);
    }



	public int getEndLine()
	{
		return _endLine;
	}

	public int getStartLine()
	{
		return _startLine;
	}

	public void setEndLine(int i)
	{
		_endLine = i;
	}

	public void setStartLine(int i)
	{
		_startLine = i;
	}

}
