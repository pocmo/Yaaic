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

import java.util.Date;

import org.yaaic.utils.MircColors;
import org.yaaic.utils.Smilies;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * A channel or server message
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Message
{
    public static final int COLOR_GREEN   = 0xFF458509;
    public static final int COLOR_RED     = 0xFFcc0000;
    public static final int COLOR_BLUE    = 0xFF729fcf;
    public static final int COLOR_YELLOW  = 0xFFbe9b01;
    public static final int COLOR_GREY    = 0xFFaaaaaa;
    public static final int COLOR_DEFAULT = 0xFFeeeeee;

    /* normal message, this is the default */
    public static final int TYPE_MESSAGE = 0;

    /* join, part or quit */
    public static final int TYPE_MISC    = 1;

    /* Some are light versions because dark colors hardly readable on
     * Yaaic's dark background */
    public static final int[] colors = {
        0xFFffffff, // White
        0xFFffff00, // Yellow
        0xFFff00ff, // Fuchsia
        0xFFff0000, // Red
        0xFFc0c0c0, // Silver
        0xFF808080, // Gray
        0xFF808000, // Olive
        0xFFC040C0, // Light Purple
        0xFFC04040, // Light Maroon
        0xFF00ffff, // Agua
        0xFF80ff80, // Light Lime
        0xFF008080, // Teal
        0xFF008000, // Green
        0xFF8484FF, // Light Blue
        0xFF6060D0, // Light Navy
        0xFF000000, // Black
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

            if (settings.showMircColors()) {
                renderedText = MircColors.toSpannable(text);
            } else {
                renderedText = new SpannableString(
                    MircColors.removeStyleAndColors(text)
                );
            }

            if (settings.showGraphicalSmilies()) {
                renderedText = Smilies.toSpannable(renderedText, context);
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
    public TextView renderTextView(Context context)
    {
        TextView canvas = new TextView(context);

        canvas.setAutoLinkMask(Linkify.ALL);
        canvas.setLinksClickable(true);
        canvas.setLinkTextColor(COLOR_BLUE);

        canvas.setText(this.render(context));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupViewForHoneycombAndLater(canvas);
        }

        return canvas;
    }

    @TargetApi(11)
    private void setupViewForHoneycombAndLater(TextView canvas) {
        canvas.setTextIsSelectable(true);
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
