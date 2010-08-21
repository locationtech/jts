package com.vividsolutions.jtsexample.io.gml2;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.gml2.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * An example of using the {@link GMLHandler} class
 * to read geometry data out of KML files.
 * 
 * @author mbdavis
 *
 */
public class KMLReaderExample 
{
  public static void main(String[] args)
  throws Exception
  {
  	String filename = "C:\\proj\\JTS\\KML\\usPop-STUS-p06.kml";
  	KMLReader rdr = new KMLReader(filename);
  	rdr.read();
  }
}

class KMLReader
{
	private String filename;
	
	public KMLReader(String filename)
	{
		this.filename = filename;
	}
	
	public void read()
	throws IOException, SAXException
	{
    XMLReader xr; 
    
    xr = XMLReaderFactory.createXMLReader();
    //xr = new org.apache.xerces.parsers.SAXParser();
    KMLHandler kmlHandler = new KMLHandler();
    xr.setContentHandler(kmlHandler);
    xr.setErrorHandler(kmlHandler);
    
    Reader r = new BufferedReader(new FileReader(filename));
    LineNumberReader myReader = new LineNumberReader(r);
    xr.parse(new InputSource(myReader));
    
    List geoms = kmlHandler.getGeometries();
	}
}

class KMLHandler extends DefaultHandler
{
	private List geoms = new ArrayList();;
	
	private GMLHandler currGeomHandler;
	private String lastEltName = null;
	private GeometryFactory fact = new FixingGeometryFactory();
	
	public KMLHandler()
	{
		super();
	}
	
	public List getGeometries()
	{
		return geoms;
	}
	
  /**
   *  SAX handler. Handle state and state transitions based on an element
   *  starting.
   *
   *@param  uri               Description of the Parameter
   *@param  name              Description of the Parameter
   *@param  qName             Description of the Parameter
   *@param  atts              Description of the Parameter
   *@exception  SAXException  Description of the Exception
   */
  public void startElement(String uri, String name, String qName,
			Attributes atts) throws SAXException {
		if (name.equalsIgnoreCase(GMLConstants.GML_POLYGON)) {
			currGeomHandler = new GMLHandler(fact, null);
		}
		if (currGeomHandler != null)
			currGeomHandler.startElement(uri, name, qName, atts);
		if (currGeomHandler == null) {
			lastEltName = name;
			//System.out.println(name);
		}
	}
  
	public void characters(char[] ch, int start, int length) throws SAXException 
	{
    if (currGeomHandler != null) {
    	currGeomHandler.characters(ch, start, length);
    }
    else {
    	String content = new String(ch, start, length).trim();
    	if (content.length() > 0) {
    		System.out.println(lastEltName + "= " + content);
    	}
    }
	}
	
	public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException {
    if (currGeomHandler != null)
    	currGeomHandler.ignorableWhitespace(ch, start, length);
	}
	
  /**
   *  SAX handler - handle state information and transitions based on ending
   *  elements.
   *
   *@param  uri               Description of the Parameter
   *@param  name              Description of the Parameter
   *@param  qName             Description of the Parameter
   *@exception  SAXException  Description of the Exception
   */
  public void endElement(String uri, String name, String qName)
			throws SAXException {
		// System.out.println("/" + name);

		if (currGeomHandler != null) {
			currGeomHandler.endElement(uri, name, qName);

			if (currGeomHandler.isGeometryComplete()) {
				Geometry g = currGeomHandler.getGeometry();
				System.out.println(g);
				geoms.add(g);

				// reset to indicate no longer parsing geometry
				currGeomHandler = null;
			}
		}

	}
}

/**
 * A GeometryFactory extension which fixes structurally bad coordinate sequences
 * used to create LinearRings.
 * 
 * @author mbdavis
 * 
 */
class FixingGeometryFactory extends GeometryFactory
{
	public LinearRing createLinearRing(CoordinateSequence cs)
	{
		if (cs.getCoordinate(0).equals(cs.getCoordinate(cs.size() - 1))) 
			return super.createLinearRing(cs);
			
			// add a new coordinate to close the ring
			CoordinateSequenceFactory csFact = getCoordinateSequenceFactory();
			CoordinateSequence csNew = csFact.create(cs.size() + 1, cs.getDimension());
			CoordinateSequences.copy(cs, 0, csNew, 0, cs.size());
			CoordinateSequences.copyCoord(csNew, 0, csNew, csNew.size() - 1);
			return super.createLinearRing(csNew);
	}

	
}
