package com.vividsolutions.jtstest.testbuilder;

import javax.swing.ImageIcon;


/**
 * Gets an icon from this class' package.
 */
public class IconLoader {
    public static ImageIcon icon(String filename) {
        return new ImageIcon(IconLoader.class.getResource(filename));
    }
}
