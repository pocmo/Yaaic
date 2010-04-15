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

import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import org.yaaic.layout.NonScalingBackgroundDrawable;
import org.yaaic.listener.ConversationListener;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Extra;
import org.yaaic.model.Message;
import org.yaaic.model.Scrollback;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Status;
import org.yaaic.receiver.ConversationReceiver;
import org.yaaic.receiver.ServerReceiver;
import org.yaaic.view.MessageListView;

/**
 * The server view with a scrollable list of all channels
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationActivity extends Activity implements ServiceConnection, ServerListener, ConversationListener, OnItemClickListener, OnKeyListener, OnItemSelectedListener
{
	private int serverId;
	private Server server;
	private IRCBinder binder;
	private ConversationReceiver channelReceiver;
	private ServerReceiver serverReceiver;
	
	private ViewSwitcher switcher;
	private Gallery deck;
	private DeckAdapter deckAdapter;
	private Scrollback scrollback;
	
	// XXX: This is ugly. This is a buffer for a channel that should be joined after showing the
	//      JoinActivity. As onActivityResult() is called before onResume() a "channel joined"
	//      broadcast may get lost as the broadcast receivers are registered in onResume() but the
	//      join command would be called in onActivityResult(). joinChannelBuffer will save the
	//      channel name in onActivityResult() and run the join command in onResume().
	private String joinChannelBuffer;
	
	/**
	 * On create
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		serverId = getIntent().getExtras().getInt("serverId");
		server = (Server) Yaaic.getInstance().getServerById(serverId);
		setTitle("Yaaic - " + server.getTitle());
		
		setContentView(R.layout.conversations);
		
		((TextView) findViewById(R.id.title)).setText(server.getTitle());
		((EditText) findViewById(R.id.input)).setOnKeyListener(this);
		
        deckAdapter = new DeckAdapter();
		deck = (Gallery) findViewById(R.id.deck);
		deck.setOnItemSelectedListener(this);
		deck.setAdapter(deckAdapter);
		deck.setOnItemClickListener(this);
		deck.setBackgroundDrawable(new NonScalingBackgroundDrawable(this, deck, R.drawable.background));
		
		if (server.getStatus() == Status.PRE_CONNECTING) {
			server.clearConversations();
			deckAdapter.clearConversations();
		}


		switcher = (ViewSwitcher) findViewById(R.id.switcher);
		
		// Optimization : cache field lookups 
		Collection<Conversation> mConversations = server.getConversations();
		
		for (Conversation conversation : mConversations) {
			onNewConversation(conversation.getName());
		}
		
		// Create a new scrollback history
		scrollback = new Scrollback();
	}
	
	/**
	 * On resume
	 */
	@Override
	public void onResume()
	{
		// register the receivers as early as possible, otherwise we may loose a broadcast message
		channelReceiver = new ConversationReceiver(server.getId(), this);
		registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_MESSAGE));
		registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_NEW));
		registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_REMOVE));
		
		serverReceiver = new ServerReceiver(this);
		registerReceiver(serverReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));
		
		super.onResume();
		
		((ImageView) findViewById(R.id.status)).setImageResource(server.getStatusIcon());
		
		// Start service
        Intent intent = new Intent(this, IRCService.class);
        intent.setAction(IRCService.ACTION_FOREGROUND);
        startService(intent);
        bindService(intent, this, 0);
        
		if (!server.isConnected()) {
			 ((EditText) findViewById(R.id.input)).setEnabled(false);
		} else {
			 ((EditText) findViewById(R.id.input)).setEnabled(true);
		}

		// Optimization - cache field lookup
		Collection<Conversation> mConversations = server.getConversations();
		MessageListAdapter mAdapter;
		
		// Fill view with messages that have been buffered while paused
		for (Conversation conversation : mConversations) {
			mAdapter = conversation.getMessageListAdapter();
			
			if (mAdapter != null) {
				mAdapter.addBulkMessages(conversation.getBuffer());
				conversation.clearBuffer();
			}
		}
		
		// Join channel that has been selected in JoinActivity (onActivityResult())
		if (joinChannelBuffer != null) {
			new Thread() {
				public void run() {
					binder.getService().getConnection(serverId).joinChannel(joinChannelBuffer);
					joinChannelBuffer = null;
				}
			}.start();
		}
	}
	
	/**
	 * On Pause
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		
		if (binder != null && binder.getService() != null) {
			binder.getService().checkServiceStatus();
		}
		
		/*if (!binder.getService().hasConnections()) {
			Log.d("Yaaic", "Stopping service");
			//binder.getService().stopSelf();
		} else {
			Log.d("Yaaic", "Unbinding service");
		}*/

		unbindService(this);
		unregisterReceiver(channelReceiver);
		unregisterReceiver(serverReceiver);
	}

	/**
	 * On service connected
	 */
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		this.binder = (IRCBinder) service;
		
		// connect to irc server if connect has been requested
		if (server.getStatus() == Status.PRE_CONNECTING && getIntent().hasExtra("connect")) {
			server.setStatus(Status.CONNECTING);
			binder.connect(server);
		}
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
    	inflater.inflate(R.menu.conversations, menu);
    	
    	return true;
	}
	
	/**
	 * On prepare options menu
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.getItem(0).setEnabled(server.isConnected()); // join
		menu.getItem(1).setEnabled(server.isConnected()); // users
		menu.getItem(2).setEnabled(server.isConnected()); // close
		
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
				server.setStatus(Status.DISCONNECTED);
				server.clearConversations();
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.close:
				Conversation conversationToClose = deckAdapter.getItem(deck.getSelectedItemPosition());
				// Make sure we part a channel when closing the channel conversation
				if (conversationToClose.getType() == Conversation.TYPE_CHANNEL) {
					binder.getService().getConnection(serverId).partChannel(conversationToClose.getName());
				}
				else if (conversationToClose.getType() == Conversation.TYPE_QUERY) {
					onRemoveConversation(conversationToClose.getName());
				} else {
					Toast.makeText(this, "You can not close the server info window", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.join:
				startActivityForResult(new Intent(this, JoinActivity.class), 0);
				break;
			case R.id.users:
				Conversation conversationForUserList = deckAdapter.getItem(deck.getSelectedItemPosition());
				if (conversationForUserList.getType() == Conversation.TYPE_CHANNEL) {
					Intent intent = new Intent(this, UsersActivity.class);
					intent.putExtra(
						Extra.USERS,
						binder.getService().getConnection(server.getId()).getUsersAsStringArray(
							conversationForUserList.getName()
						)
					);
					startActivity(intent);
				} else {
					Toast.makeText(this, "Only usable from within a channel", Toast.LENGTH_SHORT).show();
				}
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
	 * On conversation message
	 */
	public void onConversationMessage(String target)
	{
		Conversation conversation = server.getConversation(target);
		MessageListAdapter adapter = conversation.getMessageListAdapter();
		
		while(conversation.hasBufferedMessages()) {
			Message message = conversation.pollBufferedMessage();
			
			if (adapter != null) {
				adapter.addMessage(message);
			}
		}
	}

	/**
	 * On new conversation
	 */
	public void onNewConversation(String target)
	{
		deckAdapter.addItem(server.getConversation(target));
		
		if (!deckAdapter.isSwitched()) {
			// Scroll to new conversation
			deck.setSelection(deckAdapter.getCount() - 1);
		}
	}
	
	/**
	 * On conversation remove
	 */
	public void onRemoveConversation(String target)
	{
		deckAdapter.removeItem(target);
		
		if (deckAdapter.isSwitched()) {
			switcher.showNext();
			switcher.removeView(deckAdapter.getSwitchedView());
			deckAdapter.setSwitched(null, null);
		}
	}

	/**
	 * On server status update
	 */
	public void onStatusUpdate()
	{
		((ImageView) findViewById(R.id.status)).setImageResource(server.getStatusIcon());
		
		EditText input = (EditText) findViewById(R.id.input);
		if (server.isConnected()) {
			input.setEnabled(true); 
		} else {
			input.setEnabled(false);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You disconnected from " + server.getTitle() + ". Do you want to reconnect?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					binder.getService().getConnection(server.getId()).setAutojoinChannels(
						server.getCurrentChannelNames()
					);
					server.clearConversations();
					deckAdapter.clearConversations();
					deckAdapter.addItem(server.getConversation(ServerInfo.DEFAULT_NAME));
					binder.connect(server);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	/**
	 * On conversation item clicked
	 */
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		Conversation conversation = deckAdapter.getItem(position);
		
		MessageListView canvas = deckAdapter.renderConversation(conversation, switcher);
		canvas.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		canvas.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		canvas.setDelegateTouchEvents(false); // Do not delegate
		
		deckAdapter.setSwitched(conversation.getName(), canvas);
		switcher.addView(canvas);
		switcher.showNext();
	}
	
	/**
	 * On key down
	 *
	 * XXX: As we only track the back key: Android >= 2.0 will call a method called onBackPressed()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (deckAdapter.isSwitched()) {
				switcher.showNext();
				switcher.removeView(deckAdapter.getSwitchedView());
				deckAdapter.setSwitched(null, null);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * On key pressed (input line)
	 */
	public boolean onKey(View view, int keyCode, KeyEvent event)
	{
		EditText input = (EditText) view;
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
			String message = scrollback.goBack();
			if (message != null) {
				input.setText(message);
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
			String message = scrollback.goForward();
			if (message != null) {
				input.setText(message);
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
			if (!server.isConnected()) {
				Message message = new Message("Not connected");
				message.setColor(Message.COLOR_RED);
				message.setIcon(R.drawable.error);
				server.getConversation(server.getSelectedConversation()).addMessage(message);
				onConversationMessage(server.getSelectedConversation());
			}
			
			String text = input.getText().toString();
			input.setText("");
			
			if (text.equals("")) {
				// ignore empty messages
				return true;
			}
			
			scrollback.addMessage(text);
			
			Conversation conversation = deckAdapter.getItem(deck.getSelectedItemPosition());
			
			if (conversation != null) {
				if (!text.trim().startsWith("/")) {
					if (conversation.getType() != Conversation.TYPE_SERVER) {
						String nickname = binder.getService().getConnection(serverId).getNick();
						conversation.addMessage(new Message("<" + nickname + "> " + text));
						binder.getService().getConnection(serverId).sendMessage(conversation.getName(), text);
					} else {
						Message message = new Message("You can only chat from within a channel or a query");
						message.setColor(Message.COLOR_YELLOW);
						message.setIcon(R.drawable.warning);
						conversation.addMessage(message);
					}
					onConversationMessage(conversation.getName());
				} else {
					CommandParser.getInstance().parse(text, server, conversation, binder.getService());
				}
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * On activity result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// currently there's only the "join channel" activity
		if (resultCode == RESULT_OK) {
			joinChannelBuffer = data.getExtras().getString("channel");
		}
	}

	/**
	 * On channel selected/focused
	 */
	public void onItemSelected(AdapterView<?> deck, View view, int position, long id)
	{
		Conversation conversation = (Conversation) deck.getItemAtPosition(position);
		
		if (conversation != null && conversation.getType() != Conversation.TYPE_SERVER) {
			((TextView) findViewById(R.id.title)).setText(server.getTitle() + " - " + conversation.getName());
		} else {
			onNothingSelected(deck);
		}
		
		// Remember selection
		if (conversation != null) {
			server.setSelectedConversation(conversation.getName());
		}
	}

	/**
	 * On no channel selected/focused
	 */
	public void onNothingSelected(AdapterView<?> deck)
	{
		((TextView) findViewById(R.id.title)).setText(server.getTitle());
	}
}
