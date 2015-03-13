/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari
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
package org.yaaic.irc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.activity.ServersActivity;
import org.yaaic.db.Database;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Settings;
import org.yaaic.model.Status;
import org.yaaic.receiver.ReconnectReceiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

/**
 * The background service for managing the irc connections
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCService extends Service
{
    public static final String ACTION_FOREGROUND = "org.yaaic.service.foreground";
    public static final String ACTION_BACKGROUND = "org.yaaic.service.background";
    public static final String ACTION_ACK_NEW_MENTIONS = "org.yaaic.service.ack_new_mentions";
    public static final String EXTRA_ACK_SERVERID = "org.yaaic.service.ack_serverid";
    public static final String EXTRA_ACK_CONVTITLE = "org.yaaic.service.ack_convtitle";

    private static final int FOREGROUND_NOTIFICATION = 1;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int NOTIFICATION_LED_ON_MS = 300;
    private static final int NOTIFICATION_LED_COLOR = 0xff00ff00;

    @SuppressWarnings("rawtypes")
    private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
    @SuppressWarnings("rawtypes")
    private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
    @SuppressWarnings("rawtypes")
    private static final Class[] mSetForegroudSignaure = new Class[] { boolean.class };

    private final IRCBinder binder;
    private final HashMap<Integer, IRCConnection> connections;
    private boolean foreground = false;
    private final ArrayList<String> connectedServerTitles;
    private final LinkedHashMap<String, Conversation> mentions;
    private int newMentions = 0;

    private NotificationManager notificationManager;
    private Method mStartForeground;
    private Method mStopForeground;
    private final Object[] mStartForegroundArgs = new Object[2];
    private final Object[] mStopForegroundArgs = new Object[1];
    private Notification notification;
    private Settings settings;

    private HashMap<Integer, PendingIntent> alarmIntents;
    private HashMap<Integer, ReconnectReceiver> alarmReceivers;
    private final Object alarmIntentsLock;

    /**
     * Create new service
     */
    public IRCService()
    {
        super();

        this.connections = new HashMap<Integer, IRCConnection>();
        this.binder = new IRCBinder(this);
        this.connectedServerTitles = new ArrayList<String>();
        this.mentions = new LinkedHashMap<String, Conversation>();
        this.alarmIntents = new HashMap<Integer, PendingIntent>();
        this.alarmReceivers = new HashMap<Integer, ReconnectReceiver>();
        this.alarmIntentsLock = new Object();
    }

    /**
     * On create
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        settings = new Settings(getBaseContext());
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }

        // Load servers from Database
        Database db = new Database(this);
        Yaaic.getInstance().setServers(db.getServers());
        db.close();

        // Broadcast changed server list
        sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
    }

    /**
     * Get Settings object
     *
     * @return the settings helper object
     */
    public Settings getSettings()
    {
        return settings;
    }

    /**
     * On start (will be called on pre-2.0 platform. On 2.0 or later onStartCommand()
     * will be called)
     */
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        handleCommand(intent);
    }

    /**
     * On start command (Android >= 2.0)
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null) {
            handleCommand(intent);
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //return START_STICKY;
        return 1;
    }


    /**
     * Handle command
     *
     * @param intent
     */
    private void handleCommand(Intent intent)
    {
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            if (foreground) {
                return; // XXX: We are already in foreground...
            }
            foreground = true;

            // Set the icon, scrolling text and timestamp
            notification = new Notification(R.drawable.icon, getText(R.string.notification_running), System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this notification
            Intent notifyIntent = new Intent(this, ServersActivity.class);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, getText(R.string.app_name), getText(R.string.notification_not_connected), contentIntent);

            startForegroundCompat(FOREGROUND_NOTIFICATION, notification);
        } else if (ACTION_BACKGROUND.equals(intent.getAction()) && !foreground) {
            stopForegroundCompat(FOREGROUND_NOTIFICATION);
        } else if (ACTION_ACK_NEW_MENTIONS.equals(intent.getAction())) {
            ackNewMentions(intent.getIntExtra(EXTRA_ACK_SERVERID, -1), intent.getStringExtra(EXTRA_ACK_CONVTITLE));
        }
    }

    /**
     * Update notification and vibrate and/or flash a LED light if needed
     *
     * @param text       The ticker text to display
     * @param contentText       The text to display in the notification dropdown
     * @param vibrate True if the device should vibrate, false otherwise
     * @param sound True if the device should make sound, false otherwise
     * @param light True if the device should flash a LED light, false otherwise
     */
    private void updateNotification(String text, String contentText, boolean vibrate, boolean sound, boolean light)
    {
        if (foreground) {
            notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
            Intent notifyIntent = new Intent(this, ServersActivity.class);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

            if (contentText == null) {
                if (newMentions >= 1) {
                    StringBuilder sb = new StringBuilder();
                    for (Conversation conv : mentions.values()) {
                        sb.append(conv.getName() + " (" + conv.getNewMentions() + "), ");
                    }
                    contentText = getString(R.string.notification_mentions, sb.substring(0, sb.length()-2));
                } else if (!connectedServerTitles.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String title : connectedServerTitles) {
                        sb.append(title + ", ");
                    }
                    contentText = getString(R.string.notification_connected, sb.substring(0, sb.length()-2));
                } else {
                    contentText = getString(R.string.notification_not_connected);
                }
            }

            notification.setLatestEventInfo(this, getText(R.string.app_name), contentText, contentIntent);

            if (vibrate) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            if (sound) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }

            if (light) {
                notification.ledARGB   = NOTIFICATION_LED_COLOR;
                notification.ledOnMS   = NOTIFICATION_LED_ON_MS;
                notification.ledOffMS  = NOTIFICATION_LED_OFF_MS;
                notification.flags    |= Notification.FLAG_SHOW_LIGHTS;
            }

            notification.number = newMentions;

            notificationManager.notify(FOREGROUND_NOTIFICATION, notification);
        }
    }

    /**
     * Generates a string uniquely identifying a conversation.
     */
    public String getConversationId(int serverId, String title) {
        return "" + serverId + ":" + title;
    }

    /**
     * Notify the service of a new mention (updates the status bar notification)
     *
     * @param conversation The conversation where the new mention occurred
     * @param msg The text of the new message
     * @param vibrate Whether the notification should include vibration
     * @param sound Whether the notification should include sound
     * @param light Whether the notification should include a flashing LED light
     */
    public synchronized void addNewMention(int serverId, Conversation conversation, String msg, boolean vibrate, boolean sound, boolean light)
    {
        if (conversation == null) {
            return;
        }

        conversation.addNewMention();
        ++newMentions;
        String convId = getConversationId(serverId, conversation.getName());
        if (!mentions.containsKey(convId)) {
            mentions.put(convId, conversation);
        }

        if (newMentions == 1) {
            updateNotification(msg, msg, vibrate, sound, light);
        } else {
            updateNotification(msg, null, vibrate, sound, light);
        }
    }

    /**
     * Notify the service that new mentions have been viewed (updates the status bar notification)
     *
     * @param convTitle The title of the conversation whose new mentions have been read
     */
    public synchronized void ackNewMentions(int serverId, String convTitle)
    {
        if (convTitle == null) {
            return;
        }

        Conversation conversation = mentions.remove(getConversationId(serverId, convTitle));
        if (conversation == null) {
            return;
        }
        newMentions -= conversation.getNewMentions();
        conversation.clearNewMentions();
        if (newMentions < 0) {
            newMentions = 0;
        }

        updateNotification(null, null, false, false, false);
    }

    /**
     * Notify the service of connection to a server (updates the status bar notification)
     *
     * @param title The title of the newly connected server
     */
    public synchronized void notifyConnected(String title)
    {
        connectedServerTitles.add(title);
        updateNotification(getString(R.string.notification_connected, title), null, false, false, false);
    }

    /**
     * Notify the service of disconnection from a server (updates the status bar notification)
     *
     * @param title The title of the disconnected server
     */
    public synchronized void notifyDisconnected(String title)
    {
        connectedServerTitles.remove(title);
        updateNotification(getString(R.string.notification_disconnected, title), null, false, false, false);
    }


    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    private void startForegroundCompat(int id, Notification notification)
    {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
            } catch (IllegalAccessException e) {
                // Should not happen.
            }
        } else {
            // Fall back on the old API.
            try {
                Method setForeground = getClass().getMethod("setForeground", mSetForegroudSignaure);
                setForeground.invoke(this, new Object[] { true });
            } catch (NoSuchMethodException exception) {
                // Should not happen
            } catch (InvocationTargetException e) {
                // Should not happen.
            } catch (IllegalAccessException e) {
                // Should not happen.
            }

            notificationManager.notify(id, notification);
        }
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    public void stopForegroundCompat(int id)
    {
        foreground = false;

        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
            } catch (IllegalAccessException e) {
                // Should not happen.
            }
        } else {
            // Fall back on the old API.  Note to cancel BEFORE changing the
            // foreground state, since we could be killed at that point.
            notificationManager.cancel(id);

            try {
                Method setForeground = getClass().getMethod("setForeground", mSetForegroudSignaure);
                setForeground.invoke(this, new Object[] { true });
            } catch (NoSuchMethodException exception) {
                // Should not happen
            } catch (InvocationTargetException e) {
                // Should not happen.
            } catch (IllegalAccessException e) {
                // Should not happen.
            }
        }
    }

    /**
     * Connect to the given server
     */
    public void connect(final Server server)
    {
        final int serverId = server.getId();
        final int reconnectInterval = settings.getReconnectInterval()*60000;
        final IRCService service = this;

        if (settings.isReconnectEnabled()) {
            server.setMayReconnect(true);
        }

        new Thread("Connect thread for " + server.getTitle()) {
            @Override
            public void run() {
                synchronized(alarmIntentsLock) {
                    alarmIntents.remove(serverId);
                    ReconnectReceiver lastReceiver = alarmReceivers.remove(serverId);
                    if (lastReceiver != null) {
                        unregisterReceiver(lastReceiver);
                    }
                }

                if (settings.isReconnectEnabled() && !server.mayReconnect()) {
                    return;
                }

                try {
                    IRCConnection connection = getConnection(serverId);

                    connection.setNickname(server.getIdentity().getNickname());
                    connection.setAliases(server.getIdentity().getAliases());
                    connection.setIdent(server.getIdentity().getIdent());
                    connection.setRealName(server.getIdentity().getRealName());
                    connection.setUseSSL(server.useSSL());

                    if (server.getCharset() != null) {
                        connection.setEncoding(server.getCharset());
                    }

                    if (server.getAuthentication().hasSaslCredentials()) {
                        connection.setSaslCredentials(
                            server.getAuthentication().getSaslUsername(),
                            server.getAuthentication().getSaslPassword()
                            );
                    }

                    if (server.getPassword() != "") {
                        connection.connect(server.getHost(), server.getPort(), server.getPassword());
                    } else {
                        connection.connect(server.getHost(), server.getPort());
                    }
                }
                catch (Exception e) {
                    server.setStatus(Status.DISCONNECTED);

                    Intent sIntent = Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, serverId);
                    sendBroadcast(sIntent);

                    IRCConnection connection = getConnection(serverId);

                    Message message;

                    if (e instanceof NickAlreadyInUseException) {
                        message = new Message(getString(R.string.nickname_in_use, connection.getNick()));
                        server.setMayReconnect(false);
                    } else if (e instanceof IrcException) {
                        message = new Message(getString(R.string.irc_login_error, server.getHost(), server.getPort()));
                        server.setMayReconnect(false);
                    } else {
                        message = new Message(getString(R.string.could_not_connect, server.getHost(), server.getPort()));
                        if (settings.isReconnectEnabled()) {
                            Intent rIntent = new Intent(Broadcast.SERVER_RECONNECT + serverId);
                            PendingIntent pendingRIntent = PendingIntent.getBroadcast(service, 0, rIntent, 0);
                            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                            ReconnectReceiver receiver = new ReconnectReceiver(service, server);
                            synchronized(alarmIntentsLock) {
                                alarmReceivers.put(serverId, receiver);
                                registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_RECONNECT + serverId));
                                am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + reconnectInterval, pendingRIntent);
                                alarmIntents.put(serverId, pendingRIntent);
                            }
                        }
                    }

                    message.setColor(Message.COLOR_RED);
                    message.setIcon(R.drawable.error);
                    server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

                    Intent cIntent = Broadcast.createConversationIntent(
                        Broadcast.CONVERSATION_MESSAGE,
                        serverId,
                        ServerInfo.DEFAULT_NAME
                        );
                    sendBroadcast(cIntent);
                }
            }
        }.start();
    }

    /**
     * Get connection for given server
     *
     * @param serverId
     * @return
     */
    public synchronized IRCConnection getConnection(int serverId)
    {
        IRCConnection connection = connections.get(serverId);

        if (connection == null) {
            connection = new IRCConnection(this, serverId);
            connections.put(serverId, connection);
        }

        return connection;
    }

    /**
     * Does the service keep a connection object for this server?
     *
     * @return true if there's a connection object, false otherwise
     */
    public boolean hasConnection(int serverId)
    {
        return connections.containsKey(serverId);
    }

    /**
     * Check status of service
     */
    public void checkServiceStatus()
    {
        boolean shutDown = true;
        ArrayList<Server> mServers = Yaaic.getInstance().getServersAsArrayList();
        int mSize = mServers.size();
        Server server;

        for (int i = 0; i < mSize; i++) {
            server = mServers.get(i);
            if (server.isDisconnected() && !server.mayReconnect()) {
                int serverId = server.getId();
                synchronized(this) {
                    IRCConnection connection = connections.get(serverId);
                    if (connection != null) {
                        connection.dispose();
                    }
                    connections.remove(serverId);
                }

                synchronized(alarmIntentsLock) {
                    // XXX: alarmIntents can be null
                    PendingIntent pendingRIntent = alarmIntents.get(serverId);
                    if (pendingRIntent != null) {
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        am.cancel(pendingRIntent);
                        alarmIntents.remove(serverId);
                    }
                    ReconnectReceiver receiver = alarmReceivers.get(serverId);
                    if (receiver != null) {
                        unregisterReceiver(receiver);
                        alarmReceivers.remove(serverId);
                    }
                }
            } else {
                shutDown = false;
            }
        }

        if (shutDown) {
            foreground = false;
            stopForegroundCompat(R.string.app_name);
            stopSelf();
        }
    }

    /**
     * On Destroy
     */
    @Override
    public void onDestroy()
    {
        // Make sure our notification is gone.
        if (foreground) {
            stopForegroundCompat(R.string.app_name);
        }

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        synchronized(alarmIntentsLock) {
            for (PendingIntent pendingRIntent : alarmIntents.values()) {
                am.cancel(pendingRIntent);
            }
            for (ReconnectReceiver receiver : alarmReceivers.values()) {
                unregisterReceiver(receiver);
            }
            alarmIntents.clear();
            alarmIntents = null;
            alarmReceivers.clear();
            alarmReceivers = null;
        }
    }

    /**
     * On Activity binding to this service
     *
     * @param intent
     * @return
     */
    @Override
    public IRCBinder onBind(Intent intent)
    {
        return binder;
    }
}
