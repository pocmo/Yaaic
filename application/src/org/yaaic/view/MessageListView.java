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
import android.widget.ListView;

/**
 * A customized ListView for Messages
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageListView extends ListView
{
    /**
     * Create a new MessageListView
     *
     * @param context
     */
    public MessageListView(Context context)
    {
        super(context);

        setOnItemClickListener(MessageClickListener.getInstance());

        setDivider(null);

        setBackgroundResource(R.layout.border);
        setCacheColorHint(0xFF181818);
        setVerticalFadingEdgeEnabled(false);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);

        // Scale padding by screen density
        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (5 * density);
        setPadding(padding, padding, padding, 0);

        // XXX: This should be dynamically
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
}
