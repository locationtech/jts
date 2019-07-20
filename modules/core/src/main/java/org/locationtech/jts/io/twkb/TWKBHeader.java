package org.locationtech.jts.io.twkb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.twkb.TWKBIO.TWKBOutputStream;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

/**
 * <pre>
 * {@code
 * 
 * twkb                    := <header> <geometry_body>
 * header                  := <type_and_precision> <metadata_header> [extended_dimensions_header] [geometry_body_size]
 * type_and_precision      := byte := <type_mask OR precision>)
 * type_mask               := <ubyte> (0b0000XXXX -> 1=point, 2=linestring, 3=polygon, 4=multipoint, 
 *                                     5=multilinestring, 6=multipolygon, 7=geometry collection)
 * precision               := <signed byte> (zig-zag encoded 4-bit signed integer, 0bXXXX0000. Number of base-10 decimal places 
 *                                           stored. A positive retaining information to the right of the decimal place, negative 
 *                                           rounding up to the left of the decimal place)  
 * metadata_header := byte := <bbox_flag OR  size_flag OR idlist_flag OR extended_precision_flag OR empty_geometry_flag>
 * bbox_flag               := 0b00000001
 * size_flag               := 0b00000010
 * idlist_flag             := 0b00000100
 * extended_precision_flag := 0b00001000
 * empty_geometry_flag     := 0b00010000
 * 
 * # extended_dimensions_header present iif extended_precision_flag == 1
 * extended_dimensions_header  := byte := <Z_dimension_presence_flag OR M_dimension_presence_flag OR Z_precision OR M_precision>
 * Z_dimension_presence_flag   := 0b00000001 
 * M_dimension_presence_flag   := 0b00000010
 * Z_precision                 := 0b000XXX00 3-bit unsigned integer using bits 3-5 
 * M_precision                 := 0bXXX00000 3-bit unsigned integer using bits 6-8
 * 
 * # geometry_body_size present iif size_flag == 1 
 * geometry_body_size := uint32 # size in bytes of <geometry_body>
 * 
 * # geometry_body present iif empty_geometry_flag == 0
 * geometry_body := [bounds] [idlist] <geometry>
 * # bounds present iff bbox_flag == 1 
 * # 2 signed varints per dimension. i.e.:
 * # [xmin, deltax, ymin, deltay]                              iif Z_dimension_presence_flag == 0 AND M_dimension_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz]                iif Z_dimension_presence_flag == 1 AND M_dimension_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz, mmin, deltam]  iif Z_dimension_presence_flag == 1 AND M_dimension_presence_flag == 1
 * # [xmin, deltax, ymin, deltay, mmin, deltam]                iif Z_dimension_presence_flag == 0 AND M_dimension_presence_flag == 1
 * bounds          := sint32[4] | sint32[6] | sint32[8] 
 * geometry        := point | linestring | polygon | multipoint | multilinestring | multipolygon | geomcollection
 * point           := sint32[dimension]
 * linestring      := <npoints:uint32> [point[npoints]]
 * polygon         := <nrings:uint32> [linestring]
 * multipoint      := <nmembers:uint32> [idlist:<sint32[nmembers]>] [point[nmembers]]
 * multilinestring := <nmembers:uint32> [idlist:<sint32[nmembers]>] [linestring[nmembers]]
 * multipolygon    := <nmembers:uint32> [idlist:<sint32[nmembers]>] [polygon[nmembers]]
 * geomcollection  := <nmembers:uint32> [idlist:<sint32[nmembers]>] [twkb[nmembers]]
 * 
 * uint32 := <Unsigned variable-length encoded integer>
 * sint32 := <Signed variable-length, zig-zag encoded integer>
 * byte := <Single octect>
 * 
 * }
 * </pre>
 */
@Value
@Wither
@Builder
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
class TWKBHeader {

    static enum GeometryType {
        POINT(1, GeometryFactory::createPoint), //
        LINESTRING(2, GeometryFactory::createLineString), //
        POLYGON(3, GeometryFactory::createPolygon), //
        MULTIPOINT(4, GeometryFactory::createMultiPoint), //
        MULTILINESTRING(5, GeometryFactory::createMultiLineString), //
        MULTIPOLYGON(6, GeometryFactory::createMultiPolygon), //
        GEOMETRYCOLLECTION(7, GeometryFactory::createGeometryCollection);

