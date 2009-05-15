package org.yaaic.client;

import org.yaaic.client.db.ServerConstants;
import org.yaaic.client.db.ServerDatabase;
import org.yaaic.client.irc.IrcBinder;
import org.yaaic.client.irc.IrcService;

import android.app.ExpandableListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ServerWindow extends ExpandableListActivity implements ServiceConnection
{
	private static final String TAG = "Yaaic/ServerWindow";
	private IrcBinder binder;
	private String title;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serverwindow);
		
		title = getIntent().getStringExtra("server_title");
		setTitle(title);
		
		((TextView) findViewById(R.id.server_title)).setText(title);
		
		setListAdapter(new ServerWindowListAdapter(this));
		//registerForContextMenu(getExpandableListView());
		
        Intent serviceIntent = new Intent(this, IrcService.class);
        boolean binding = bindService(serviceIntent, this, 0);
        Log.d(TAG, "Binding to Service: " + binding);
	}
	
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.server, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case R.id.server_quit:
    			finish();
    			return true;
    	}
		return false;
    }
    
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Log.d(TAG, "Service connected");
		
		binder = (IrcBinder) service;
		
		ServerDatabase db = new ServerDatabase(this);
		Cursor cursor = db.getServer(title);
		if (cursor.moveToNext()) {
			binder.connect(
					cursor.getString(cursor.getColumnIndex(ServerConstants.TITLE)),
					cursor.getString(cursor.getColumnIndex(ServerConstants.HOST)),
					cursor.getInt(cursor.getColumnIndex(ServerConstants.PORT)),
					cursor.getString(cursor.getColumnIndex(ServerConstants.PASSWORD))
			);
		}
		cursor.close();
		db.close();
		
		TextView tv = (TextView) findViewById(R.id.server_title);
		
		if (binder.isConnected(title)) {
			tv.setCompoundDrawablesWithIntrinsicBounds(
				getResources().getDrawable(android.R.drawable.presence_online),
				null,
				null,
				null
			);
		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(android.R.drawable.presence_offline),
					null,
					null,
					null
				);
		}
	}
	
	public void onServiceDisconnected(ComponentName name)
	{
		Log.d(TAG, "Service disconnected");
	}
	
    public void onDestroy()
    {
    	Log.d(TAG, "onDestroy");
    	
    	super.onDestroy();
    	
    	unbindService(this);
    }
}
