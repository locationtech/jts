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

package org.locationtech.jtstest.util.io;

import java.io.IOException;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.DefaultJDOMFactory;
import org.jdom.JDOMFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.input.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This builder works in parallell with {@link LineNumberElement}
 * to provide each element with information on its beginning and
 * ending line number in the corresponding source.
 * This only works for SAX parsers that supply that information, and
 * since this is optional, there are no guarantees.
 * <p>
 * Note that this builder always creates its own for each
 * build, thereby cancelling any previous call to setFactory.
 * <p>
 * All elements created are instances of {@link LineNumberElement}.
 * No other construct currently receive line number information.
 *
 * @author Per Norrman
 *
 */
public class LineNumberSAXBuilder extends SAXBuilder
{

        protected SAXHandler createContentHandler()
	{
		return new MySAXHandler(new MyFactory());
	}



	private class MyFactory extends DefaultJDOMFactory
	{

		public Element element(String name)
		{
			return new LineNumberElement(name);
		}

		public Element element(String name, String prefix, String uri)
		{
			return new LineNumberElement(name, prefix, uri);
		}

		public Element element(String name, Namespace namespace)
		{
			return new LineNumberElement(name, namespace);
		}

		public Element element(String name, String uri)
		{
			return new LineNumberElement(name, uri);
		}

	}

	private class MySAXHandler extends SAXHandler
	{

		public MySAXHandler(JDOMFactory f)
		{
			super(f);
		}

		/** override */
		public void startElement(
			String arg0,
			String arg1,
			String arg2,
			Attributes arg3)
			throws SAXException
		{
			super.startElement(arg0, arg1, arg2, arg3);
			Locator l = getDocumentLocator();
			if (l != null)
			{
				((LineNumberElement) getCurrentElement()).setStartLine(
					l.getLineNumber());
			}
		}

		/** override */
		public void endElement(String arg0, String arg1, String arg2)
			throws SAXException
		{
			Locator l = getDocumentLocator();
			if (l != null)
			{
				((LineNumberElement) getCurrentElement()).setEndLine(
					l.getLineNumber());
			}

			super.endElement(arg0, arg1, arg2);
		}

	}

}
