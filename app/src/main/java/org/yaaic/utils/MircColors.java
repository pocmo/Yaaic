/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.yaaic.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

/**
 * Class for parsing and handling mIRC colors in text messages.
 * 
 * @author Liato
 */
public abstract class MircColors
{
    /*
     * Colors from the "Classic" theme in mIRC.
     */
    private static final int[] colors = {
        0xFFFFFF,  // White
        0x000000,  // Black
        0x00007F,  // Blue (navy)
        0x009300,  // Green
        0xFC0000,  // Red
        0x7F0000,  // Brown (maroon)
        0x9C009C,  // Purple
        0xFC7F00,  // Orange (olive)
        0xFFFF00,  // Yellow
        0x00FC00,  // Light Green (lime)
        0x008080,  // Teal (a green/blue cyan)
        0x00FFFF,  // Light Cyan (cyan) (aqua)
        0x0000FF,  // Light Blue (royal)
        0xFF00FF,  // Pink (light purple) (fuchsia)
        0x7F7F7F,  // Grey
        0xD2D2D2   // Light Grey (silver)
    };

    private static final Pattern boldPattern = Pattern.compile("\\x02([^\\x02\\x0F]*)(\\x02|(\\x0F))?");
    private static final Pattern underlinePattern = Pattern.compile("\\x1F([^\\x1F\\x0F]*)(\\x1F|(\\x0F))?");
    private static final Pattern italicPattern = Pattern.compile("\\x1D([^\\x1D\\x0F]*)(\\x1D|(\\x0F))?");
    private static final Pattern inversePattern = Pattern.compile("\\x16([^\\x16\\x0F]*)(\\x16|(\\x0F))?");
    private static final Pattern colorPattern = Pattern.compile("\\x03(\\d{1,2})(?:,(\\d{1,2}))?([^\\x03\\x0F]*)(\\x03|\\x0F)?");
    private static final Pattern cleanupPattern = Pattern.compile("(?:\\x02|\\x1F|\\x1D|\\x0F|\\x16|\\x03(?:(?:\\d{1,2})(?:,\\d{1,2})?)?)");

    /**
     * Converts a string with mIRC style and color codes to a SpannableString with
     * all the style and color codes applied.
     * 
     * @param text  A string with mIRC color codes.
     * @return      A SpannableString with all the styles applied.
     */
    public static SpannableString toSpannable(SpannableString text)
    {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        replaceControlCodes(boldPattern.matcher(ssb), ssb, new StyleSpan(Typeface.BOLD));
        replaceControlCodes(underlinePattern.matcher(ssb), ssb, new UnderlineSpan());
        replaceControlCodes(italicPattern.matcher(ssb), ssb, new StyleSpan(Typeface.ITALIC));

        /*
         * Inverse assumes that the background is black and the foreground is white.
         * We apply the background color first and then apply the foreground color
         * to all the parts where BackgroundColorSpans are found.
         */
        replaceControlCodes(inversePattern.matcher(ssb), ssb, new BackgroundColorSpan(colors[0] | 0xFF000000));
        BackgroundColorSpan[] inverseSpans = ssb.getSpans(0, ssb.length(), BackgroundColorSpan.class);
        for (int i = 0; i < inverseSpans.length; i++) {
            ssb.setSpan(new ForegroundColorSpan(colors[1] | 0xFF000000), ssb.getSpanStart(inverseSpans[i]),ssb.getSpanEnd(inverseSpans[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher m = colorPattern.matcher(ssb);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            Integer color = Integer.parseInt(m.group(1));
            int codelength = m.group(1).length()+1;

            if (color <= 15 && color >= 0) {
                ssb.setSpan(new ForegroundColorSpan(colors[color] | 0xFF000000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (m.group(2) != null) {
                color = Integer.parseInt(m.group(2));
                if (color <= 15 && color >= 0) {
                    ssb.setSpan(new BackgroundColorSpan(colors[color] | 0xFF000000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                codelength = codelength + m.group(2).length() + 1;
            }

            ssb.delete(start, start+codelength);
            // Reset the matcher with the modified text so that the ending color code character can be matched again.
            m.reset(ssb);
        }
        // Remove left over codes
        return new SpannableString(removeStyleAndColors(ssb));
    }

    /**
     * Converts a string with mIRC style and color codes to a SpannableString with
     * all the style and color codes applied.
     * 
     * @param text  A string with mIRC color codes.
     * @return      A SpannableString with all the styles applied.
     */
    public static SpannableString toSpannable(String text)
    {
        return toSpannable(new SpannableString(text));
    }

    /**
     * Replace the control codes
     * 
     * @param m
     * @param ssb
     * @param style
     */
    private static void replaceControlCodes(Matcher m, SpannableStringBuilder ssb, CharacterStyle style)
    {
        ArrayList<Integer> toremove = new ArrayList<Integer>();

        while (m.find()) {
            toremove.add(0, m.start());
            // Remove the ending control character unless it's \x0F
            if (m.group(2) != null && m.group(2) != m.group(3)) {
                toremove.add(0, m.end()-1);
            }
            ssb.setSpan(style, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Integer i : toremove) {
            ssb.delete(i, i+1);
        }
    }

    /**
     * Removes mIRC color and style codes and returns the message without them.
     * 
     * @param text  A message with mirc colors and styles.
     * @return      The same message with all the colors and styles removed.
     */
    public static String removeStyleAndColors(String text)
    {
        return cleanupPattern.matcher(text).replaceAll("");
    }

    /**
     * Removes mIRC color and style codes and returns the message without them.
     * 
     * @param text  A message with mirc colors and styles.
     * @return      The same message with all the colors and styles removed.
     */
    public static SpannableStringBuilder removeStyleAndColors(SpannableStringBuilder text)
    {
        ArrayList<int[]> toremove = new ArrayList<int[]>();
        Matcher m = cleanupPattern.matcher(text);
        while (m.find()) {
            toremove.add(0, new int[] {m.start(), m.end()});
        }
        for (int[] i : toremove) {
            text.delete(i[0], i[1]);
        }
        return text;
    }
}
