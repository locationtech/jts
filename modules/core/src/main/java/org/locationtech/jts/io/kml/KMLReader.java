/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.kml;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Constructs a {@link Geometry} object from the OGC KML representation.
 * Works only with KML geometry elements and may also parse attributes within these elements
 */
public class KMLReader {
    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private final GeometryFactory geometryFactory;
    private final Set<String> attributeNames;

    private static final String POINT = "Point";
    private static final String LINESTRING = "LineString";
    private static final String POLYGON = "Polygon";
    private static final String MULTIGEOMETRY = "MultiGeometry";

    private static final String COORDINATES = "coordinates";
    private static final String OUTER_BOUNDARY_IS = "outerBoundaryIs";
    private static final String INNER_BOUNDARY_IS = "innerBoundaryIs";

    private static final String NO_ELEMENT_ERROR = "No element %s found in %s";

    /**
     * Creates a reader that creates objects using the default {@link GeometryFactory}.
     */
    public KMLReader() {
        this(new GeometryFactory(), Collections.emptyList());
    }

    /**
     * Creates a reader that creates objects using the given
     * {@link GeometryFactory}.
     *
     * @param geometryFactory the factory used to create <code>Geometry</code>s.
     */
    public KMLReader(GeometryFactory geometryFactory) {
        this(geometryFactory, Collections.emptyList());
    }

    /**
     * Creates a reader that creates objects using the default {@link GeometryFactory}.
     *
     * @param attributeNames names of attributes that should be parsed (i.e. extrude, altitudeMode, tesselate, etc).
     */
    public KMLReader(Collection<String> attributeNames) {
        this(new GeometryFactory(), attributeNames);
    }

    /**
     * Creates a reader that creates objects using the given
     * {@link GeometryFactory}.
     *
     * @param geometryFactory the factory used to create <code>Geometry</code>s.
     * @param attributeNames  names of attributes that should be parsed (i.e. extrude, altitudeMode, tesselate, etc).
     */
    public KMLReader(GeometryFactory geometryFactory, Collection<String> attributeNames) {
        this.geometryFactory = geometryFactory;
        this.attributeNames = attributeNames == null
                ? Collections.emptySet()
                : new HashSet<>(attributeNames);
    }

    /**
     * Reads a KML representation of a {@link Geometry} from a {@link String}.
     * If any attribute names were specified during {@link KMLReader} construction,
     * they will be stored as {@link Map} in {@link Geometry#setUserData(Object)}
     *
     * @param kmlGeometryString string that specifies kml representation of geometry
     * @return a <code>Geometry</code> specified by <code>kmlGeometryString</code>
     * @throws ParseException if a parsing problem occurs
     */
    public Geometry read(String kmlGeometryString) throws ParseException {
        try (StringReader sr = new StringReader(kmlGeometryString)) {
            XMLStreamReader xmlSr = inputFactory.createXMLStreamReader(sr);
            return parseKML(xmlSr);
        } catch (XMLStreamException e) {
            throw new ParseException(e);
        }
    }

    private Coordinate[] parseKMLCoordinates(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        String coordinates = xmlStreamReader.getElementText();

        if (coordinates.isEmpty()) {
            raiseParseError("Empty coordinates");
        }

        double[] parsedOrdinates = {Double.NaN, Double.NaN, Double.NaN};
        List<Coordinate> coordinateList = new ArrayList();

        int spaceIdx = coordinates.indexOf(' ');
        int currentIdx = 0;

        while (currentIdx < coordinates.length()) {
            if (spaceIdx == -1) {
                spaceIdx = coordinates.length();
            }

            String coordinate = coordinates.substring(currentIdx, spaceIdx);

            int yOrdinateComma = coordinate.indexOf(',');
            if (yOrdinateComma == -1 || yOrdinateComma == coordinate.length() - 1 || yOrdinateComma == 0) {
                raiseParseError("Invalid coordinate format");
            }

            parsedOrdinates[0] = Double.parseDouble(coordinate.substring(0, yOrdinateComma));

            int zOrdinateComma = coordinate.indexOf(',', yOrdinateComma + 1);
            if (zOrdinateComma == -1) {
                parsedOrdinates[1] = Double.parseDouble(coordinate.substring(yOrdinateComma + 1));
            } else {
                parsedOrdinates[1] = Double.parseDouble(coordinate.substring(yOrdinateComma + 1, zOrdinateComma));
                parsedOrdinates[2] = Double.parseDouble(coordinate.substring(zOrdinateComma + 1));
            }

            Coordinate crd = new Coordinate(parsedOrdinates[0], parsedOrdinates[1], parsedOrdinates[2]);
            geometryFactory.getPrecisionModel().makePrecise(crd);

            coordinateList.add(crd);
            currentIdx = spaceIdx + 1;
            spaceIdx = coordinates.indexOf(' ', currentIdx);
            parsedOrdinates[0] = parsedOrdinates[1] = parsedOrdinates[2] = Double.NaN;
        }

        return coordinateList.toArray(new Coordinate[]{});
    }

