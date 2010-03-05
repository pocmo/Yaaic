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

import java.util.HashMap;
import java.util.LinkedList;

import org.yaaic.model.Channel;

import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * The adapter for the "DeckView"
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DeckAdapter extends BaseAdapter
{
	private HashMap<String, View> map = new HashMap<String, View>();
	private LinkedList<Channel> channels = new LinkedList<Channel>();
	private View currentView;
	private String currentChannel;
	
	private int width;
	private int height;
	
	/**
	 * Create a new DeckAdapter
	 * 
	 * @param width
	 * @param height
	 */
	public DeckAdapter(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Get number of item
	 */
	public int getCount()
	{
		return channels.size();
	}

	/**
	 * Get item at position
	 */
	public Channel getItem(int position)
	{
		return channels.get(position);
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
	public void addItem(Channel channel)
	{
		channels.add(channel);
		
		notifyDataSetChanged();
	}
	
	/**
	 * Get an item by the channel's name
	 * 
	 * @param channel
	 * @return The item
	 */
	public View getItemByName(String channel)
	{
		if (map.containsKey(channel)) {
			return map.get(channel);
		}
		
		return null;
	}
	
	/**
	 * Remove an item
	 * 
	 * @param channel
	 */
	public void removeItem(Channel channel)
	{
		channels.remove(channel);
		
		notifyDataSetChanged();
	}
	
	/**
	 * Set single channel view
	 * 
	 * @param switched
	 */
	public void setSwitched(String channel, View current)
	{
		currentChannel = channel;
		currentView = current;
	}
	
	/**
	 * Get single channel view
	 * 
	 * @return
	 */
	public View getSwitchedView()
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
		Channel channel = getItem(position);
		convertView = map.get(channel.getName());
		
		if (convertView == null) {
			convertView = renderChannel(channel, parent);
			map.put(channel.getName(), convertView);
		}

		return convertView;
	}
	
	public View renderChannel(Channel channel, ViewGroup parent)
	{
		TextView canvas = new TextView(parent.getContext());
		canvas.setText(channel.getName());
		
		for (String message : channel.getHistory()) {
			canvas.append("\n" + message);
		}
		
		canvas.setTextColor(0xff000000);
		
		// XXX: Refactor this crap :)
        
		float fw = (float) width;
		float fh = (float) height;
		
		float vwf = fw / 100 * 80;
		float vhf = fh / 100 * 80;
		
		int w = (int) vwf;
		int h = (int) vhf;
		
		canvas.setPadding(10, 10, 10, 10);
		canvas.setBackgroundColor(0xff888888);
		canvas.setLayoutParams(new Gallery.LayoutParams(w, h));
		
		return canvas;
	}
}