        private int value;

        private Function<GeometryFactory, Geometry> emptyBuilder;

        GeometryType(int value, Function<GeometryFactory, Geometry> emptyBuilder) {
            this.value = value;
            this.emptyBuilder = emptyBuilder;
        }

        public int getValue() {
            return value;
        }

        // held as a class variable cause calling values() on a tight loop has a non-depreciable
        // performance impact
        private static final GeometryType[] VALUES = GeometryType.values();

        public static GeometryType valueOf(int value) {
            return VALUES[value - 1];
        }

        public static GeometryType valueOf(Class<? extends Geometry> gclass) {
            Objects.requireNonNull(gclass);
            if (Point.class.isAssignableFrom(gclass))
                return POINT;
            if (LineString.class.isAssignableFrom(gclass))
                return LINESTRING;
            if (Polygon.class.isAssignableFrom(gclass))
                return POLYGON;
            if (MultiPoint.class.isAssignableFrom(gclass))
                return MULTIPOINT;
            if (MultiLineString.class.isAssignableFrom(gclass))
                return MULTILINESTRING;
            if (MultiPolygon.class.isAssignableFrom(gclass))
                return MULTIPOLYGON;
            if (GeometryCollection.class.isAssignableFrom(gclass))
                return GEOMETRYCOLLECTION;

            throw new IllegalArgumentException("Unrecognized geometry tpye: " + gclass);
        }

        public Geometry createEmpty(GeometryFactory factory) {
            return this.emptyBuilder.apply(factory);
        }
    }

    // first 1-byte header //
    private GeometryType geometryType;

    private @Builder.Default int xyPrecision = 0;

    // metadata_header := byte
    // bbox_flag := 0b00000001
    // size_flag := 0b00000010
    // idlist_flag := 0b00000100
    // extended_precision_flag := 0b00001000
    // empty_geometry_flag := 0b00010000
    private @Builder.Default boolean hasBBOX = false;

    private @Builder.Default boolean hasSize = false;

    private @Builder.Default boolean hasIdList = false;

    // private @Builder.Default boolean hasExtendedPrecision = false;
    public boolean hasExtendedPrecision() {
        return hasZ() || hasM();
    }

    private @Builder.Default boolean isEmpty = false;

    // extended_dimensions_header present iif extended_precision_flag == 1
    // extended_dimensions_header := byte
    // Z_dimension_presence_flag := 0b00000001
    // M_dimension_presence_flag := 0b00000010
    // Z_precision := 0b000XXX00 3-bit unsigned integer using bits 3-5
    // M_precision := 0bXXX00000 3-bit unsigned integer using bits 6-8

    private @Builder.Default boolean hasZ = false;

    private @Builder.Default boolean hasM = false;

    private @Builder.Default int zPrecision = 0;

    private @Builder.Default int mPrecision = 0;

    /**
     * Size of encoded geometry body, iif size_flag == 1, defaults to {@code -1} if {@link #hasSize}
     * ({@code == false}
     * <p>
     * {@code geometry_body_size := uint32 # size in bytes of <geometry_body>}
     */
    private @Getter int geometryBodySize;

    /////////////////////// custom optimizations ///////////////////////

    // See TWKBWriter.setOptimizedEncoding for a description of this flag
    private @Builder.Default boolean optimizedEncoding = true;

    public int getDimensions() {
        return 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
    }

    public int getPrecision(final int dimensionIndex) {
        switch (dimensionIndex) {
        case 0:
        case 1:
            return xyPrecision;
        case 2:
            if (!(hasZ || hasM)) {
                throw new IllegalArgumentException("Geometry only has XY dimensions.");
            }
            return hasZ ? zPrecision : mPrecision;
        case 3:
            if (!(hasZ && hasM)) {
                throw new IllegalArgumentException("Geometry has no M dimension.");
            }
            return mPrecision;
        default:
            throw new IllegalArgumentException(
                    "Dimension index shall be between 0 and 3: " + dimensionIndex);
        }
    }

