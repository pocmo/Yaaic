/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

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
import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Extra;
import org.yaaic.model.Message;
import org.yaaic.model.Query;
import org.yaaic.model.Scrollback;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Settings;
import org.yaaic.model.Status;
import org.yaaic.model.User;
import org.yaaic.receiver.ConversationReceiver;
import org.yaaic.receiver.ServerReceiver;
import org.yaaic.view.ConversationSwitcher;
import org.yaaic.view.MessageListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
public class ConversationActivity extends Activity implements ServiceConnection, ServerListener, ConversationListener
{
    public static final int REQUEST_CODE_SPEECH = 99;

    private static final int REQUEST_CODE_JOIN = 1;
    private static final int REQUEST_CODE_USERS = 2;
    private static final int REQUEST_CODE_USER = 3;
    private static final int REQUEST_CODE_NICK_COMPLETION= 4;

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

    private int historySize;

    private boolean reconnectDialogActive = false;

    OnKeyListener inputKeyListener = new OnKeyListener() {
        /**
         * On key pressed (input line)
         */
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event)
        {
            EditText input = (EditText) view;

            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                String message = scrollback.goBack();
                if (message != null) {
                    input.setText(message);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                String message = scrollback.goForward();
                if (message != null) {
                    input.setText(message);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage(input.getText().toString());
                input.setText("");
                return true;
            }

            // Nick completion
            if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                doNickCompletion(input);
                return true;
            }

            return false;
        }
    };

    /**
     * On create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        serverId = getIntent().getExtras().getInt("serverId");
        server = Yaaic.getInstance().getServerById(serverId);
        Settings settings = new Settings(this);

        // Finish activity if server does not exist anymore - See #55
        if (server == null) {
            this.finish();
        }

        setTitle("Yaaic - " + server.getTitle());

        setContentView(R.layout.conversations);
        if (settings.fullscreenConversations()){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        ((TextView) findViewById(R.id.title)).setText(server.getTitle());

        EditText input = (EditText) findViewById(R.id.input);
        input.setOnKeyListener(inputKeyListener);

        switcher = (ViewSwitcher) findViewById(R.id.switcher);

        dots = (ConversationSwitcher) findViewById(R.id.dots);
        dots.setServer(server);

        deckAdapter = new DeckAdapter();
        deck = (Gallery) findViewById(R.id.deck);
        deck.setOnItemSelectedListener(new ConversationSelectedListener(this, server, (TextView) findViewById(R.id.title), dots));
        deck.setAdapter(deckAdapter);
        deck.setOnItemClickListener(new ConversationClickListener(deckAdapter, switcher));
        deck.setBackgroundDrawable(new NonScalingBackgroundDrawable(this, deck, R.drawable.background));

        historySize = settings.getHistorySize();

        if (server.getStatus() == Status.PRE_CONNECTING) {
            server.clearConversations();
            deckAdapter.clearConversations();
            server.getConversation(ServerInfo.DEFAULT_NAME).setHistorySize(historySize);
        }

        // Optimization : cache field lookups
        Collection<Conversation> mConversations = server.getConversations();

        for (Conversation conversation : mConversations) {
            // Only scroll to new conversation if it was selected before
            if (conversation.getStatus() == Conversation.STATUS_SELECTED) {
                onNewConversation(conversation.getName());
            } else {
                createNewConversation(conversation.getName());
            }
        }

        int setInputTypeFlags = 0;

        if (settings.autoCorrectText()) {
            setInputTypeFlags |= InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        } else {
            // keep compatibility with api level 3
            if ((android.os.Build.VERSION.SDK.charAt(0) - '0') >= 5) {
                setInputTypeFlags |= 0x80000; // InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
        }
        if (settings.autoCapSentences()) {
            setInputTypeFlags |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        }

        if (isLandscape && settings.imeExtract()) {
            setInputTypeFlags |= InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE;
        }

        if (!settings.imeExtract()) {
            input.setImeOptions(input.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        }

        input.setInputType(input.getInputType() | setInputTypeFlags);

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
        registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_TOPIC));

        serverReceiver = new ServerReceiver(this);
        registerReceiver(serverReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));

        super.onResume();

        // Check if speech recognition is enabled and available
        if (new Settings(this).isVoiceRecognitionEnabled()) {
            PackageManager pm = getPackageManager();
            Button speechButton = (Button) findViewById(R.id.speech);
            List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                ((Button) findViewById(R.id.speech)).setOnClickListener(new SpeechClickListener(this));
                speechButton.setVisibility(View.VISIBLE);
            }
        }

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
            String name = conversation.getName();
            mAdapter = deckAdapter.getItemAdapter(name);

            if (mAdapter != null) {
                mAdapter.addBulkMessages(conversation.getBuffer());
                conversation.clearBuffer();
            } else {
                // Was conversation created while we were paused?
                if (deckAdapter.getPositionByName(name) == -1) {
                    onNewConversation(name);
                }
            }

            // Clear new message notifications for the selected conversation
            if (conversation.getStatus() == Conversation.STATUS_SELECTED && conversation.getNewMentions() > 0) {
                Intent ackIntent = new Intent(this, IRCService.class);
                ackIntent.setAction(IRCService.ACTION_ACK_NEW_MENTIONS);
                ackIntent.putExtra(IRCService.EXTRA_ACK_SERVERID, serverId);
                ackIntent.putExtra(IRCService.EXTRA_ACK_CONVTITLE, name);
                startService(ackIntent);
            }
        }

        // Remove views for conversations that ended while we were paused
        int numViews = deckAdapter.getCount();
        if (numViews > mConversations.size()) {
            for (int i = 0; i < numViews; ++i) {
                if (!mConversations.contains(deckAdapter.getItem(i))) {
                    deckAdapter.removeItem(i--);
                    --numViews;
                }
            }
        }

        // Join channel that has been selected in JoinActivity (onActivityResult())
        if (joinChannelBuffer != null) {
            new Thread() {
                @Override
                public void run() {
                    binder.getService().getConnection(serverId).joinChannel(joinChannelBuffer);
                    joinChannelBuffer = null;
                }
            }.start();
        }

        server.setIsForeground(true);
    }

    /**
     * On Pause
     */
    @Override
    public void onPause()
    {
        super.onPause();

        server.setIsForeground(false);

        if (binder != null && binder.getService() != null) {
            binder.getService().checkServiceStatus();
        }

        unbindService(this);
        unregisterReceiver(channelReceiver);
        unregisterReceiver(serverReceiver);
    }

    /**
     * On save instance state (e.g. before a configuration change)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (deckAdapter.isSwitched()) {
            outState.putBoolean("isSwitched", deckAdapter.isSwitched());
            outState.putString("switchedName", deckAdapter.getSwitchedName());
        }
    }

    /**
     * On restore instance state (e.g. after a configuration change)
     */
    @Override
    protected void onRestoreInstanceState(Bundle inState)
    {
        super.onRestoreInstanceState(inState);

        if (inState.getBoolean("isSwitched")) {
            deckAdapter.setSwitched(inState.getString("switchedName"), null);
        }
    }

    /**
     * On service connected
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        this.binder = (IRCBinder) service;

        // connect to irc server if connect has been requested
        if (server.getStatus() == Status.PRE_CONNECTING && getIntent().hasExtra("connect")) {
            server.setStatus(Status.CONNECTING);
            binder.connect(server);
        } else {
            onStatusUpdate();
        }
    }

    /**
     * On service disconnected
     */
    @Override
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
                server.setMayReconnect(false);
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
                    Toast.makeText(this, getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
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
    @Override
    public void onConversationMessage(String target)
    {
        Conversation conversation = server.getConversation(target);

        if (conversation == null) {
            // In an early state it can happen that the conversation object
            // is not created yet.
            return;
        }

        MessageListAdapter adapter = deckAdapter.getItemAdapter(target);

        while(conversation.hasBufferedMessages()) {
            Message message = conversation.pollBufferedMessage();

            if (adapter != null && message != null) {
                adapter.addMessage(message);
                int status;

                switch (message.getType())
                {
                    case Message.TYPE_MISC:
                        status = Conversation.STATUS_MISC;
                        break;

                    default:
                        status = Conversation.STATUS_MESSAGE;
                        break;
                }
                conversation.setStatus(status);
            }
        }

        if (dots != null) {
            dots.invalidate();
        }
    }

    /**
     * On new conversation
     */
    @Override
    public void onNewConversation(String target)
    {
        createNewConversation(target);

        if (!deckAdapter.isSwitched()) {
            // Scroll to new conversation
            deck.setSelection(deckAdapter.getCount() - 1);
        }
    }
    public void createNewConversation(String target)
    {
        deckAdapter.addItem(server.getConversation(target));
    }

    /**
     * On conversation remove
     */
    @Override
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
     * On topic change
     */
    public void onTopicChanged(String target)
    {
        String selected = server.getSelectedConversation();
        if (selected.equals(target)) {
            // onTopicChanged is only called for channels
            Channel channel = (Channel) server.getConversation(selected);
            StringBuilder sb = new StringBuilder();
            sb.append(server.getTitle() + " - " + channel.getName());
            if (!(channel.getTopic()).equals("")) {
                sb.append(" - " + channel.getTopic());
            }
            ((TextView) findViewById(R.id.title)).setText(sb.toString());
        }
    }

    /**
     * On server status update
     */
    @Override
    public void onStatusUpdate()
    {
        ((ImageView) findViewById(R.id.status)).setImageResource(server.getStatusIcon());

        EditText input = (EditText) findViewById(R.id.input);

        if (server.isConnected()) {
            input.setEnabled(true);
        } else {
            input.setEnabled(false);

            if (server.getStatus() == Status.CONNECTING) {
                return;
            }

            // Service is not connected or initialized yet - See #54
            if (binder == null || binder.getService() == null || binder.getService().getSettings() == null) {
                return;
            }

            if (!binder.getService().getSettings().isReconnectEnabled() && !reconnectDialogActive) {
                reconnectDialogActive = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.reconnect_after_disconnect, server.getTitle()))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (!server.isDisconnected()) {
                            reconnectDialogActive = false;
                            return;
                        }
                        binder.getService().getConnection(server.getId()).setAutojoinChannels(
                            server.getCurrentChannelNames()
                        );
                        server.setStatus(Status.CONNECTING);
                        binder.connect(server);
                        reconnectDialogActive = false;
                    }
                })
                .setNegativeButton(getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        server.setMayReconnect(false);
                        reconnectDialogActive = false;
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
                MessageListView canvas = (MessageListView) deckAdapter.getView(deckAdapter.getPositionByName(deckAdapter.getSwitchedName()), null, switcher);
                canvas.setSwitched(false);
                deckAdapter.setSwitched(null, null);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
                if (matches.size() > 0) {
                    ((EditText) findViewById(R.id.input)).setText(matches.get(0));
                }
                break;
            case REQUEST_CODE_JOIN:
                joinChannelBuffer = data.getExtras().getString("channel");
                break;
            case REQUEST_CODE_USERS:
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra(Extra.USER, data.getStringExtra(Extra.USER));
                startActivityForResult(intent, REQUEST_CODE_USER);
                break;
            case REQUEST_CODE_NICK_COMPLETION:
                insertNickCompletion((EditText) findViewById(R.id.input), data.getExtras().getString(Extra.USER));
                break;
            case REQUEST_CODE_USER:
                final int actionId = data.getExtras().getInt(Extra.ACTION);
                final String nickname = data.getExtras().getString(Extra.USER);
                final IRCConnection connection = binder.getService().getConnection(server.getId());
                final String conversation = server.getSelectedConversation();
                final Handler handler = new Handler();

                // XXX: Implement me - The action should be handled after onResume()
                //                     to catch the broadcasts... now we just wait a second
                // Yes .. that's very ugly - we need some kind of queue that is handled after onResume()

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }

                        String nicknameWithoutPrefix = nickname;

                        while (
                            nicknameWithoutPrefix.startsWith("@") ||
                            nicknameWithoutPrefix.startsWith("+") ||
                            nicknameWithoutPrefix.startsWith(".") ||
                            nicknameWithoutPrefix.startsWith("%")
                        ) {
                            // Strip prefix(es) now
                            nicknameWithoutPrefix = nicknameWithoutPrefix.substring(1);
                        }

                        switch (actionId) {
                            case User.ACTION_REPLY:
                                final String replyText = nicknameWithoutPrefix + ": ";
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        EditText input = (EditText) findViewById(R.id.input);
                                        input.setText(replyText);
                                        input.setSelection(replyText.length());
                                    }
                                });
                                break;
                            case User.ACTION_QUERY:
                                Conversation query = server.getConversation(nicknameWithoutPrefix);
                                if (query == null) {
                                    // Open a query if there's none yet
                                    query = new Query(nicknameWithoutPrefix);
                                    query.setHistorySize(binder.getService().getSettings().getHistorySize());
                                    server.addConversation(query);

                                    Intent intent = Broadcast.createConversationIntent(
                                        Broadcast.CONVERSATION_NEW,
                                        server.getId(),
                                        nicknameWithoutPrefix
                                    );
                                    binder.getService().sendBroadcast(intent);
                                }
                                break;
                            case User.ACTION_OP:
                                connection.op(conversation, nicknameWithoutPrefix);
                                break;
                            case User.ACTION_DEOP:
                                connection.deOp(conversation, nicknameWithoutPrefix);
                                break;
                            case User.ACTION_VOICE:
                                connection.voice(conversation, nicknameWithoutPrefix);
                                break;
                            case User.ACTION_DEVOICE:
                                connection.deVoice(conversation, nicknameWithoutPrefix);
                                break;
                            case User.ACTION_KICK:
                                connection.kick(conversation, nicknameWithoutPrefix);
                                break;
                            case User.ACTION_BAN:
                                connection.ban(conversation, nicknameWithoutPrefix + "!*@*");
                                break;
                        }
                    }
                }.start();

                break;
        }
    }

    /**
     * Send a message in this conversation
     *
     * @param text The text of the message
     */
    private void sendMessage(String text) {
        if (text.equals("")) {
            // ignore empty messages
            return;
        }

        if (!server.isConnected()) {
            Message message = new Message(getString(R.string.message_not_connected));
            message.setColor(Message.COLOR_RED);
            message.setIcon(R.drawable.error);
            server.getConversation(server.getSelectedConversation()).addMessage(message);
            onConversationMessage(server.getSelectedConversation());
        }

        scrollback.addMessage(text);

        Conversation conversation = deckAdapter.getItem(deck.getSelectedItemPosition());

        if (conversation != null) {
            if (!text.trim().startsWith("/")) {
                if (conversation.getType() != Conversation.TYPE_SERVER) {
                    String nickname = binder.getService().getConnection(serverId).getNick();
                    //conversation.addMessage(new Message("<" + nickname + "> " + text));
                    conversation.addMessage(new Message(text, nickname));
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
    }

    /**
     * Complete a nick in the input line
     */
    private void doNickCompletion(EditText input) {
        String text = input.getText().toString();

        if (text.length() <= 0) {
            return;
        }

        String[] tokens = text.split("[\\s,.-]+");

        if (tokens.length <= 0) {
            return;
        }

        String word = tokens[tokens.length - 1].toLowerCase();
        tokens[tokens.length - 1] = null;

        int begin   = input.getSelectionStart();
        int end     = input.getSelectionEnd();
        int cursor  = Math.min(begin, end);
        int sel_end = Math.max(begin, end);

        boolean in_selection = (cursor != sel_end);

        if (in_selection) {
            word = text.substring(cursor, sel_end);
        } else {
            // use the word at the curent cursor position
            while(true) {
                cursor -= 1;
                if (cursor <= 0 || text.charAt(cursor) == ' ') {
                    break;
                }
            }

            if (cursor < 0) {
                cursor = 0;
            }

            if (text.charAt(cursor) == ' ') {
                cursor += 1;
            }

            sel_end = text.indexOf(' ', cursor);

            if (sel_end == -1) {
                sel_end = text.length();
            }

            word = text.substring(cursor, sel_end);
        }
        // Log.d("Yaaic", "Trying to complete nick: " + word);

        Conversation conversationForUserList = deckAdapter.getItem(deck.getSelectedItemPosition());

        String[] users = null;

        if (conversationForUserList.getType() == Conversation.TYPE_CHANNEL) {
            users = binder.getService().getConnection(server.getId()).getUsersAsStringArray(
                conversationForUserList.getName()
            );
        }

        // go through users and add matches
        if (users != null) {
            List<Integer> result = new ArrayList<Integer>();

            for (int i = 0; i < users.length; i++) {
                String nick = removeStatusChar(users[i].toLowerCase());
                if (nick.startsWith(word.toLowerCase())) {
                    result.add(Integer.valueOf(i));
                }
            }

            if (result.size() == 1) {
                input.setSelection(cursor, sel_end);
                insertNickCompletion(input, users[result.get(0).intValue()]);
            } else if (result.size() > 0) {
                Intent intent  = new Intent(this, UsersActivity.class);
                String[] extra = new String[result.size()];
                int i = 0;

                for (Integer n : result) {
                    extra[i++] = users[n.intValue()];
                }

                input.setSelection(cursor, sel_end);
                intent.putExtra(Extra.USERS, extra);
                startActivityForResult(intent, REQUEST_CODE_NICK_COMPLETION);
            }
        }
    }

    /**
     * Insert a given nick completion into the input line
     *
     * @param input The input line widget, with the incomplete nick selected
     * @param nick The completed nick
     */
    private void insertNickCompletion(EditText input, String nick) {
        int start = input.getSelectionStart();
        int end  = input.getSelectionEnd();
        nick = removeStatusChar(nick);

        if (start == 0) {
            nick += ":";
        }

        nick += " ";
        input.getText().replace(start, end, nick, 0, nick.length());
        // put cursor after inserted text
        input.setSelection(start + nick.length());
        input.clearComposingText();
        input.post(new Runnable() {
            @Override
            public void run() {
                // make the softkeyboard come up again (only if no hw keyboard is attached)
                EditText input = (EditText) findViewById(R.id.input);
                openSoftKeyboard(input);
            }
        });
        input.requestFocus();
    }

    /**
     * Open the soft keyboard (helper function)
     */
    private void openSoftKeyboard(View view) {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Remove the status char off the front of a nick if one is present
     * 
     * @param nick
     * @return nick without statuschar
     */
    private String removeStatusChar(String nick)
    {
        /* Discard status characters */
        if (nick.startsWith("@") || nick.startsWith("+")
            || nick.startsWith("%")) {
            nick = nick.substring(1);
        }
        return nick;
    }
}
