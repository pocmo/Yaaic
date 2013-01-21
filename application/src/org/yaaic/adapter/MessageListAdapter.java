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
package org.yaaic.adapter;

import java.util.LinkedList;

import org.yaaic.model.Conversation;
import org.yaaic.model.Message;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for (channel) messages in a ListView
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageListAdapter extends BaseAdapter
{
    private final LinkedList<TextView> messages;
    private final Context context;
    private int historySize;

    /**
     * Create a new MessageAdapter
     * 
     * @param channel
     * @param context
     */
    public MessageListAdapter(Conversation conversation, Context context)
    {
        LinkedList<TextView> messages = new LinkedList<TextView>();

        // Render channel name as first message in channel
        if (conversation.getType() != Conversation.TYPE_SERVER) {
            Message header = new Message(conversation.getName());
            header.setColor(Message.COLOR_RED);
            messages.add(header.renderTextView(context));
        }

        // Optimization - cache field lookups
        LinkedList<Message> mHistory =  conversation.getHistory();
        int mSize = mHistory.size();

        for (int i = 0; i < mSize; i++) {
            messages.add(mHistory.get(i).renderTextView(context));
        }

        // XXX: We don't want to clear the buffer, we want to add only
        //      buffered messages that are not already added (history)
        conversation.clearBuffer();

        this.messages = messages;
        this.context = context;
        historySize = conversation.getHistorySize();
    }

    /**
     * Add a message to the list
     * 
     * @param message
     */
    public void addMessage(Message message)
    {
        messages.add(message.renderTextView(context));

        if (messages.size() > historySize) {
            messages.remove(0);
        }

        notifyDataSetChanged();
    }

    /**
     * Add a list of messages to the list
     * 
     * @param messages
     */
    public void addBulkMessages(LinkedList<Message> messages)
    {
        LinkedList<TextView> mMessages = this.messages;
        Context mContext = this.context;
        int mSize = messages.size();

        for (int i = mSize - 1; i > -1; i--) {
            mMessages.add(messages.get(i).renderTextView(mContext));

            if (mMessages.size() > historySize) {
                mMessages.remove(0);
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Get number of items
     * 
     * @return
     */
    @Override
    public int getCount()
    {
        return messages.size();
    }

    /**
     * Get item at given position
     * 
     * @param position
     * @return
     */
    @Override
    public TextView getItem(int position)
    {
        return messages.get(position);
    }

    /**
     * Get id of item at given position
     * 
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Get item view for the given position
     * 
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getItem(position);
    }

    /**
     * XXX This is almost certainly covering up a bug elsewhere -- find it!
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer == null) {
            return;
        }
        super.unregisterDataSetObserver(observer);
    }
}
