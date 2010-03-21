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

import java.util.HashMap;

import org.yaaic.Yaaic;
import org.yaaic.db.Database;
import org.yaaic.model.Broadcast;

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
	
	/**
	 * Create new service
	 */
	public IRCService()
	{
		super();
		
		this.connections = new HashMap<Integer, IRCConnection>();
		this.binder = new IRCBinder(this);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// Load servers from Database
		Database db = new Database(this);
		Yaaic.getInstance().setServers(db.getServers());
		db.close();

		// Broadcast changed server list
		sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
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
