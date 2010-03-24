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

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.model.Server;
import org.yaaic.view.ServersActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

/**
 * The background service for managing the irc connections
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCService extends Service
{
	public final static String TAG = "Yaaic/IRCService";
	
	private IRCBinder binder;
	private HashMap<Integer, IRCConnection> connections;
	
    @SuppressWarnings("unchecked")
	private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
    @SuppressWarnings("unchecked")
    private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
	
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
	/**
	 * Create new service
	 */
	public IRCService()
	{
		super();
		
		Log.d(TAG, "Service created...");
		
		this.connections = new HashMap<Integer, IRCConnection>();
		this.binder = new IRCBinder(this);
	}
	
	/**
	 * On create
	 */
	@Override
	public void onCreate()
	{
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
		
		// Load servers from Database
		/*
        Database db = new Database(this);
		Yaaic.getInstance().setServers(db.getServers());
		db.close();
		*/

		// Broadcast changed server list
		//sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}

	/**
	 * On start (will be called on pre-2.0 platform. On 2.0 or later onStartCommand()
	 * will be called)
	 */
	@Override
	public void onStart(Intent intent, int startId)
	{
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
        handleCommand(intent);

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
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, "Mama", System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ServersActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name), "Papa", contentIntent);

        startForegroundCompat(R.string.app_name, notification);
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
	        mNM.notify(id, notification);
        }
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    private void stopForegroundCompat(int id) {
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
	        mNM.cancel(id);
	        setForeground(false);
        }
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
	
	public void checkServiceStatus()
	{
		Log.d("Yaaic", "(1)");
		
		boolean shutDown = true;
		ArrayList<Server> mServers = Yaaic.getInstance().getServersAsArrayList();
		int mSize = mServers.size();
		Server server;
		
		Log.d("Yaaic", "(2)");
		
		for (int i = 0; i < mSize; i++) {
			Log.d("Yaaic", "   (3)");
			server = mServers.get(i);
			if (server.isDisconnected()) {
				connections.remove(server.getId());
			} else {
				shutDown = false;
			}
		}
		
		if (shutDown) {
			stopSelf();
		}
	}
	
	/**
	 * 
	 */
    @Override
    public void onDestroy()
    {
        // Make sure our notification is gone.
        stopForegroundCompat(R.string.app_name);
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
