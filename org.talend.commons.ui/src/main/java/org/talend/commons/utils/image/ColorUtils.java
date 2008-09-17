// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.utils.image;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * ggu class global comment. Detailled comment
 */
public class ColorUtils {

    private static Color DEFAULT_COLOR = new Color(Display.getDefault(), 0, 0, 0);// white

    private final static String SEMICOLON = ";"; //$NON-NLS-1$

    private final static String COMMA = ","; //$NON-NLS-1$

    /*
     * for tPostjob/tPrejob components
     */
    // public static final Color SPECIAL_SUBJOB_TITLE_COLOR = new Color(null, 230, 100, 0);
    //
    // public final static Color SPECIAL_SUBJOB_COLOR = new Color(null, 255, 220, 180);
    /**
     * return such as "255;255;255"
     */
    public static String getColorValue(Color color) {
        if (color == null) {
            color = DEFAULT_COLOR;
        }
        StringBuffer sb = new StringBuffer();

        sb.append(color.getRed());
        sb.append(SEMICOLON);
        sb.append(color.getGreen());
        sb.append(SEMICOLON);
        sb.append(color.getBlue());

        return sb.toString();
    }

    /**
     * return such as "255;255;255"
     */
    public static String getColorValue(RGB rgb) {
        if (rgb == null) {
            rgb = DEFAULT_COLOR.getRGB();
        }
        StringBuffer sb = new StringBuffer();

        sb.append(rgb.red);
        sb.append(SEMICOLON);
        sb.append(rgb.green);
        sb.append(SEMICOLON);
        sb.append(rgb.blue);

        return sb.toString();
    }

    public static Color parseStringToColor(String color) {
        return parseStringToColor(color, null);
    }

    /**
     * 
     * ggu Comment method "parseStringToColor".
     * 
     * can parse the "255,255,255" and "255;255;255".
     */
    public static Color parseStringToColor(String color, Color defaultColor) {
        if (defaultColor == null) {
            defaultColor = DEFAULT_COLOR;
        }
        if (color == null) {
            return defaultColor;
        }
        if (color.contains(COMMA)) {
            RGB rgb = StringConverter.asRGB(color, defaultColor.getRGB());
            return new Color(null, rgb);
        }
        if (color.contains(SEMICOLON)) {
            try {
                StringTokenizer stok = new StringTokenizer(color, SEMICOLON);
                String red = stok.nextToken().trim();
                String green = stok.nextToken().trim();
                String blue = stok.nextToken().trim();

                int rval = 0, gval = 0, bval = 0;

                rval = Integer.parseInt(red);
                gval = Integer.parseInt(green);
                bval = Integer.parseInt(blue);

                return new Color(null, rval, gval, bval);
            } catch (NumberFormatException e) {
                return defaultColor;
            } catch (NoSuchElementException e) {
                return defaultColor;
            }
        }
        return defaultColor;
    }

    public static RGB parserStringToRGB(String color) {
        return parseStringToColor(color).getRGB();
    }

    /**
     * 
     * ggu Comment method "transform".
     * 
     * "255,255,255" (such as preference value) transform to "255;255;255"
     */
    public static String transform(String color) {
        if (color == null) {
            return null;
        }
        return color.replaceAll(COMMA, SEMICOLON);
    }
}
