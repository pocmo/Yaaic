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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableLayout.LayoutParams;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.DeckAdapter;
import org.yaaic.adapter.MessageListAdapter;
import org.yaaic.command.CommandParser;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ChannelListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;
import org.yaaic.receiver.ChannelReceiver;

/**
 * The server view with a scrollable list of all channels
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerActivity extends Activity implements ServiceConnection, ChannelListener, OnItemClickListener, OnKeyListener, OnItemSelectedListener
{
	public static final String TAG = "Yaaic/ServerActivity";
	
	private int serverId;
	private Server server;
	private IRCBinder binder;
	private ChannelReceiver receiver;
	
	private ViewSwitcher switcher;
	private Gallery deck;
	private DeckAdapter deckAdapter;
	
	/**
	 * On create
	 */
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
		((EditText) findViewById(R.id.input)).setOnKeyListener(this);
		
		deck = (Gallery) findViewById(R.id.deck);
		deck.setOnItemSelectedListener(this);
        deckAdapter = new DeckAdapter();
		deck.setAdapter(deckAdapter);
		deck.setOnItemClickListener(this);

		switcher = (ViewSwitcher) findViewById(R.id.switcher);
		
		for (Conversation conversation : server.getConversations()) {
			onNewConversation(conversation.getName());
		}
	}
	
	/**
	 * On resume
	 */
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
	
	/**
	 * On Pause
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		
		unbindService(this);
		unregisterReceiver(receiver);
	}

	/**
	 * On service connected
	 */
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		this.binder = (IRCBinder) service;
	}

	/**
	 * On service disconnected
	 */
	public void onServiceDisconnected(ComponentName name)
	{
		this.binder = null;
	}

	/**
	 * On options menu requested
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
    	super.onCreateOptionsMenu(menu);
    	
    	// inflate from xml
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.server, menu);
    	
    	return true;
	}

	/**
	 * On menu item selected
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.disconnect:
				binder.getService().getConnection(serverId).quitServer();
				server.clearConversations();
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
	public void onConversationMessage(String target)
	{
		Conversation conversation = server.getConversation(target);
		
		while(conversation.hasBufferedMessages()) {
			Message message = conversation.pollBufferedMessage();
			
			int position = deckAdapter.getPositionByName(target);
			
			if (position != -1) {
				MessageListView view = (MessageListView) deck.getChildAt(position);
				if (view != null) {
					MessageListAdapter adapter = view.getAdapter();
					adapter.addMessage(message);
				} else {
					Log.d(TAG, "MessageListView Adapter is null");
				}
			}
			
			if (deckAdapter.isSwitched()) {
				MessageListView switchedView = deckAdapter.getSwitchedView();
				switchedView.getAdapter().addMessage(message);
			}
		}
	}

	/**
	 * On new channel
	 */
	public void onNewConversation(String target)
	{
		deckAdapter.addItem(server.getConversation(target));
	}

	/**
	 * On Channel item clicked
	 */
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		Log.d(TAG, "Selected channel: " + position);
		
		Conversation conversation = deckAdapter.getItem(position);
		MessageListView canvas = deckAdapter.renderConversation(conversation, switcher);
		canvas.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		canvas.setDelegateTouchEvents(false); // Do not delegate
		deckAdapter.setSwitched(conversation.getName(), canvas);
		switcher.addView(canvas);
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
			deckAdapter.setSwitched(null, null);
		} else {
			finish();
		}
	}

	/**
	 * On channel remove
	 */
	public void onRemoveConversation(String target)
	{
		// XXX: Implement me :)
	}

	/**
	 * On key pressed (input line)
	 */
	public boolean onKey(View view, int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
			EditText input = (EditText) view;
			String text = input.getText().toString();
			input.setText("");
			
			Conversation conversation = deckAdapter.getItem(deck.getSelectedItemPosition());
			
			if (conversation != null) {
				if (!text.trim().startsWith("/")) {
					String nickname = this.binder.getService().getConnection(serverId).getNick();
					conversation.addMessage(new Message("<" + nickname + "> " + text));
					onConversationMessage(conversation.getName());
					this.binder.getService().getConnection(serverId).sendMessage(conversation.getName(), text);
				} else {
					CommandParser.getInstance().parse(text, server, conversation, binder.getService());
				}
			}
			
			return true;
		}
		return false;
	}

	/**
	 * On channel selected/focused
	 */
	public void onItemSelected(AdapterView<?> deck, View view, int position, long id)
	{
		Channel channel = (Channel) deck.getItemAtPosition(position);
		if (channel != null) {
			((TextView) findViewById(R.id.title)).setText(server.getTitle() + " - " + channel.getName());
		}
	}

	/**
	 * On no channel selected/focused
	 */
	public void onNothingSelected(AdapterView<?> arg0)
	{
		((TextView) findViewById(R.id.title)).setText(server.getTitle());
	}
}
