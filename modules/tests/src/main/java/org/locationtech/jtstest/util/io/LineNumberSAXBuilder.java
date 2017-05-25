/*
 Copyright (c) 2016 Vivid Solutions.
 Copyright (C) 2000-2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.
 
 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.
 
 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>. 

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
