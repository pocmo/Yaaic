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
package org.yaaic.listener;

import org.yaaic.adapter.DeckAdapter;
import org.yaaic.model.Conversation;
import org.yaaic.view.MessageListView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewSwitcher;

/**
 * Listener for clicks on conversations
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationClickListener implements OnItemClickListener
{
    private final DeckAdapter adapter;
    private final ViewSwitcher switcher;

    /**
     * Create a new ConversationClickListener
     * 
     * @param adapter
     * @param switcher
     */
    public ConversationClickListener(DeckAdapter adapter, ViewSwitcher switcher)
    {
        this.adapter = adapter;
        this.switcher = switcher;
    }

    /**
     * On conversation item clicked
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        Conversation conversation = adapter.getItem(position);

        MessageListView canvas = (MessageListView) adapter.getView(position, null, switcher);
        canvas.setSwitched(true);
        adapter.setSwitched(conversation.getName(), canvas);
    }
}
