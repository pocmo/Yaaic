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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * The adapter for the "DeckView"
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DeckAdapter extends BaseAdapter
{
	private HashMap<String, View> map = new HashMap<String, View>();
	private LinkedList<View> views = new LinkedList<View>();
	
	/**
	 * Get number of item
	 */
	public int getCount()
	{
		return views.size();
	}

	/**
	 * Get item at position
	 */
	public View getItem(int position)
	{
		return views.get(position);
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
	public void addItem(String channel, View view)
	{
		map.put(channel, view);
		views.add(view);
		
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
		return map.get(channel);
	}
	
	/**
	 * Remove an item
	 * 
	 * @param channel
	 */
	public void removeItem(String channel)
	{
		View view = map.get(channel);
		views.remove(view);
		map.remove(channel);
		
		notifyDataSetChanged();
	}

	/**
	 * Get view at given position
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return views.get(position);
	}
}
