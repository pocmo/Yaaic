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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

/**
 * Adapter for server lists
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerListAdapter extends BaseAdapter
{
	private ArrayList<Server> servers;
	
	/**
	 * Create a new adapter for server lists
	 */
	public ServerListAdapter()
	{
		loadServers();
	}
	
	public void loadServers()
	{
		servers = Yaaic.getInstance().getServersAsArrayList();
		notifyDataSetChanged();
	}
	
	/**
	 * Get number of items
	 */
	public int getCount()
	{
		return servers.size();
	}

	/**
	 * Get item at position
	 * 
	 * @param position
	 */
	public Server getItem(int position)
	{
		return servers.get(position);
	}

	/**
	 * Get id of item at position
	 * 
	 * @param position
	 */
	public long getItemId(int position)
	{
		return getItem(position).getId();
	}

	/**
	 * Get view for item at given position
	 * 
	 * @param position
	 * @param convertView
	 * @param parent
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Server server = getItem(position);
		
		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.serveritem, null);
		
		((TextView) v.findViewById(R.id.title)).setText(server.getTitle());
		((TextView) v.findViewById(R.id.host)).setText(server.getHost());
		
		((ImageView) v.findViewById(R.id.status)).setImageResource(server.getStatusIcon());
		
		return v;
	}
}
