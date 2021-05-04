package org.locationtech.jts.geom;

/**
 * Internal class which encapsulates the runtime switch to Z-interpolate when applicable.
 * <p>
 * <ul>
 * <li><code>jts.zinterpolating=false</code> - (default) do not Z-interpolate
 * <li><code>jts.zinterpolating=true</code> - Z-interpolate
 * </ul>
 * 
 * @author bjornharrtell
 *
 */
public class ZInterpolating {
    public static String ZINTERPOLATING_PROPERTY_NAME = "jts.zinterpolating";
    public static String ZINTERPOLATING_PROPERTY_VALUE_TRUE = "true";
    public static String ZINTERPOLATING_PROPERTY_VALUE_FALSE = "false";
    public static boolean ZINTERPOLATING_DEFAULT = false;
    private static boolean isZInterpolating = ZINTERPOLATING_DEFAULT;

    static {
        setZInterpolatingImpl(System.getProperty(ZINTERPOLATING_PROPERTY_NAME));
    }

    public static boolean getZInterpolating() {
        return isZInterpolating;
    }

    public static void setZInterpolating(boolean zInterpolating) {
        isZInterpolating = zInterpolating;
    }

    private static void setZInterpolatingImpl(String isZInterpolatingCode) {
        if (isZInterpolatingCode == null)
            return;
            isZInterpolating = ZINTERPOLATING_DEFAULT;
        if (ZINTERPOLATING_PROPERTY_VALUE_TRUE.equalsIgnoreCase(isZInterpolatingCode))
            isZInterpolating = true;
    }
}
