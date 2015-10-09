/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2015 Sebastian Kaspari

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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Emojis {
    private static final HashMap<String, String> mappings = new HashMap<>();
    private static Pattern pattern;

    static {
        mappings.put(":)", "\uD83D\uDE03");
        mappings.put(":-)", "\uD83D\uDE03");
        mappings.put(":(", "\uD83D\uDE1E");
        mappings.put(":-(", "\uD83D\uDE1E");
        mappings.put(";)", "\uD83D\uDE09");
        mappings.put(";-)", "\uD83D\uDE09");
        mappings.put(":p", "\uD83D\uDE1B");
        mappings.put(":-p", "\uD83D\uDE1B");
        mappings.put(":P", "\uD83D\uDE1B");
        mappings.put(":-P", "\uD83D\uDE1B");
        mappings.put(":D", "\uD83D\uDE04");
        mappings.put(":-D", "\uD83D\uDE04");
        mappings.put(":[", "\uD83D\uDE12");
        mappings.put(":-[", "\uD83D\uDE12");
        mappings.put(":\\", "\uD83D\uDE14");
        mappings.put(":-\\", "\uD83D\uDE14");
        mappings.put(":o", "\uD83D\uDE2E");
        mappings.put(":-o", "\uD83D\uDE2E");
        mappings.put(":O", "\uD83D\uDE32");
        mappings.put(":-O", "\uD83D\uDE32");
        mappings.put(":*", "\uD83D\uDE18");
        mappings.put(":-*", "\uD83D\uDE18");
        mappings.put("8)", "\uD83D\uDE0E");
        mappings.put("8-)", "\uD83D\uDE0E");
        mappings.put(":'(", "\uD83D\uDE22");
        mappings.put(":'-(", "\uD83D\uDE22");
        mappings.put(":X", "\uD83D\uDE2F");
        mappings.put(":-X", "\uD83D\uDE2F");

        StringBuilder regex = new StringBuilder("(");

        for (String emoji : mappings.keySet()) {
            regex.append(Pattern.quote(emoji));
            regex.append("|");
        }

        regex.deleteCharAt(regex.length() - 1);
        regex.append(")");

        pattern = Pattern.compile(regex.toString());
    }

    /**
     * Replace text smileys like :) with Emojis.
     */
    public static String convert(String text) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, mappings.get(matcher.group(1)));
        }

        matcher.appendTail(buffer);

        return buffer.toString();
    }
}
