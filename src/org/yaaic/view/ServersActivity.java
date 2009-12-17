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
package org.yaaic.view;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.ServerListAdapter;
import org.yaaic.db.Database;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Server;
import org.yaaic.model.Status;
import org.yaaic.receiver.ServerReceiver;

/**
 * List of servers
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServersActivity extends ListActivity implements ServiceConnection, ServerListener, OnItemLongClickListener {
	public static final String TAG = "Yaaic/ServersActivity";
	
	private IRCBinder binder;
	private ServerReceiver receiver;
	private ServerListAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servers);
        
        adapter = new ServerListAdapter();
        setListAdapter(adapter);
        
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	
        // Start and connect to service
        Intent intent = new Intent(this, IRCService.class);
        startService(intent);
        bindService(intent, this, 0);
    	
    	receiver = new ServerReceiver(this);
    	
    	registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_UPDATE));
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	unbindService(this);
    	unregisterReceiver(receiver);
    }
    
    /**
     * Service connected to Activity
     */
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		binder = (IRCBinder) service;
	}

	/**
	 * Service disconnected from Activity
	 */
	public void onServiceDisconnected(ComponentName name)
	{
		binder = null;
	}

	/**
	 * On server selected
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final Server server = adapter.getItem(position);
		
		switch (server.getStatus()) {
			case Status.DISCONNECTED:
				server.setStatus(Status.CONNECTING);
				adapter.notifyDataSetChanged();
				binder.connect(server);
				break;
			case Status.CONNECTED:
				Intent intent = new Intent(this, ServerActivity.class);
				intent.putExtra("serverId", server.getId());
				startActivityForResult(intent, 0);
			break;
		}
	}
	
	/**
	 * On long click
	 */
	public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id)
	{
		final Server server = adapter.getItem(position);
		
		final CharSequence[] items = {
			getString(R.string.connect),
			getString(R.string.disconnect),
			getString(R.string.delete)
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(server.getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch (item) {
			        case 0: // Connect
			        	binder.connect(server);
			        	server.setStatus(Status.CONNECTING);
			        	adapter.notifyDataSetChanged();
			        	break;
			        case 1: // Disconnect
						binder.getService().getConnection(server.getId()).quitServer();
			        	break;
			        case 2: // Delete
		        		deleteServer(server.getId());
			        	break;
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		return true;
	}
	
	/**
	 * Options Menu (Menu Button pressed)
	 */
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	// inflate from xml
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.servers, menu);
    	
    	return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				startActivityForResult(new Intent(this, AddServerActivity.class), 0);
			break;
		}
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// Refresh list from database
			adapter.loadServers();
		}
	}
	
	/**
	 * Delete server
	 * 
	 * @param serverId
	 */
	public void deleteServer(int serverId)
	{
    	Database db = new Database(this);
    	db.removeServerById(serverId);
    	
    	Yaaic.getInstance().removeServerById(serverId);
    	adapter.loadServers();
	}

	/**
	 * On server status updat
	 */
	public void onStatusUpdate()
	{
		Log.d(TAG, "Received server status update");
		adapter.loadServers();
	}
   
}