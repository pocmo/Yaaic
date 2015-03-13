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

import java.util.ArrayList;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.model.Server;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for server lists
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerListAdapter extends BaseAdapter
{
    private static final int COLOR_CONNECTED    = 0xFFbcbcbc;
    private static final int COLOR_DISCONNECTED = 0xFF585858;

    private ArrayList<Server> servers;

    /**
     * Create a new adapter for server lists
     */
    public ServerListAdapter()
    {
        loadServers();
    }

    /**
     * Load servers from database
     *
     * Delegate call to yaaic instance
     */
    public void loadServers()
    {
        servers = Yaaic.getInstance().getServersAsArrayList();
        notifyDataSetChanged();
    }

    /**
     * Get number of items
     */
    @Override
    public int getCount()
    {
        int size = servers.size();

        // Display "Add server" item
        if (size == 0) {
            return 1;
        }

        return size;
    }

    /**
     * Get item at position
     * 
     * @param position
     */
    @Override
    public Server getItem(int position)
    {
        if (servers.size() == 0) {
            return null; // No server object for the "add server" view
        }

        return servers.get(position);
    }

    /**
     * Get id of item at position
     * 
     * @param position
     */
    @Override
    public long getItemId(int position)
    {
        if (servers.size() == 0) {
            return 0;
        }

        return getItem(position).getId();
    }

    /**
     * Get view for item at given position
     * 
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Server server = getItem(position);

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (server == null) {
            // Return "Add server" view
            return inflater.inflate(R.layout.addserveritem, null);
        }

        View v = inflater.inflate(R.layout.serveritem, null);

        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(server.getTitle());

        TextView hostView = (TextView) v.findViewById(R.id.host);
        hostView.setText(server.getIdentity().getNickname() + " @ " + server.getHost() + " : " + server.getPort());

        if (server.isConnected()) {
            titleView.setTextColor(COLOR_CONNECTED);
            hostView.setTextColor(COLOR_CONNECTED);
        } else {
            titleView.setTextColor(COLOR_DISCONNECTED);
            hostView.setTextColor(COLOR_DISCONNECTED);
        }

        ((ImageView) v.findViewById(R.id.status)).setImageResource(server.getStatusIcon());

        return v;
    }
}
