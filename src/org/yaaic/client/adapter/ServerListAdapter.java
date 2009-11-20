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
package org.yaaic.client.adapter;

import org.yaaic.client.R;
import org.yaaic.client.irc.IrcBinder;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * ServerListAdapter
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public class ServerListAdapter extends SimpleCursorAdapter
{
	/**
	 * Debugging/Log tag
	 */
	public static final String TAG = "Yaaic/ServerListAdapter";
	
	/**
	 * Activity that uses the ServerListAdapter
	 */
	private Activity context;
	
	/**
	 * IrcBinder for IrcService
	 */
	private IrcBinder binder;
	
	/**
	 * Create a new, fresh ServerListAdapter
	 * 
	 * @param context
	 * @param layout
	 * @param cursor
	 * @param from
	 * @param to
	 */
	public ServerListAdapter(Activity context, int layout, Cursor cursor, String[] from, int[] to)
	{
		super(context, layout, cursor, from, to);
		
		this.context = context;
	}
	
	public void setIrcBinder(IrcBinder binder)
	{
		this.binder = binder;
	}
	
	/**
	 * Get view for a row
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// Get row from simple cursor adapter
		View row =  super.getView(position, convertView, parent);
		
		TextView tvServerTitle = (TextView) row.findViewById(R.id.server_title);
		TextView tvServerIcon = (TextView) row.findViewById(R.id.server_icon);
		String title = tvServerTitle.getText().toString();
		
		Log.d(TAG, "Generating view for: " + title);
		
		if (binder != null) {
			Log.d(TAG, "Got a binder..");
			
			
			if (binder.isConnected(title)) {
				tvServerIcon.setCompoundDrawablesWithIntrinsicBounds(
					context.getResources().getDrawable(android.R.drawable.presence_online),
					null,
					null,
					null
				);
			} else {
				tvServerIcon.setCompoundDrawablesWithIntrinsicBounds(
					context.getResources().getDrawable(android.R.drawable.presence_offline),
					null,
					null,
					null
				);
			}
			
		}
		
		return row;
	}
}
