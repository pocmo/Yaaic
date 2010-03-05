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

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.DeckAdapter;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ChannelListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Server;
import org.yaaic.receiver.ChannelReceiver;

/**
 * Connected to server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerActivity extends Activity implements ServiceConnection, ChannelListener
{
	public static final String TAG = "Yaaic/ServerActivity";
	
	private int serverId;
	private Server server;
	private IRCBinder binder;
	private ChannelReceiver receiver;
	
	private ViewSwitcher switcher;
	private Gallery deck;
	private DeckAdapter deckAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		serverId = getIntent().getExtras().getInt("serverId");
		server = (Server) Yaaic.getInstance().getServerById(serverId);
		setTitle("Yaaic - " + server.getTitle());
		
		setContentView(R.layout.server);
		
		((TextView) findViewById(R.id.title)).setText(server.getTitle());
		((ImageView) findViewById(R.id.status)).setImageResource(server.getStatusIcon());
		
		deckAdapter = new DeckAdapter();
		deck = (Gallery) findViewById(R.id.deck);
		deck.setAdapter(deckAdapter);
		switcher = (ViewSwitcher) findViewById(R.id.switcher);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
        Intent intent = new Intent(this, IRCService.class);
        bindService(intent, this, 0);
        
    	receiver = new ChannelReceiver(this);
    	registerReceiver(receiver, new IntentFilter(Broadcast.CHANNEL_MESSAGE));
    	registerReceiver(receiver, new IntentFilter(Broadcast.CHANNEL_NEW));
    	registerReceiver(receiver, new IntentFilter(Broadcast.CHANNEL_REMOVE));
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		unbindService(this);
		unregisterReceiver(receiver);
	}

	public void onServiceConnected(ComponentName name, IBinder service)
	{
		this.binder = (IRCBinder) service;
	}

	public void onServiceDisconnected(ComponentName name)
	{
		this.binder = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	// inflate from xml
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.server, menu);
    	
    	return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.disconnect:
				binder.getService().getConnection(serverId).quitServer();
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.join:
				final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.channeldialog);
				dialog.setTitle(R.string.channel);

				Button button = (Button) dialog.findViewById(R.id.join);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String channel = ((EditText) v.getRootView().findViewById(R.id.channel)).getText().toString();
						binder.getService().getConnection(serverId).joinChannel(channel);
						dialog.cancel();
					}
				});
				
				dialog.show();
				break;
		}
		
		return true;
	}

	/**
	 * On channel message
	 */
	public void onChannelMessage(String target)
	{
		String message = server.getChannel(target).pollMessage();
		
		TextView canvas = (TextView) deckAdapter.getItemByName(target);
		
		if (canvas != null) {
			Log.d(TAG, "Got canvas, setting text");
			canvas.append("\n" + message);
			deckAdapter.notifyDataSetChanged();
		} else {
			Log.d(TAG, "No canvas found");
		}
		//Toast.makeText(this, "(" + target + ") " + message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * On new channel
	 */
	public void onNewChannel(String target)
	{
		Log.d(TAG, "onNewChannel() " + target);
		
		TextView canvas = new TextView(this);
		canvas.setText(target);
		canvas.setTextColor(0xff000000);
		
		// XXX: Refactor this crap :)
		
        Display d = getWindowManager().getDefaultDisplay();
        int width = d.getWidth();
        int height = d.getHeight();
        
		float fw = (float) width;
		float fh = (float) height;
		
		float vwf = fw / 100 * 80;
		float vhf = fh / 100 * 80;
		
		int w = (int) vwf;
		int h = (int) vhf;
		
		canvas.setPadding(10, 10, 10, 10);
		canvas.setBackgroundColor(0xff888888);
		canvas.setLayoutParams(new Gallery.LayoutParams(w, h));
		
		deckAdapter.addItem(target, canvas);
		/*
		deck.addView(child)
		containers.put(target, new ChannelContainer(channel, canvas));
		*/
	}

	/**
	 * On channel remove
	 */
	public void onRemoveChannel(String target)
	{
	}
}