    public static TWKBHeader read(DataInput in) throws IOException {
        Objects.requireNonNull(in);
        // first 1-byte header //
        final int typeAndPrecisionHeader = in.readByte() & 0xFF;
        final int geometryTypeCode = typeAndPrecisionHeader & 0b00001111;
        final GeometryType geometryType = GeometryType.valueOf(geometryTypeCode);
        final int precision = Varint.zigzagDecode((typeAndPrecisionHeader & 0b11110000) >> 4);
        // metadata_header := byte
        // bbox_flag := 0b00000001
        // size_flag := 0b00000010
        // idlist_flag := 0b00000100
        // extended_precision_flag := 0b00001000
        // empty_geometry_flag := 0b00010000
        final int metadata_header = in.readByte() & 0xFF;
        final boolean hasBBOX = (metadata_header & 0b00000001) > 0;
        final boolean hasSize = (metadata_header & 0b00000010) > 0;
        final boolean hasIdList = (metadata_header & 0b00000100) > 0;
        final boolean hasExtendedPrecision = (metadata_header & 0b00001000) > 0;
        final boolean isEmpty = (metadata_header & 0b00010000) > 0;

        // extended_dimensions_header present iif extended_precision_flag == 1
        // extended_dimensions_header := byte
        // Z_dimension_presence_flag := 0b00000001
        // M_dimension_presence_flag := 0b00000010
        // Z_precision := 0b000XXX00 3-bit unsigned integer using bits 3-5
        // M_precision := 0bXXX00000 3-bit unsigned integer using bits 6-8
        boolean hasZ = false;
        boolean hasM = false;
        int zprecision = 0;
        int mprecision = 0;
        if (hasExtendedPrecision) {
            final int extendedDimsHeader = in.readByte() & 0xFF;
            hasZ = (extendedDimsHeader & 0b00000001) > 0;
            hasM = (extendedDimsHeader & 0b00000010) > 0;
            zprecision = (extendedDimsHeader & 0b00011100) >> 2;
            mprecision = (extendedDimsHeader & 0b11100000) >> 5;
        }

        // # geometry_body_size present iif size_flag == 1
        // geometry_body_size := uint32 # size in bytes of <geometry_body>
        int geometryBodySize = -1;
        if (hasSize) {
            geometryBodySize = Varint.readUnsignedVarInt(in);
        }
        return TWKBHeader.builder()//
                .geometryType(geometryType)//
                .xyPrecision(precision)//
                .hasZ(hasZ).zPrecision(zprecision)//
                .hasM(hasM).mPrecision(mprecision)//
                .hasIdList(hasIdList)//
                .isEmpty(isEmpty)//
                .hasSize(hasSize)//
                .hasBBOX(hasBBOX)//
                .geometryBodySize(geometryBodySize)//
                .build();
    }

    public void writeTo(DataOutput out) throws IOException {
        writeTo(TWKBOutputStream.of(out));
    }

    void writeTo(TWKBOutputStream out) throws IOException {
        Objects.requireNonNull(out);
        final int typeAndPrecisionHeader;
        final int metadataHeader;
        {
            final int geometryType = this.geometryType.getValue();
            final int precisionHeader = Varint.zigZagEncode(this.xyPrecision) << 4;
            typeAndPrecisionHeader = precisionHeader | geometryType;

            metadataHeader = (hasBBOX ? 0b00000001 : 0) //
                    | (hasSize ? 0b00000010 : 0)//
                    | (hasIdList ? 0b00000100 : 0)//
                    | (hasExtendedPrecision() ? 0b00001000 : 0)//
                    | (isEmpty ? 0b00010000 : 0);
        }
        out.writeByte(typeAndPrecisionHeader);
        out.writeByte(metadataHeader);
        if (hasExtendedPrecision()) {
            // final int extendedDimsHeader = in.readByte() & 0xFF;
            // hasZ = (extendedDimsHeader & 0b00000001) > 0;
            // hasM = (extendedDimsHeader & 0b00000010) > 0;
            // zprecision = (extendedDimsHeader & 0b00011100) >> 2;
            // mprecision = (extendedDimsHeader & 0b11100000) >> 5;

            int extendedDimsHeader = (hasZ ? 0b00000001 : 0) | (hasM ? 0b00000010 : 0);
            extendedDimsHeader |= zPrecision << 2;
            extendedDimsHeader |= mPrecision << 5;

            out.writeByte(extendedDimsHeader);
        }
        if (hasSize) {
            out.writeUnsignedVarInt(this.geometryBodySize);
        }
    }

}