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

import org.yaaic.listener.MessageClickListener;
import org.yaaic.model.Conversation;
import org.yaaic.view.MessageListView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The adapter for the "DeckView"
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DeckAdapter extends BaseAdapter
{
    private LinkedList<ConversationInfo> conversations;
    private MessageListView currentView;
    private String currentChannel;

    public class ConversationInfo {
        public Conversation conv;
        public MessageListAdapter adapter;
        public MessageListView view;

        public ConversationInfo(Conversation conv) {
            this.conv = conv;
            this.adapter = null;
            this.view = null;
        }
    }

    /**
     * Create a new DeckAdapter instance
     */
    public DeckAdapter()
    {
        conversations = new LinkedList<ConversationInfo>();
    }

    /**
     * Clear conversations
     */
    public void clearConversations()
    {
        conversations = new LinkedList<ConversationInfo>();
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
     * Get ConversationInfo on item at position
     */
    private ConversationInfo getItemInfo(int position) {
        if (position >= 0 && position < conversations.size()) {
            return conversations.get(position);
        }
        return null;
    }

    /**
     * Get item at position
     */
    @Override
    public Conversation getItem(int position)
    {
        ConversationInfo convInfo = getItemInfo(position);
        if (convInfo != null) {
            return convInfo.conv;
        } else {
            return null;
        }
    }

    /**
     * Get MessageListAdapter belonging to a conversation
     *
     * @param position Position of the conversation in the deck
     */
    public MessageListAdapter getItemAdapter(int position) {
        ConversationInfo convInfo = getItemInfo(position);
        if (convInfo != null) {
            return convInfo.adapter;
        } else {
            return null;
        }
    }

    /**
     * Get MessageListAdapter belonging to a conversation
     *
     * @param name Name of the conversation
     */
    public MessageListAdapter getItemAdapter(String name) {
        return getItemAdapter(getPositionByName(name));
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
        conversations.add(new ConversationInfo(conversation));

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
        LinkedList<ConversationInfo> mItems = this.conversations;

        for (int i = 0; i <  mSize; i++) {
            if (mItems.get(i).conv.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Remove an item
     * 
     * @param position
     */
    public void removeItem(int position)
    {
        if (position >= 0 && position < conversations.size()) {
            conversations.remove(position);
            notifyDataSetChanged();
        }
    }

    /**
     * Remove an item
     * 
     * @param target
     */
    public void removeItem(String target)
    {
        removeItem(getPositionByName(target));
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
        return currentChannel != null;
    }

    /**
     * Get view at given position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ConversationInfo convInfo = getItemInfo(position);

        // Market stack traces prove that sometimes we get a null converstion
        // because the collection changed while a view is requested for an
        // item that does not exist anymore... so we just need to reply with
        // some kind of view here.
        if (convInfo == null || convInfo.conv == null) {
            return new TextView(parent.getContext());
        }

        if (convInfo.view != null) {
            return convInfo.view;
        } else {
            return renderConversation(convInfo, parent);
        }
    }

    /**
     * Render a conversation view (MessageListView)
     * 
     * @param channel The conversation of the view
     * @param parent The parent view (context)
     * @return The rendered MessageListView
     */
    private MessageListView renderConversation(ConversationInfo convInfo, ViewGroup parent)
    {
        MessageListView list = new MessageListView(parent.getContext(), parent);
        convInfo.view = list;
        list.setOnItemClickListener(MessageClickListener.getInstance());

        MessageListAdapter adapter = convInfo.adapter;

        if (adapter == null) {
            adapter = new MessageListAdapter(convInfo.conv, parent.getContext());
            convInfo.adapter = adapter;
        }

        list.setAdapter(adapter);
        list.setSelection(adapter.getCount() - 1); // scroll to bottom

        if (convInfo.conv.getName().equals(currentChannel)) {
            list.setSwitched(true);
            currentView = list;
        }

        return list;
    }
}
