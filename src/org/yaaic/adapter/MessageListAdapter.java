/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2010 Sebastian Kaspari

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
	private LinkedList<TextView> messages;
	private Context context;
	
	/**
	 * Create a new MessageAdapter
	 * 
	 * @param channel
	 * @param context
	 */
	public MessageListAdapter(Conversation conversation, Context context)
	{
		this.messages = new LinkedList<TextView>();
		this.context = context;
		
		// Render channel name as first message in channel
		if (conversation.getType() != Conversation.TYPE_SERVER) {
			Message header = new Message(conversation.getName());
			header.setColor(Message.COLOR_RED);
			messages.add(header.renderTextView(context));
		}
		
		for (int i = 0; i < conversation.getHistorySize(); i++) {
			messages.add(conversation.getHistoryMessage(i).renderTextView(context));
		}
		
		// XXX: We don't want to clear the buffer, we want to add only
		//      buffered messages that are not already added (history)
		conversation.clearBuffer();
	}

	/**
	 * Add a message to the list
	 * 
	 * @param message
	 */
	public void addMessage(Message message)
	{
		messages.add(message.renderTextView(context));
		
		if (messages.size() > Conversation.HISTORY_SIZE) {
			messages.remove(0);
		}
		
		notifyDataSetChanged();
	}
	
	/**
	 * Get number of items
	 * 
	 * @return
	 */
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return getItem(position);
	}
}
