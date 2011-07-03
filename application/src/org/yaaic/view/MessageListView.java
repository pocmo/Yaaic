/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

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
package org.yaaic.view;

import org.yaaic.R;
import org.yaaic.adapter.MessageListAdapter;
import org.yaaic.listener.MessageClickListener;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;
import android.widget.ListView;

/**
 * A customized ListView for Messages
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageListView extends ListView
{
    private boolean switched = false;
    private final View parent;
    private int parentWidth;
    private int parentHeight;
    private final int padding;
    private final int paddingWide;

    /**
     * Create a new MessageListView
     * 
     * @param context
     */
    public MessageListView(Context context, View parent)
    {
        super(context);

        this.parent = parent;
        setOnItemClickListener(MessageClickListener.getInstance());

        parentWidth = parent.getWidth();
        parentHeight = parent.getHeight();

        setDivider(null);
        setLayoutParams(new Gallery.LayoutParams(
            parentWidth*85/100,
            parentHeight
        ));

        setBackgroundResource(R.layout.rounded);
        setCacheColorHint(0xee000000);
        setVerticalFadingEdgeEnabled(false);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);

        // Scale padding by screen density
        float density = context.getResources().getDisplayMetrics().density;
        padding = (int)(5 * density);
        paddingWide = (int)(12 * density);
        setPadding(padding, padding, padding, 0);
    }

    /**
     * Handle touch screen motion events
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!switched) {
            // We delegate the touch events to the underlying view
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * On draw
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        if (parent.getWidth() != parentWidth || parent.getHeight() != parentHeight) {
            // parent size changed, resizing this child too

            parentWidth = parent.getWidth();
            parentHeight = parent.getHeight();

            if (!switched) {
                setLayoutParams(new Gallery.LayoutParams(
                    parentWidth*85/100,
                    parentHeight
                ));
            }
        }

        super.onDraw(canvas);
    }

    /**
     * Get the adapter of this MessageListView
     * (Helper to avoid casting)
     * 
     * @return The MessageListAdapter
     */
    @Override
    public MessageListAdapter getAdapter()
    {
        return (MessageListAdapter) super.getAdapter();
    }

    /**
     * Set whether this conversation is switched (taking up all of deck's space
     * and handling touch events itself)
     */
    public void setSwitched(boolean switched)
    {
        this.switched = switched;

        if (switched) {
            setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.FILL_PARENT));
            setTranscriptMode(TRANSCRIPT_MODE_NORMAL);
            setPadding(paddingWide, padding, paddingWide, 0);
        } else {
            setLayoutParams(new Gallery.LayoutParams(parentWidth*85/100, parentHeight));
            setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);
            setPadding(padding, padding, padding, 0);
        }
    }
}
