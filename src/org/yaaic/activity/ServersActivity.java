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
package org.yaaic.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.ServerListAdapter;
import org.yaaic.db.Database;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.layout.NonScalingBackgroundDrawable;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Extra;
import org.yaaic.model.Server;
import org.yaaic.model.Status;
import org.yaaic.receiver.ServerReceiver;

/**
 * List of servers
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServersActivity extends ListActivity implements ServiceConnection, ServerListener, OnItemLongClickListener {
	private IRCBinder binder;
	private ServerReceiver receiver;
	private ServerListAdapter adapter;
	
	/**
	 * On create
	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servers);
        
        adapter = new ServerListAdapter();
        setListAdapter(adapter);
        
        getListView().setOnItemLongClickListener(this);
        getListView().setBackgroundDrawable(new NonScalingBackgroundDrawable(this, getListView(), R.drawable.background));
    }

    /**
     * On resume
     */
    @Override
    public void onResume()
    {
    	super.onResume();
    	
        // Start and connect to service
        Intent intent = new Intent(this, IRCService.class);
        intent.setAction(IRCService.ACTION_BACKGROUND);
        startService(intent);
        bindService(intent, this, 0);

    	receiver = new ServerReceiver(this);
    	registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_UPDATE));
    	
    	adapter.loadServers();
    }
    
    /**
     * On pause
     */
    @Override
    public void onPause()
    {
    	super.onPause();
    	
		if (binder != null && binder.getService() != null) {
			binder.getService().checkServiceStatus();
		}
		
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
		Server server = adapter.getItem(position);
		
		Intent intent = new Intent(this, ConversationActivity.class);
		
		if (server.getStatus() == Status.DISCONNECTED) {
			server.setStatus(Status.PRE_CONNECTING);
			intent.putExtra("connect", true);
		}		
		
		intent.putExtra("serverId", server.getId());
		startActivity(intent);
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
			getString(R.string.edit),
			getString(R.string.delete)
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(server.getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch (item) {
			        case 0: // Connect
			        	if (server.getStatus() == Status.DISCONNECTED) {
				        	binder.connect(server);
				        	server.setStatus(Status.CONNECTING);
				        	adapter.notifyDataSetChanged();
			        	}
			        	break;
			        case 1: // Disconnect
			        	server.clearConversations();
						server.setStatus(Status.DISCONNECTED);
						binder.getService().getConnection(server.getId()).quitServer();
			        	break;
			        case 2: // Edit
			        	editServer(server.getId());
			        	break;
			        case 3: // Delete
			        	binder.getService().getConnection(server.getId()).quitServer();
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
	 * Start activity to edit server with given id
	 * 
	 * @param serverId The id of the server
	 */
	private void editServer(int serverId)
	{
		Server server = Yaaic.getInstance().getServerById(serverId);
		
		if (server.getStatus() != Status.DISCONNECTED) {
			Toast.makeText(this, "Disconnect from server before editing", Toast.LENGTH_SHORT).show();
		}
		else {
	    	Intent intent = new Intent(this, AddServerActivity.class);
	    	intent.putExtra(Extra.SERVER, serverId);
	    	startActivityForResult(intent, 0);
		}
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

    /**
     * On menu item selected
     */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.add:
				startActivityForResult(new Intent(this, AddServerActivity.class), 0);
				break;
			case R.id.about:
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.exit:
				ArrayList<Server> mServers = Yaaic.getInstance().getServersAsArrayList();
				for (Server server : mServers) {
					if (binder.getService().hasConnection(server.getId())) {
						binder.getService().getConnection(server.getId()).quitServer();
					}
				}
				// ugly
				binder.getService().stopForegroundCompat(R.string.app_name);
				finish();
		}
		
		return true;
	}

	/**
	 * On activity result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
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
    	db.close();
    	
    	Yaaic.getInstance().removeServerById(serverId);
    	adapter.loadServers();
	}

	/**
	 * On server status update
	 */
	public void onStatusUpdate()
	{
		adapter.loadServers();
		
		if (!adapter.isEmpty()) {
			// Hide background if there are servers in the list
			getListView().setBackgroundDrawable(null);
		}
	}
   
}