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
package org.locationtech.jts.io.gml2;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.gml2.GeometryStrategies.ParseStrategy;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A SAX {@link DefaultHandler} which builds {@link Geometry}s 
 * from GML2-formatted geometries.
 * An XML parser can delegate SAX events to this handler
 * to parse and building Geometrys. 
 * <p>
 * This handler currently ignores both namespaces and prefixes. 
 * 
 * Hints: 
 * <ul>
 * <li>If your parent handler is a DefaultHandler register the parent handler to receive the errors and locator calls.
 * <li>Use {@link GeometryStrategies#findStrategy(String, String)} to help check for applicability
 * </ul>
 * 
 * @see DefaultHandler
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class GMLHandler extends DefaultHandler {

	/**
	 * This class is intended to log the SAX acitivity within a given element until its termination. 
	 * At this time, a new object of value is created and passed to the parent. 
	 * An object of value is typically either java.lang.* or a JTS Geometry
	 * This class is not intended for use outside this distribution, 
	 * and may change in subsequent versions.
	 *
	 * @author David Zwiers, Vivid Solutions.
	 */
	static class Handler {
		protected Attributes attrs = null;

		protected ParseStrategy strategy;

		/**
		 * @param strategy 
		 * @param attributes Nullable
		 */
		public Handler(ParseStrategy strategy, Attributes attributes) {
			if (attributes != null)
				this.attrs = new AttributesImpl(attributes);
			this.strategy = strategy;
		}

		protected StringBuffer text = null;

		/**
		 * Caches text for the future
		 * @param str
		 */
		public void addText(String str) {
			if (text == null)
				text = new StringBuffer();
			text.append(str);
		}

		protected List children = null;

		/**
		 * Store param for the future
		 * 
		 * @param obj
		 */
		public void keep(Object obj) {
			if (children == null)
				children = new LinkedList();
			children.add(obj);

		}

		/**
		 * @param gf GeometryFactory
		 * @return Parsed Object
		 * @throws SAXException 
		 */
		public Object create(GeometryFactory gf) throws SAXException {
			return strategy.parse(this, gf);
		}
	}

	private Stack stack = new Stack();

	private ErrorHandler delegate = null;

	private GeometryFactory gf = null;

	/**
	 * Creates a new handler.
	 * Allows the user to specify a delegate object for error / warning messages. 
	 * If the delegate also implements ContentHandler then the document Locator will be passed on.
	 * 
	 * @param gf Geometry Factory
	 * @param delegate Nullable
	 * 
	 * @see ErrorHandler
	 * @see ContentHandler
	 * @see ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 * @see org.xml.sax.Locator
	 * 
	 */
	public GMLHandler(GeometryFactory gf, ErrorHandler delegate) {
		this.delegate = delegate;
		this.gf = gf;
		stack.push(new Handler(null, null));
	}

	/**
	 * Tests whether this handler has completed parsing 
	 * a geometry.
	 * If this is the case, {@link #getGeometry()} can be called
	 * to get the value of the parsed geometry.
	 * 
	 * @return if the parsing of the geometry is complete
	 */
	public boolean isGeometryComplete()
	{
		if (stack.size() > 1)
			return false;
		// top level node on stack needs to have at least one child 
		Handler h = (Handler) stack.peek();
		if (h.children.size() < 1)
			return false;
		return true;
		
	}
	
	/**
	 * Gets the geometry parsed by this handler.
	 * This method should only be called AFTER the parser has completed execution
	 * 
	 * @return the parsed Geometry, or a GeometryCollection if more than one geometry was parsed
	 * @throws IllegalStateException if called before the parse is complete
	 */
	public Geometry getGeometry() {
		if (stack.size() == 1) {
			Handler h = (Handler) stack.peek();
			if (h.children.size() == 1)
				return (Geometry) h.children.get(0);
			return gf.createGeometryCollection(
					(Geometry[]) h.children.toArray(new Geometry[stack.size()]));
		}
		throw new IllegalStateException(
				"Parse did not complete as expected, there are " + stack.size()
						+ " elements on the Stack");
	}

	//////////////////////////////////////////////
	// Parsing Methods

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (!stack.isEmpty())
			((Handler) stack.peek()).addText(new String(ch, start, length));
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if (!stack.isEmpty())
			((Handler) stack.peek()).addText(" ");
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		Handler thisAction = (Handler) stack.pop();
		((Handler) stack.peek()).keep(thisAction.create(gf));
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// create a handler
		ParseStrategy ps = GeometryStrategies.findStrategy(uri, localName);
		if (ps == null) {
			String qn = qName.substring(qName.indexOf(':') + 1, qName.length());
			ps = GeometryStrategies.findStrategy(null, qn);
		}
		Handler h = new Handler(ps, attributes);
		// and add it to the stack
		stack.push(h);
	}

	//////////////////////////////////////////////
	// Logging Methods

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		if (delegate != null && delegate instanceof ContentHandler)
			((ContentHandler) delegate).setDocumentLocator(locator);

	}

	private Locator locator = null;

	protected Locator getDocumentLocator() {
		return locator;
	}

	//////////////////////////////////////////////
	// ERROR Methods

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		if (delegate != null)
			delegate.fatalError(e);
		else
			super.fatalError(e);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException e) throws SAXException {
		if (delegate != null)
			delegate.error(e);
		else
			super.error(e);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		if (delegate != null)
			delegate.warning(e);
		else
			super.warning(e);
	}

}
