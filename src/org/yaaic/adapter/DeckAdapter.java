/*
 Yaaic - Yet Another Android IRC Client

Copyright 2009 Sebastian Kaspari

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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.yaaic.model.Conversation;
import org.yaaic.view.MessageListView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ListView;

/**
 * The adapter for the "DeckView"
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DeckAdapter extends BaseAdapter
{
	public static final String TAG = "Yaaic/DeckAdapter";
	
	private List<Conversation> conversations;
	private MessageListView currentView;
	private String currentChannel;
	
	/**
	 * Create a new DeckAdapter instance
	 */
	public DeckAdapter()
	{
		conversations = Collections.synchronizedList(new LinkedList<Conversation>());
	}
	
	/**
	 * Get number of item
	 */
	public int getCount()
	{
		return conversations.size();
	}

	/**
	 * Get item at position
	 */
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
		for (int i = 0; i <  conversations.size(); i++) {
			if (conversations.get(i).getName().equals(name)) {
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Conversation conversation = getItem(position);
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
		MessageListView list = new MessageListView(parent.getContext());
		list.setAdapter(new MessageListAdapter(conversation, parent.getContext()));
		
		list.setDivider(null);
		list.setLayoutParams(new Gallery.LayoutParams(
			parent.getWidth() / 100 * 85,
			parent.getHeight() / 100 * 95
		));
		list.setBackgroundColor(0xff222222);
		list.setPadding(5, 5, 5, 5);
		list.setVerticalFadingEdgeEnabled(false);
		list.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_INSET);
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		list.setSelection(list.getAdapter().getCount() - 1); // scroll to bottom
		
		return list;
	}
}
