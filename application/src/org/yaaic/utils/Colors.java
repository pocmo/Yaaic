package org.yaaic.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;


public class Colors {
    /*
     * Colors from the "Classic" theme in mIRC.
     */
    public static final String[] colors = {
        "#FFFFFF",  // White
        "#000000",  // Black
        "#00007F",  // Blue (navy)
        "#009300",  // Green
        "#FC0000",  // Red
        "#7F0000",  // Brown (maroon)
        "#9C009C",  // Purple
        "#FC7F00",  // Orange (olive)
        "#FFFF00",  // Yellow
        "#00FC00",  // Light Green (lime)
        "#008080",  // Teal (a green/blue cyan)
        "#00FFFF",  // Light Cyan (cyan) (aqua)
        "#0000FF",  // Light Blue (royal)
        "#FF00FF",  // Pink (light purple) (fuchsia)
        "#7F7F7F",  // Grey
        "#D2D2D2"   // Light Grey (silver)
    };

    private static final Pattern boldPattern = Pattern.compile("\\x02([^\\x02\\x0F]*)(?:\\x02|(\\x0F))?");
    private static final Pattern underlinePattern = Pattern.compile("\\x1F([^\\x1F\\x0F]*)(?:\\x1F|(\\x0F))?");
    private static final Pattern italicPattern = Pattern.compile("\\x1D([^\\x1D\\x0F]*)(?:\\x1D|(\\x0F))?");
    private static final Pattern inversePattern = Pattern.compile("\\x16([^\\x16\\x0F]*)(?:\\x16|(\\x0F))?");
    private static final Pattern colorPattern = Pattern.compile("\\x03(\\d{1,2})(?:,(\\d{1,2}))?([^\\x03\\x0F]*)(\\x03|\\x0F)?");
    private static final Pattern cleanupPattern = Pattern.compile("(?:\\x02|\\x1F|\\x1D|\\x0F|\\x16|\\x03(?:(?:\\d{1,2})(?:,\\d{1,2})?)?)");

    private Colors() {}

    /**
     * Converts a string with mIRC color codes to a HTML string.
     * 
     * @param text  A string with mIRC color codes.
     * @return      HTML string.
     */
    public static String mircColorParser(String text) {
        text = replaceControlCodes(boldPattern.matcher(text), "<b>", "</b>");
        text = replaceControlCodes(underlinePattern.matcher(text), "<u>", "</u>");
        text = replaceControlCodes(italicPattern.matcher(text), "<i>", "</i>");
        // Inverse assumes that the background is black and the foreground is white.
        text = replaceControlCodes(inversePattern.matcher(text), "<font bgcolor=\"" + colors[0] + "\" color=\"" + colors[1] + "\">", "</font>");

        StringBuffer sb = new StringBuffer(text);
        StringBuilder ft = new StringBuilder();
        Matcher m = colorPattern.matcher(text);
        while (m.find()) {
            sb.delete(0, sb.length());
            ft.delete(0, ft.length());

            // Build the font tag
            ft.append("<font");
            Integer color = Integer.parseInt(m.group(1));
            if (color <= 15 && color >= 0) {
                ft.append(" color=\""+colors[color]+"\"");
            }
            if (m.group(2) != null) {
                color = Integer.parseInt(m.group(2));
                if (color <= 15 && color >= 0) {
                    ft.append(" bgcolor=\""+colors[color]+"\"");
                }
            }
            ft.append(">");
            ft.append(m.group(3));
            ft.append("</font>");
            if (m.group(4) != null) {
                ft.append(m.group(4));
            }
            m.appendReplacement(sb, ft.toString());
            m.appendTail(sb);
            m.reset(sb.toString());
        }

        // Remove left over codes
        Log.d("html", removeStyleAndColors(sb.toString()));
        return removeStyleAndColors(sb.toString());
    }

    private static String replaceControlCodes(Matcher m, String startTag, String endTag) {
        /*
         * matcher(...).replaceAll("<x>\1\2</x>") inserts "null" if the second
         * capture group isn't found so we'll do it this way instead.
         */
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, startTag+m.group(1)+endTag+(m.group(2) == null ? "" : m.group(2)));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static String removeStyleAndColors(String text) {
        return cleanupPattern.matcher(text).replaceAll("");
    }
}