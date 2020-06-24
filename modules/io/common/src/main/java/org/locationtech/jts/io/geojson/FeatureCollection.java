package org.locationtech.jts.io.geojson;

import java.util.ArrayList;

/**
 * A FeatureCollection in GeoJSON contain a collection of zero or more features.
 * <p>
 * A specification of the GeoJson format can be found at the GeoJson web site:
 * <a href='http://geojson.org/geojson-spec.html'>http://geojson.org/geojson-spec.html</a>.
 * <p>
 */
public class FeatureCollection extends ArrayList<Feature> {

    /**
     * Constructs a feature collection of the provided features.
     *
     * @param features
     */
    public FeatureCollection(Feature ...features) {
        for (Feature feature: features) {
            add(feature);
        }
    }
}
