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
package org.yaaic.irc;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.yaaic.model.Server;

import android.os.Binder;
import android.util.Log;

/**
 * Binder for service communication
 *  
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCBinder extends Binder
{
	public static final String TAG = "Yaaic/IRCBinder";
	
	private IRCService service;
	
	/**
	 * Create a new binder for given service 
	 * 
	 * @param service
	 */
	public IRCBinder(IRCService service)
	{
		super();
		
		this.service = service;
	}
	
	/**
	 * Connect to given server
	 * 
	 * @param server
	 */
	public void connect(final Server server)
	{
		new Thread() {
			public void run() {
				try {
					IRCConnection connection = getService().getConnection(server.getId());

					connection.setNickname(server.getIdentity().getNickname());
					connection.setIdent(server.getIdentity().getIdent());
					connection.setRealName(server.getIdentity().getRealName());
					
					connection.connect(server.getHost(), server.getPort());
				}
				catch (NickAlreadyInUseException e) {
					Log.d(TAG, "NickAlreadyInUseException: " + e.getMessage());
				}
				catch (IrcException e) {
					Log.d(TAG, "IrcException: " + e.getMessage());
				}
				catch (IOException e) {
					Log.d(TAG, "IOException: " + e.getMessage());
				}
			}
		}.start();
	}
	
	/**
	 * Get service associated with this service
	 * @return
	 */
	public IRCService getService()
	{
		return service;
	}
}
