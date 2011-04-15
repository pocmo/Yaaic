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
package org.yaaic.adapter;

import java.util.LinkedList;

import org.yaaic.R;
import org.yaaic.listener.MessageClickListener;
import org.yaaic.model.Conversation;
import org.yaaic.view.MessageListView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The adapter for the "DeckView"
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DeckAdapter extends BaseAdapter
{
    private LinkedList<Conversation> conversations;
    private MessageListView currentView;
    private String currentChannel;

    /**
     * Create a new DeckAdapter instance
     */
    public DeckAdapter()
    {
        conversations = new LinkedList<Conversation>();
    }

    /**
     * Clear conversations
     */
    public void clearConversations()
    {
        conversations = new LinkedList<Conversation>();
    }

    /**
     * Get number of item
     */
    @Override
    public int getCount()
    {
        return conversations.size();
    }

    /**
     * Get item at position
     */
    @Override
    public Conversation getItem(int position)
    {
        if (position >= 0 && position < conversations.size()) {
            return conversations.get(position);
        }
        return null;
    }

    /**
     * Get id of item at position
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Add an item
     * 
     * @param channel Name of the channel
     * @param view The view object
     */
    public void addItem(Conversation conversation)
    {
        conversations.add(conversation);

        notifyDataSetChanged();
    }

    /**
     * Get an item by the channel's name
     * 
     * @param channel
     * @return The item
     */
    public int getPositionByName(String name)
    {
        // Optimization - cache field lookups
        int mSize = conversations.size();
        LinkedList<Conversation> mItems = this.conversations;

        for (int i = 0; i <  mSize; i++) {
            if (mItems.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Remove an item
     * 
     * @param channel
     */
    public void removeItem(String target)
    {
        int position = getPositionByName(target);

        if (position != -1) {
            conversations.remove(position);
            notifyDataSetChanged();
        }
    }

    /**
     * Set single channel view
     * 
     * @param switched
     */
    public void setSwitched(String channel, MessageListView current)
    {
        currentChannel = channel;
        currentView = current;
    }

    /**
     * Get single channel view
     * 
     * @return
     */
    public MessageListView getSwitchedView()
    {
        return currentView;
    }

    /**
     * Get name of channel (single channel view)
     * 
     * @return
     */
    public String getSwitchedName()
    {
        return currentChannel;
    }

    /**
     * Has the view been switched to single channel view?
     * 
     * @return view true if view is in single channel view, false otherwise
     */
    public boolean isSwitched()
    {
        return currentView != null;
    }

    /**
     * Get view at given position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Conversation conversation = getItem(position);

        // Market stack traces prove that sometimes we get a null converstion
        // because the collection changed while a view is requested for an
        // item that does not exist anymore... so we just need to reply with
        // some kind of view here.
        if (conversation == null) {
            return new TextView(parent.getContext());
        }

        return renderConversation(conversation, parent);
    }

    /**
     * Render a conversation view (MessageListView)
     * 
     * @param channel The conversation of the view
     * @param parent The parent view (context)
     * @return The rendered MessageListView
     */
    public MessageListView renderConversation(Conversation conversation, ViewGroup parent)
    {
        MessageListView list = new MessageListView(parent.getContext(), parent);
        list.setOnItemClickListener(MessageClickListener.getInstance());

        MessageListAdapter adapter = conversation.getMessageListAdapter();

        if (adapter == null) {
            adapter = new MessageListAdapter(conversation, parent.getContext());
            conversation.setMessageListAdapter(adapter);
        }

        list.setAdapter(adapter);

        list.setDivider(null);
        list.setLayoutParams(new Gallery.LayoutParams(
            parent.getWidth()*85/100,
            parent.getHeight()
        ));

        list.setBackgroundResource(R.layout.rounded);
        list.setCacheColorHint(0xee000000);
        list.setPadding(5, 5, 5, 5);
        list.setVerticalFadingEdgeEnabled(false);
        list.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_INSET);
        list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setSelection(list.getAdapter().getCount() - 1); // scroll to bottom

        return list;
    }
}
