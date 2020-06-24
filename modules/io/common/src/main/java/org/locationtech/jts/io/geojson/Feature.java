package org.locationtech.jts.io.geojson;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;
import java.util.Objects;

/**
 * A Feature in GeoJSON contain a geometry object and additional properties
 * <p>
 * A specification of the GeoJson format can be found at the GeoJson web site:
 * <a href='http://geojson.org/geojson-spec.html'>http://geojson.org/geojson-spec.html</a>.
 * <p>
 */
public class Feature {
    private String id;
    private Geometry geometry;
    private Map<String, Object> properties;

    /**
     * Constructs a Feature instance.
     * @param id the feature id which (can be null)
     * @param geometry the feature geometry
     * @param properties the feature properties which cannot be null
     */
    public Feature(String id, Geometry geometry, Map<String, Object> properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    /**
     * Returns the Feature id
     *
     * @return the feature id string or null
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the {@link Geometry}
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Returns the feature properties
     *
     * @return the property map or null
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Compares the specific object with this feature for equality.
     *
     * @param object is the object to be compared for equality with this feature
     * @return
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Feature)) {
            return false;
        }
        Feature other = (Feature) object;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        if (!geometry.equals(other.geometry)) {
            return false;
        }
        if (!Objects.equals(properties, other.properties)) {
            return false;
        }
        return true;
    }
}
