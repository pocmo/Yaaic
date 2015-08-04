/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2015 Sebastian Kaspari
Copyright 2012 Daniel E. Moctezuma <democtezuma@gmail.com>

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
package org.yaaic.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.activity.JoinActivity;
import org.yaaic.activity.UserActivity;
import org.yaaic.activity.UsersActivity;
import org.yaaic.activity.YaaicActivity;
import org.yaaic.adapter.ConversationPagerAdapter;
import org.yaaic.adapter.MessageListAdapter;
import org.yaaic.command.CommandParser;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCConnection;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ConversationListener;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
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
import org.yaaic.view.ConversationTabLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The server view with a scrollable pager of all conversations.
 */
public class ConversationFragment extends Fragment implements ServerListener, ConversationListener, ServiceConnection {
    public static final String TRANSACTION_TAG = "fragment_conversation";

    private static final int REQUEST_CODE_JOIN = 1;
    private static final int REQUEST_CODE_USERS = 2;
    private static final int REQUEST_CODE_USER = 3;
    private static final int REQUEST_CODE_NICK_COMPLETION= 4;

    private int serverId;
    private Server server;
    private IRCBinder binder;
    private ConversationReceiver channelReceiver;
    private ServerReceiver serverReceiver;

    private YaaicActivity activity;

    private EditText input;
    private ViewPager pager;
    private ConversationPagerAdapter pagerAdapter;
    private ConversationTabLayout tabLayout;

    private Scrollback scrollback;

    // XXX: This is ugly. This is a buffer for a channel that should be joined after showing the
    //      JoinActivity. As onActivityResult() is called before onResume() a "channel joined"
    //      broadcast may get lost as the broadcast receivers are registered in onResume() but the
    //      join command would be called in onActivityResult(). joinChannelBuffer will save the
    //      channel name in onActivityResult() and run the join command in onResume().
    private String joinChannelBuffer;

    private boolean reconnectDialogActive = false;

