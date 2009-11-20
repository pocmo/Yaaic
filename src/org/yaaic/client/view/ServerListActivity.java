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
package org.yaaic.client.view;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import org.yaaic.client.R;
import org.yaaic.client.adapter.ServerListAdapter;
import org.yaaic.client.db.ServerConstants;
import org.yaaic.client.db.ServerDatabase;
import org.yaaic.client.irc.IrcBinder;
import org.yaaic.client.irc.IrcService;

/**
 * ServerList Activity
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public class ServerListActivity extends ListActivity implements OnItemLongClickListener, ServiceConnection
{
	/**
	 * Debugging/Log tag
	 */
	public static final String TAG = "Yaaic/ServerList";
	
	private ServerDatabase db;
	
	/**
	 * Mapping for servers database to servers list view
	 */
	private static final String[] FROM = { ServerConstants.TITLE, ServerConstants.HOST };
	
	/**
	 * Mapping for servers database to servers list view
	 */
	private static final int[] TO = { R.id.server_title, R.id.server_host };
	
	/**
	 * Cursor for servers database
	 */
	private Cursor cursor;
	
	/**
	 * Binder for IrcService
	 */
	private IrcBinder binder;
	
	/**
	 * On Create
	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.d(TAG, "onCreate");
    	
        super.onCreate(savedInstanceState);

        // Start Service
        Intent serviceIntent = new Intent(this, IrcService.class);
        startService(serviceIntent);
        boolean binding = bindService(serviceIntent, this, 0);
        Log.d(TAG, "Binding to Service: " + binding);
        
        setContentView(R.layout.serverlist);
        
    	db = new ServerDatabase(this);

    	cursor = db.getServers();
    	this.startManagingCursor(cursor);
    	
    	SimpleCursorAdapter adapter = new ServerListAdapter(this, R.layout.serveritem, cursor, FROM, TO);
    	this.setListAdapter(adapter);

    	this.getListView().setOnItemLongClickListener(this);
        
    	this.initAnimation();
    }
    
    /**
     * Create and apply animations for the server list (on create)
     */
    public void initAnimation()
    {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(300);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        ListView lv = (ListView) getListView();
        lv.setLayoutAnimation(controller);
    }
    
    /**
     * On Resume
     */
    public void onResume()
    {
    	Log.d(TAG, "onResume");
    	
    	super.onResume();
    }
    
    /**
     * On Pause
     */
    public void onPause()
    {
    	Log.d(TAG, "onPause");
    	
    	super.onPause();
    }
    
    /**
     * On Destroy
     */
    public void onDestroy()
    {
    	Log.d(TAG, "onDestroy");
    	
    	super.onDestroy();
    	
    	db.close();
    	unbindService(this); // disconnect service
    }
    
    /**
     * Click Listener
     */
    public void onListItemClick(ListView listView, View view, int position, long id)
    {
    	TextView tv = (TextView) view.findViewById(R.id.server_title);
    	
    	Intent serverIntent = new Intent(this, ServerActivity.class);
    	serverIntent.putExtra("server_title", tv.getText());
    	startActivity(serverIntent);
    	//cursor.requery();
    }
    
    /**
     * Long Click Listener
     */
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
		final TextView tv = (TextView) v.findViewById(R.id.server_title);
		
    	new AlertDialog.Builder(this)
		.setTitle(tv.getText())
		.setItems(R.array.server_popup,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					onServerDialogSelect(i, tv.getText().toString());
				}
			}
		).show();
		return true;
	}
	
	/**
	 * Server selected (Long click)
	 * 
	 * @param item id/position of item selected
	 * @param title title of item (server title [unique id])
	 */
	public void onServerDialogSelect(int item, String title)
	{
		Log.d(TAG, "ServerDialogSelect: Item #" + item);
		
		switch (item)
		{
			case 0: // Connect
				if (!binder.isConnected(title)) {
					connectToServer(title);
				} else {
					Toast toast = Toast.makeText(this, "You are already connected to " + title, Toast.LENGTH_SHORT);
					toast.show();
				}
				break;
			case 1: // Delete
				db.removeServer(title);
				cursor.requery();
				break;
			case 2: // Disconnect
				binder.disconnect(title);
				break;
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
    	inflater.inflate(R.menu.main, menu);
    	
    	return true;
    }
    
    /**
     * Listener for menu items
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		// Add a new server
    		case R.id.server_add:
    			Intent serverAddIntent = new Intent(this, ServerAddActivity.class);
    			startActivity(serverAddIntent);
    			return true;
			// Show settings
    		case R.id.settings:
    			Intent settingsIntent = new Intent(this, SettingsActivity.class);
    			startActivity(settingsIntent);
    			return true;
			// Show about window
    		case R.id.about:
    			Intent aboutIntent = new Intent(this, AboutActivity.class);
    			startActivity(aboutIntent);
    			return true;
    	}
    	return false;
    }

    /**
     * Listener: IrcService connected to activity
     */
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Log.d(TAG, "Service connected");
		
		binder = (IrcBinder) service;
		
        // Look for AutoConnect servers and connect if needed
        Cursor autoCursor = db.getAutoConnectServers();
		while(autoCursor.moveToNext()) {
			binder.connect(
				autoCursor.getString(autoCursor.getColumnIndex(ServerConstants.TITLE)),
				autoCursor.getString(autoCursor.getColumnIndex(ServerConstants.HOST)),
				autoCursor.getInt(autoCursor.getColumnIndex(ServerConstants.PORT)),
				autoCursor.getString(autoCursor.getColumnIndex(ServerConstants.PASSWORD))
			);
		}
		autoCursor.close();
		
		ServerListAdapter adapter = (ServerListAdapter) getListAdapter();
		adapter.setIrcBinder(binder);
		cursor.requery();
	}
	
	/**
	 * Listener: IrcServers disconnected from activity
	 */
	public void onServiceDisconnected(ComponentName name)
	{
		Log.d(TAG, "Service disconnected");
	}
	
	/**
	 * Connect to given server
	 * 
	 * @param title Title of server (unique id)
	 */
	private void connectToServer(String title)
	{
		Cursor cursor = db.getServer(title);
		if (cursor.moveToNext()) {
			binder.connect(
				cursor.getString(cursor.getColumnIndex(ServerConstants.TITLE)),
				cursor.getString(cursor.getColumnIndex(ServerConstants.HOST)),
				cursor.getInt(cursor.getColumnIndex(ServerConstants.PORT)),
				cursor.getString(cursor.getColumnIndex(ServerConstants.PASSWORD))
			);
		} else {
			Log.d(TAG, "Could not find server: " + title);
		}
		cursor.close();
	}
}