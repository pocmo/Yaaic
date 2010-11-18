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
package org.yaaic.irc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.activity.ServersActivity;
import org.yaaic.db.Database;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Message;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Settings;
import org.yaaic.model.Status;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

/**
 * The background service for managing the irc connections
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCService extends Service
{
	private IRCBinder binder;
	private HashMap<Integer, IRCConnection> connections;
	private boolean foreground = false;
	
	@SuppressWarnings("rawtypes")
	private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
	@SuppressWarnings("rawtypes")
    private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
    
    public static final String ACTION_FOREGROUND = "org.yaaic.service.foreground";
    public static final String ACTION_BACKGROUND = "org.yaaic.service.background";
	
    private NotificationManager notificationManager;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    private Notification notification;
    private Settings settings;
    
	/**
	 * Create new service
	 */
	public IRCService()
	{
		super();
		
		this.connections = new HashMap<Integer, IRCConnection>();
		this.binder = new IRCBinder(this);
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
	        notification = new Notification(R.drawable.icon, "", System.currentTimeMillis());
	
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ServersActivity.class), 0);
	
	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, getText(R.string.app_name), "", contentIntent);
	
	        startForegroundCompat(R.string.app_name, notification);
    	} else if (ACTION_BACKGROUND.equals(intent.getAction()) && !foreground) {
            stopForegroundCompat(R.string.app_name);
        }
    }
    
    /**
     * Update notification
     * 
     * @param text The text to display
     */
    public void updateNotification(String text)
    {
        updateNotification(text, false);
    }

    /**
     * Update notification and vibrate if needed
     *
     * @param text 	  The text to display
     * @param vibrate True if the device should vibrate, false otherwise
     */
    public void updateNotification(String text, boolean vibrate)
    {
    	if (foreground) {
    		notificationManager.cancel(R.string.app_name);
    		notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
    		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ServersActivity.class), 0);
    		notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);
    		if (vibrate) {
    		    notification.defaults |= Notification.DEFAULT_VIBRATE;
    		}
    		notificationManager.notify(R.string.app_name, notification);
    	}
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
	        setForeground(true);
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
	        setForeground(false);
        }
    }
    
    /**
     * Connect to the given server
     */
    public void connect(final Server server)
    {
		new Thread() {
			public void run() {
				try {
					IRCConnection connection = getConnection(server.getId());

					connection.setNickname(server.getIdentity().getNickname());
					connection.setAliases(server.getIdentity().getAliases());
					connection.setIdent(server.getIdentity().getIdent());
					connection.setRealName(server.getIdentity().getRealName());
					connection.setUseSSL(server.useSSL());
					
					if (server.getCharset() != null) {
						connection.setEncoding(server.getCharset());
					}
					
					if (server.getPassword() != "") {
						connection.connect(server.getHost(), server.getPort(), server.getPassword());
					} else {
						connection.connect(server.getHost(), server.getPort());
					}
				}
				catch (Exception e) {
					server.setStatus(Status.DISCONNECTED);
					
					Intent sIntent = Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId());
					sendBroadcast(sIntent);
					
					IRCConnection connection = getConnection(server.getId());
					
					Message message;
					
					if (e instanceof NickAlreadyInUseException) {
						message = new Message(getString(R.string.nickname_in_use, connection.getNick()));
					} else if (e instanceof IrcException) {
						message = new Message(getString(R.string.irc_login_error, server.getHost(), server.getPort()));
					} else {
						message = new Message(getString(R.string.could_not_connect, server.getHost(), server.getPort()));
					}
					
					message.setColor(Message.COLOR_RED);
					message.setIcon(R.drawable.error);
					server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);
					
					Intent cIntent = Broadcast.createConversationIntent(
						Broadcast.CONVERSATION_MESSAGE,
						server.getId(),
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
			if (server.isDisconnected()) {
				connections.remove(server.getId());
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