    private final View.OnKeyListener inputKeyListener = new View.OnKeyListener() {
        /**
         * On key pressed (input line)
         */
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
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
                sendCurrentMessage();

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

    public ConversationFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof YaaicActivity)) {
            throw new IllegalArgumentException("Activity has to implement YaaicActivity interface");
        }

        this.activity = (YaaicActivity) activity;
    }

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverId = getArguments().getInt("serverId");
        server = Yaaic.getInstance().getServerById(serverId);

        scrollback = new Scrollback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

        Settings settings = new Settings(getActivity());

        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        input = (EditText) view.findViewById(R.id.input);
        input.setOnKeyListener(inputKeyListener);

        pager = (ViewPager) view.findViewById(R.id.pager);

        pagerAdapter = new ConversationPagerAdapter(getActivity(), server);
        pager.setAdapter(pagerAdapter);

        tabLayout = new ConversationTabLayout(container.getContext());
        tabLayout.setViewPager(pager);
        tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        tabLayout.setDividerColors(getResources().getColor(R.color.divider));

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.BOTTOM;

        activity.getToolbar().addView(tabLayout, params);

        if (server.getStatus() == Status.PRE_CONNECTING) {
            server.clearConversations();
            pagerAdapter.clearConversations();
            server.getConversation(ServerInfo.DEFAULT_NAME).setHistorySize(
                settings.getHistorySize()
            );
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

        setInputTypeFlags |= InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;

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

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().length() > 0) {
                    sendCurrentMessage();
                }
            }
        });

        sendButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                doNickCompletion(input);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        activity.getToolbar().removeView(tabLayout);
    }

    /**
     * On resume
     */
    @Override
    public void onResume() {
        // register the receivers as early as possible, otherwise we may loose a broadcast message
        channelReceiver = new ConversationReceiver(server.getId(), this);
        getActivity().registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_MESSAGE));
        getActivity().registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_NEW));
        getActivity().registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_REMOVE));
        getActivity().registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_CLEAR));
        getActivity().registerReceiver(channelReceiver, new IntentFilter(Broadcast.CONVERSATION_TOPIC));

        serverReceiver = new ServerReceiver(this);
        getActivity().registerReceiver(serverReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));

        super.onResume();

        // Start service
        Intent intent = new Intent(getActivity(), IRCService.class);
        intent.setAction(IRCService.ACTION_FOREGROUND);
        getActivity().startService(intent);
        getActivity().bindService(intent, this, 0);

        input.setEnabled(server.isConnected());

        // Optimization - cache field lookup
        Collection<Conversation> mConversations = server.getConversations();
        MessageListAdapter mAdapter;

        // Fill view with messages that have been buffered while paused
        for (Conversation conversation : mConversations) {
            String name = conversation.getName();
            mAdapter = pagerAdapter.getItemAdapter(name);

            if (mAdapter != null) {
                mAdapter.addBulkMessages(conversation.getBuffer());
                conversation.clearBuffer();
            } else {
                // Was conversation created while we were paused?
                if (pagerAdapter.getPositionByName(name) == -1) {
                    onNewConversation(name);
                }
            }

            // Clear new message notifications for the selected conversation
            if (conversation.getStatus() == Conversation.STATUS_SELECTED && conversation.getNewMentions() > 0) {
                Intent ackIntent = new Intent(getActivity(), IRCService.class);
                ackIntent.setAction(IRCService.ACTION_ACK_NEW_MENTIONS);
                ackIntent.putExtra(IRCService.EXTRA_ACK_SERVERID, serverId);
                ackIntent.putExtra(IRCService.EXTRA_ACK_CONVTITLE, name);
                getActivity().startService(ackIntent);
            }
        }

        // Remove views for conversations that ended while we were paused
        int numViews = pagerAdapter.getCount();
        if (numViews > mConversations.size()) {
            for (int i = 0; i < numViews; ++i) {
                if (!mConversations.contains(pagerAdapter.getItem(i))) {
                    pagerAdapter.removeConversation(i--);
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
    public void onPause() {
        super.onPause();

        server.setIsForeground(false);

        if (binder != null && binder.getService() != null) {
            binder.getService().checkServiceStatus();
        }

        getActivity().unbindService(this);
        getActivity().unregisterReceiver(channelReceiver);
        getActivity().unregisterReceiver(serverReceiver);
    }

    /**
     * On service connected
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.binder = (IRCBinder) service;

        // connect to irc server if connect has been requested
        if (server.getStatus() == Status.PRE_CONNECTING && getArguments().containsKey("connect")) {
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
    public void onServiceDisconnected(ComponentName name) {
        this.binder = null;
    }
    /**
     * On options menu requested
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.conversations, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnect:
                server.setStatus(Status.DISCONNECTED);
                server.setMayReconnect(false);
                binder.getService().getConnection(serverId).quitServer();
                server.clearConversations();
                break;

            case R.id.close:
                Conversation conversationToClose = pagerAdapter.getItem(pager.getCurrentItem());
                // Make sure we part a channel when closing the channel conversation
                if (conversationToClose.getType() == Conversation.TYPE_CHANNEL) {
                    binder.getService().getConnection(serverId).partChannel(conversationToClose.getName());
                }
                else if (conversationToClose.getType() == Conversation.TYPE_QUERY) {
                    server.removeConversation(conversationToClose.getName());
                    onRemoveConversation(conversationToClose.getName());
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.join:
                startActivityForResult(new Intent(getActivity(), JoinActivity.class), REQUEST_CODE_JOIN);
                break;

            case R.id.users:
                Conversation conversationForUserList = pagerAdapter.getItem(pager.getCurrentItem());
                if (conversationForUserList.getType() == Conversation.TYPE_CHANNEL) {
                    Intent intent = new Intent(getActivity(), UsersActivity.class);
                    intent.putExtra(
                            Extra.USERS,
                            binder.getService().getConnection(server.getId()).getUsersAsStringArray(
                                    conversationForUserList.getName()
                            )
                    );
                    startActivityForResult(intent, REQUEST_CODE_USERS);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.only_usable_from_channel), Toast.LENGTH_SHORT).show();
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
    public Server getServer() {
        return server;
    }

    /**
     * On conversation message
     */
    @Override
    public void onConversationMessage(String target) {
        Conversation conversation = server.getConversation(target);

        if (conversation == null) {
            // In an early state it can happen that the conversation object
            // is not created yet.
            return;
        }

        MessageListAdapter adapter = pagerAdapter.getItemAdapter(target);

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
    }

    /**
     * On new conversation
     */
    @Override
    public void onNewConversation(String target) {
        createNewConversation(target);

        pager.setCurrentItem(pagerAdapter.getCount() - 1);
    }

    /**
     * Create a new conversation in the pager adapter for the
     * given target conversation.
     *
     * @param target
     */
    public void createNewConversation(String target) {
        pagerAdapter.addConversation(server.getConversation(target));

        tabLayout.update();
    }

    /**
     * On conversation remove
     */
    @Override
    public void onRemoveConversation(String target) {
        int position = pagerAdapter.getPositionByName(target);

        if (position != -1) {
            pagerAdapter.removeConversation(position);
        }

        tabLayout.update();
    }

    @Override
    public void onClearConversation(String target) {
        int position = pagerAdapter.getPositionByName(target);

        if (position != -1) {
            pagerAdapter.updateConversation(position);
        }
    }

    /**
     * On topic change
     */
    @Override
    public void onTopicChanged(String target) {
        // No implementation
    }

    /**
     * On server status update
     */
    @Override
    public void onStatusUpdate() {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
     * On activity result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            // ignore other result codes
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_JOIN:
                joinChannelBuffer = data.getExtras().getString("channel");
                break;
            case REQUEST_CODE_USERS:
                Intent intent = new Intent(getActivity(), UserActivity.class);
                intent.putExtra(Extra.USER, data.getStringExtra(Extra.USER));
                startActivityForResult(intent, REQUEST_CODE_USER);
                break;
            case REQUEST_CODE_NICK_COMPLETION:
                insertNickCompletion(input, data.getExtras().getString(Extra.USER));
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

    private void sendCurrentMessage() {
        sendMessage(input.getText().toString());

        // Workaround for a race condition in EditText
        // Instead of calling input.setText("");
        // See:
        // - https://github.com/pocmo/Yaaic/issues/67
        // - http://code.google.com/p/android/issues/detail?id=17508
        TextKeyListener.clear(input.getText());
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

        Conversation conversation = pagerAdapter.getItem(pager.getCurrentItem());

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
            while (true) {
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

        Conversation conversationForUserList = pagerAdapter.getItem(pager.getCurrentItem());

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
                Intent intent  = new Intent(getActivity(), UsersActivity.class);
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
    private void insertNickCompletion(final EditText input, String nick) {
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
                openSoftKeyboard(input);
            }
        });

        input.requestFocus();
    }

    /**
     * Open the soft keyboard (helper function)
     */
    private void openSoftKeyboard(View view) {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Remove the status char off the front of a nick if one is present
     *
     * @param nick
     * @return nick without statuschar
     */
    private String removeStatusChar(String nick) {
        /* Discard status characters */
        if (nick.startsWith("@") || nick.startsWith("+")
                || nick.startsWith("%")) {
            nick = nick.substring(1);
        }
        return nick;
    }
}
