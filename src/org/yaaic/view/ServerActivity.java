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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TableLayout.LayoutParams;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.DeckAdapter;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ChannelListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Message;
import org.yaaic.model.Server;
import org.yaaic.receiver.ChannelReceiver;

/**
 * Connected to server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerActivity extends Activity implements ServiceConnection, ChannelListener, OnItemClickListener
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
		
        Display d = getWindowManager().getDefaultDisplay();

        deckAdapter = new DeckAdapter(d.getWidth(), d.getHeight());
		deck = (Gallery) findViewById(R.id.deck);
		deck.setAdapter(deckAdapter);
		deck.setOnItemClickListener(this);

		switcher = (ViewSwitcher) findViewById(R.id.switcher);
		
		for (Channel channel : server.getChannels()) {
			onNewChannel(channel.getName());
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
        Intent intent = new Intent(this, IRCService.class);
        bindService(intent, this, 0);
        
    	receiver = new ChannelReceiver(server.getId(), this);
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
				server.clearChannels();
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
	 * Get server object assigned to this activity
	 * 
	 * @return the server object
	 */
	public Server getServer()
	{
		return server;
	}

	/**
	 * On channel message
	 */
	public void onChannelMessage(String target)
	{
		Message message = server.getChannel(target).pollMessage();
		
		TextView canvas = (TextView) deckAdapter.getItemByName(target);
		
		if (canvas != null) {
			canvas.append(message.render(canvas.getContext()));
			deckAdapter.notifyDataSetChanged();
			
			if (target.equals(deckAdapter.getSwitchedName())) {
				((TextView) deckAdapter.getSwitchedView()).append(message.render(canvas.getContext()));
			}
		} else {
			Log.d(TAG, "No canvas found");
		}
	}

	/**
	 * On new channel
	 */
	public void onNewChannel(String target)
	{
		Log.d(TAG, "onNewChannel() " + target);
		
		deckAdapter.addItem(server.getChannel(target));
	}

	/**
	 * On Channel item clicked
	 */
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		Log.d(TAG, "Selected channel: " + position);
		
		Channel channel = deckAdapter.getItem(position);
		view = deckAdapter.renderChannel(channel, switcher);
		//getView(position, view, deck);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		deckAdapter.setSwitched(channel.getName(), view);
		switcher.addView(view);
		switcher.showNext();
	}
	
	/**
	 * On key down
	 * 
	 * This is glue code to call onBackPressed() which
	 * will be automatically called by later android releases
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		return false;
	}
	
	/**
	 * On back key pressed
	 */
	public void onBackPressed()
	{
		if (deckAdapter.isSwitched()) {
			switcher.showNext();
			switcher.removeView(deckAdapter.getSwitchedView());
			//switcher.showNext();
			deckAdapter.setSwitched(null, null);
			Log.d(TAG, "Back pressed");
		} else {
			Log.d(TAG, "Back pressed -> FINISH");
			finish();
		}
	}

	/**
	 * On channel remove
	 */
	public void onRemoveChannel(String target)
	{
		// XXX: Implement me :)
	}
}