    private KMLCoordinatesAndAttributes parseKMLCoordinatesAndAttributes(XMLStreamReader xmlStreamReader, String objectNodeName) throws XMLStreamException, ParseException {
        Coordinate[] coordinates = null;
        Map<String, String> attributes = null;

        while (xmlStreamReader.hasNext() && !(xmlStreamReader.isEndElement() && xmlStreamReader.getLocalName().equals(objectNodeName))) {
            if (xmlStreamReader.isStartElement()) {
                String elementName = xmlStreamReader.getLocalName();

                if (elementName.equals(COORDINATES)) {
                    coordinates = parseKMLCoordinates(xmlStreamReader);
                } else if (attributeNames.contains(elementName)) {
                    if (attributes == null) {
                        attributes = new HashMap<>();
                    }

                    attributes.put(elementName, xmlStreamReader.getElementText());
                }
            }

            xmlStreamReader.next();
        }

        if (coordinates == null) {
            raiseParseError(NO_ELEMENT_ERROR, COORDINATES, objectNodeName);
        }

        return new KMLCoordinatesAndAttributes(coordinates, attributes);
    }

    private Geometry parseKMLPoint(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        KMLCoordinatesAndAttributes kmlCoordinatesAndAttributes = parseKMLCoordinatesAndAttributes(xmlStreamReader, POINT);

        Point point = geometryFactory.createPoint(kmlCoordinatesAndAttributes.coordinates[0]);
        point.setUserData(kmlCoordinatesAndAttributes.attributes);

        return point;
    }

    private Geometry parseKMLLineString(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        KMLCoordinatesAndAttributes kmlCoordinatesAndAttributes = parseKMLCoordinatesAndAttributes(xmlStreamReader, LINESTRING);

        LineString lineString = geometryFactory.createLineString(kmlCoordinatesAndAttributes.coordinates);
        lineString.setUserData(kmlCoordinatesAndAttributes.attributes);

        return lineString;
    }

    private Geometry parseKMLPolygon(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        LinearRing shell = null;
        ArrayList<LinearRing> holes = null;
        Map<String, String> attributes = null;

        while (xmlStreamReader.hasNext() && !(xmlStreamReader.isEndElement() && xmlStreamReader.getLocalName().equals(POLYGON))) {
            if (xmlStreamReader.isStartElement()) {
                String elementName = xmlStreamReader.getLocalName();

                if (elementName.equals(OUTER_BOUNDARY_IS)) {
                    moveToElement(xmlStreamReader, COORDINATES, OUTER_BOUNDARY_IS);
                    shell = geometryFactory.createLinearRing(parseKMLCoordinates(xmlStreamReader));
                } else if (elementName.equals(INNER_BOUNDARY_IS)) {
                    moveToElement(xmlStreamReader, COORDINATES, INNER_BOUNDARY_IS);

                    if (holes == null) {
                        holes = new ArrayList<>();
                    }
                    holes.add(geometryFactory.createLinearRing(parseKMLCoordinates(xmlStreamReader)));
                } else if (attributeNames.contains(elementName)) {
                    if (attributes == null) {
                        attributes = new HashMap<>();
                    }

                    attributes.put(elementName, xmlStreamReader.getElementText());
                }
            }

            xmlStreamReader.next();
        }

        if (shell == null) {
            raiseParseError("No outer boundary for Polygon");
        }

        Polygon polygon = geometryFactory.createPolygon(shell, holes == null ? null : holes.toArray(new LinearRing[]{}));
        polygon.setUserData(attributes);

        return polygon;
    }

