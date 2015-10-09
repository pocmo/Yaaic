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
package org.yaaic.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import org.yaaic.utils.Emojis;
import org.yaaic.utils.MircColors;

import java.util.Date;

/**
 * A channel or server message
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Message
{
    public static final int COLOR_GREEN   = 0xFF4caf50;
    public static final int COLOR_RED     = 0xFFf44336;
    public static final int COLOR_BLUE    = 0xFF3f51b5;
    public static final int COLOR_YELLOW  = 0xFFffc107;
    public static final int COLOR_GREY    = 0xFF607d8b;
    public static final int COLOR_DEFAULT = 0xFF212121;

    /* normal message, this is the default */
    public static final int TYPE_MESSAGE = 0;

    /* join, part or quit */
    public static final int TYPE_MISC    = 1;

    /* Some are light versions because dark colors hardly readable on
     * Yaaic's dark background */
    private static final int[] colors = {
            0xFFf44336, // Red
            0xFFe91e63, // Pink
            0xFF9c27b0, // Purple
            0xFF673ab7, // Deep Purple
            0xFF3f51b5, // Indigo
            0xFF2196f3, // Blue
            0xFF03a9f4, // Light Blue
            0xFF00bcd4, // Cyan
            0xFF009688, // Teal
            0xFF4caf50, // Green
            0xFF8bc34a, // Light green
            0xFFcddc39, // Lime
            0xFFffeb3b, // Yellow
            0xFFffc107, // Amber
            0xFFff9800, // Orange
            0xFFff5722, // Deep Orange
            0xFF795548, // Brown
    };

    public static final int NO_ICON  = -1;
    public static final int NO_TYPE  = -1;
    public static final int NO_COLOR = -1;

    private final String text;
    private final String sender;
    private SpannableString canvas;
    private long timestamp;

    private int color = NO_COLOR;
    private int type  = NO_ICON;
    private int icon  = NO_TYPE;

    /**
     * Create a new message without an icon defaulting to TYPE_MESSAGE
     *
     * @param text
     */
    public Message(String text)
    {
        this(text, null, TYPE_MESSAGE);
    }

    /**
     * Create a new message without an icon with a specific type
     *
     * @param text
     * @param type Message type
     */
    public Message(String text, int type)
    {
        this(text, null, type);
    }

    /**
     * Create a new message sent by a user, without an icon,
     * defaulting to TYPE_MESSAGE
     *
     * @param text
     * @param sender
     */
    public Message(String text, String sender)
    {
        this(text, sender, TYPE_MESSAGE);
    }

    /**
     * Create a new message sent by a user without an icon
     *
     * @param text
     * @param sender
     * @param type Message type
     */
    public Message(String text, String sender, int type)
    {
        this.text = text;
        this.sender = sender;
        this.timestamp = new Date().getTime();
        this.type = type;
    }

    /**
     * Set the message's icon
     */
    public void setIcon(int icon)
    {
        this.icon = icon;
    }

    /**
     * Get the message's icon
     *
     * @return
     */
    public int getIcon()
    {
        return icon;
    }

    /**
     * Get the text of this message
     *
     * @return
     */
    public String getText()
    {
        return text;
    }

    /**
     * Get the type of this message
     *
     * @return One of Message.TYPE_*
     */
    public int getType()
    {
        return type;
    }

    /**
     * Set the color of this message
     */
    public void setColor(int color)
    {
        this.color = color;
    }

    /**
     * Set the timestamp of the message
     *
     * @param timestamp
     */
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Associate a color with a sender name
     *
     * @return a color hexa
     */
    private int getSenderColor()
    {
        /* It might be worth to use some hash table here */
        if (sender == null) {
            return COLOR_DEFAULT;
        }

        int color = 0;

        for(int i = 0; i < sender.length(); i++){
            color += sender.charAt(i);
        }

        /* we dont want color[colors.length-1] which is black */
        color = color % (colors.length - 1);

        return colors[color];
    }

    /**
     * Render message as spannable string
     *
     * @return
     */
    public SpannableString render(Context context)
    {
        Settings settings = new Settings(context);

        if (canvas == null) {
            String prefix    = hasIcon() && settings.showIcons() ? "  " : "";
            String nick      = hasSender() ? "<" + sender + "> " : "";
            String timestamp = settings.showTimestamp() ? renderTimeStamp(settings.use24hFormat(), settings.includeSeconds()) : "";

            canvas = new SpannableString(prefix + timestamp + nick);
            SpannableString renderedText;

            String text = settings.showGraphicalSmilies() ? Emojis.convert(this.text) : this.text;

            if (settings.showMircColors()) {
                renderedText = MircColors.toSpannable(text);
            } else {
                renderedText = new SpannableString(
                    MircColors.removeStyleAndColors(text)
                );
            }

            canvas = new SpannableString(TextUtils.concat(canvas, renderedText));

            if (hasSender()) {
                int start = (prefix + timestamp).length() + 1;
                int end = start + sender.length();

                if (settings.showColorsNick()) {
                    canvas.setSpan(new ForegroundColorSpan(getSenderColor()), start, end , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            if (hasIcon() && settings.showIcons()) {
                Drawable drawable = context.getResources().getDrawable(icon);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                canvas.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (hasColor() && settings.showColors()) {
                // Only apply the foreground color to areas that don't already have a foreground color.
                ForegroundColorSpan[] spans = canvas.getSpans(0, canvas.length(), ForegroundColorSpan.class);
                int start = 0;

                for (int i = 0; i < spans.length; i++) {
                    canvas.setSpan(new ForegroundColorSpan(color), start, canvas.getSpanStart(spans[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = canvas.getSpanEnd(spans[i]);
                }

                canvas.setSpan(new ForegroundColorSpan(color), start, canvas.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return canvas;
    }

    /**
     * Does this message have a sender?
     *
     * @return
     */
    private boolean hasSender()
    {
        return sender != null;
    }

    /**
     * Does this message have a color assigned?
     *
     * @return
     */
    private boolean hasColor()
    {
        return color != NO_COLOR;
    }

    /**
     * Does this message have an icon assigned?
     *
     * @return
     */
    private boolean hasIcon()
    {
        return icon != NO_ICON;
    }

    /**
     * Render message as text view
     *
     * @param context
     * @return
     */
    public TextView renderTextView(Context context, TextView view)
    {
        if (view == null) {
            view = new TextView(context);
        }

        view.setAutoLinkMask(Linkify.ALL);
        view.setLinksClickable(true);
        view.setLinkTextColor(COLOR_BLUE);
        view.setText(this.render(context));
        view.setTextIsSelectable(true);

        return view;
    }

    /**
     * Generate a timestamp
     *
     * @param use24hFormat
     * @return
     */
    public String renderTimeStamp(boolean use24hFormat, boolean includeSeconds)
    {
        Date date = new Date(timestamp);

        int hours = date.getHours();
        int minutes = date.getMinutes();
        int seconds = date.getSeconds();

        if (!use24hFormat) {
            hours = Math.abs(12 - hours);
            if (hours == 12) {
                hours = 0;
            }
        }

        if (includeSeconds) {
            return String.format(
                "[%02d:%02d:%02d]",
                hours,
                minutes,
                seconds);
        } else {
            return String.format(
                "[%02d:%02d]",
                hours,
                minutes);
        }
    }
}
