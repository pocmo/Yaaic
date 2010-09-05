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
import java.util.Collection;
import java.util.List;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.adapter.DeckAdapter;
import org.yaaic.adapter.MessageListAdapter;
import org.yaaic.command.CommandParser;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCConnection;
import org.yaaic.irc.IRCService;
import org.yaaic.layout.NonScalingBackgroundDrawable;
import org.yaaic.listener.ConversationClickListener;
import org.yaaic.listener.ConversationListener;
import org.yaaic.listener.ConversationSelectedListener;
import org.yaaic.listener.ServerListener;
import org.yaaic.listener.SpeechClickListener;
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
import org.yaaic.view.ConversationSwitcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * The server view with a scrollable list of all channels
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationActivity extends Activity implements ServiceConnection, ServerListener, ConversationListener, OnKeyListener
{
	public static final int REQUEST_CODE_SPEECH = 99;

	private static final int REQUEST_CODE_JOIN = 1;
	private static final int REQUEST_CODE_USERS = 2;
	private static final int REQUEST_CODE_USER = 3;
	
	private int serverId;
	private Server server;
	private IRCBinder binder;
	private ConversationReceiver channelReceiver;
	private ServerReceiver serverReceiver;
	
	private ViewSwitcher switcher;
	private Gallery deck;
	private DeckAdapter deckAdapter;
	private Scrollback scrollback;
	private ConversationSwitcher dots;
	
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
		
		switcher = (ViewSwitcher) findViewById(R.id.switcher);
		
		dots = (ConversationSwitcher) findViewById(R.id.dots);
		dots.setServer(server);

        deckAdapter = new DeckAdapter();
		deck = (Gallery) findViewById(R.id.deck);
		deck.setOnItemSelectedListener(new ConversationSelectedListener(server, (TextView) findViewById(R.id.title), dots));
		deck.setAdapter(deckAdapter);
		deck.setOnItemClickListener(new ConversationClickListener(deckAdapter, switcher));
		deck.setBackgroundDrawable(new NonScalingBackgroundDrawable(this, deck, R.drawable.background));
		
		if (server.getStatus() == Status.PRE_CONNECTING) {
			server.clearConversations();
			deckAdapter.clearConversations();
		}
		
		// Check if speech recognition is available
		PackageManager pm = getPackageManager();
		Button speechButton = (Button) findViewById(R.id.speech);
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			((Button) findViewById(R.id.speech)).setOnClickListener(new SpeechClickListener(this));
		} else {
			speechButton.setVisibility(View.GONE);
		}

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
				server.setStatus(Status.DISCONNECTED);
				binder.getService().getConnection(serverId).quitServer();
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
					server.removeConversation(conversationToClose.getName());
					onRemoveConversation(conversationToClose.getName());
				} else {
					Toast.makeText(this, "You can not close the server info window", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.join:
				startActivityForResult(new Intent(this, JoinActivity.class), REQUEST_CODE_JOIN);
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
					startActivityForResult(intent, REQUEST_CODE_USERS);
				} else {
					Toast.makeText(this, getResources().getString(R.string.only_usable_from_channel), Toast.LENGTH_SHORT).show();
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
		
		conversation.setStatus(Conversation.STATUS_MESSAGE);

		if (dots != null) {
			dots.invalidate();
		}
		
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

			if (server.getStatus() == Status.CONNECTING) {
				deckAdapter.clearConversations();
				deckAdapter.addItem(server.getConversation(ServerInfo.DEFAULT_NAME));
				return;
			}

			if (!binder.getService().getSettings().isReconnectEnabled()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.reconnect_after_disconnect, server.getTitle()))
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
				.setNegativeButton(getString(R.string.negative_button), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
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
		
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
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
						Message message = new Message(getString(R.string.chat_only_form_channel));
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
		if (resultCode != RESULT_OK) {
			// ignore other result codes
			return;
		}
		
		switch (requestCode) {
			case REQUEST_CODE_SPEECH:
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				StringBuffer buffer = new StringBuffer();
				for (String partial : matches) {
					buffer.append(" ");
					buffer.append(partial);
				}
				((EditText) findViewById(R.id.input)).setText(buffer.toString().trim());
				break;
			case REQUEST_CODE_JOIN:
				joinChannelBuffer = data.getExtras().getString("channel");
				break;
			case REQUEST_CODE_USERS:
				Intent intent = new Intent(this, UserActivity.class);
				intent.putExtra(Extra.USER, data.getStringExtra(Extra.USER));
				startActivityForResult(intent, REQUEST_CODE_USER);
				break;
			case REQUEST_CODE_USER:
				final int actionId = data.getExtras().getInt(Extra.ACTION);
				final String nickname = data.getExtras().getString(Extra.USER);
				final IRCConnection connection = binder.getService().getConnection(server.getId());
				final String conversation = server.getSelectedConversation();

				// XXX: Implement me - The action should be handled after onResume()
				//                     to catch the broadcasts... now we just wait a second
				// Yes .. that's very ugly - we need some kind of queue that is handled after onResume()
				
				new Thread() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// Do nothing
						}
						
						switch (actionId) {
							case R.id.op:
								connection.op(conversation, nickname);
								break;
							case R.id.deop:
								connection.deOp(conversation, nickname);
								break;
							case R.id.voice:
								connection.voice(conversation, nickname);
								break;
							case R.id.devoice:
								connection.deVoice(conversation, nickname);
								break;
							case R.id.kick:
								connection.kick(conversation, nickname);
								break;
							case R.id.ban:
								connection.ban(conversation, nickname + "!*@*");
								break;
						}
					}
				}.start();

				break;
		}
	}
}