    private Geometry parseKMLMultiGeometry(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        List<Geometry> geometries = new ArrayList<>();
        String firstParsedType = null;
        boolean allTypesAreSame = true;

        while (xmlStreamReader.hasNext()) {
            if (xmlStreamReader.isStartElement()) {
                String elementName = xmlStreamReader.getLocalName();
                switch (elementName) {
                    case POINT:
                    case LINESTRING:
                    case POLYGON:
                    case MULTIGEOMETRY:
                        Geometry geometry = parseKML(xmlStreamReader);

                        if (firstParsedType == null) {
                            firstParsedType = geometry.getGeometryType();
                        } else if (!firstParsedType.equals(geometry.getGeometryType())) {
                            allTypesAreSame = false;
                        }

                        geometries.add(geometry);
                }
            }

            xmlStreamReader.next();
        }

        if (geometries.isEmpty()) {
            return null;
        }

        if (geometries.size() == 1) {
            return geometries.get(0);
        }

        if (allTypesAreSame) {
            switch (firstParsedType) {
                case POINT:
                    return geometryFactory.createMultiPoint(prepareTypedArray(geometries, Point.class));
                case LINESTRING:
                    return geometryFactory.createMultiLineString(prepareTypedArray(geometries, LineString.class));
                case POLYGON:
                    return geometryFactory.createMultiPolygon(prepareTypedArray(geometries, Polygon.class));
                default:
                    return geometryFactory.createGeometryCollection(geometries.toArray(new Geometry[]{}));
            }
        } else {
            return geometryFactory.createGeometryCollection(geometries.toArray(new Geometry[]{}));
        }
    }

    private Geometry parseKML(XMLStreamReader xmlStreamReader) throws XMLStreamException, ParseException {
        boolean hasElement = false;

        while (xmlStreamReader.hasNext()) {
            if (xmlStreamReader.isStartElement()) {
                hasElement = true;
                break;
            }

            xmlStreamReader.next();
        }

        if (!hasElement) {
            raiseParseError("Invalid KML format");
        }

        String elementName = xmlStreamReader.getLocalName();
        switch (elementName) {
            case POINT:
                return parseKMLPoint(xmlStreamReader);
            case LINESTRING:
                return parseKMLLineString(xmlStreamReader);
            case POLYGON:
                return parseKMLPolygon(xmlStreamReader);
            case MULTIGEOMETRY:
                xmlStreamReader.next();
                return parseKMLMultiGeometry(xmlStreamReader);
        }

        raiseParseError("Unknown KML geometry type %s", elementName);
        return null;
    }

    private void moveToElement(XMLStreamReader xmlStreamReader, String elementName, String endElementName) throws XMLStreamException, ParseException {
        boolean elementFound = false;

        while (xmlStreamReader.hasNext() && !(xmlStreamReader.isEndElement() && xmlStreamReader.getLocalName().equals(endElementName))) {
            if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals(elementName)) {
                elementFound = true;
                break;
            }

            xmlStreamReader.next();
        }

        if (!elementFound) {
            raiseParseError(NO_ELEMENT_ERROR, elementName, endElementName);
        }
    }

    private void raiseParseError(String template, Object... parameters) throws ParseException {
        throw new ParseException(String.format(template, parameters));
    }

    private <T> T[] prepareTypedArray(List<Geometry> geometryList, Class<T> geomClass) {
        return geometryList.toArray((T[]) Array.newInstance(geomClass, geometryList.size()));
    }

    private static class KMLCoordinatesAndAttributes {
        private final Coordinate[] coordinates;
        private final Map<String, String> attributes;

        public KMLCoordinatesAndAttributes(Coordinate[] coordinates, Map<String, String> attributes) {

            this.coordinates = coordinates;
            this.attributes = attributes;
        }
    }
}
