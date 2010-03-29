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

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.yaaic.R;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Message;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Status;

import android.content.Intent;
import android.os.Binder;

/**
 * Binder for service communication
 *  
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCBinder extends Binder
{
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
					
					if (server.getPassword() != "") {
						connection.connect(server.getHost(), server.getPort(), server.getPassword());
					} else {
						connection.connect(server.getHost(), server.getPort());
					}
				}
				catch (Exception e) {
					server.setStatus(Status.DISCONNECTED);
					
					Intent sIntent = Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId());
					service.sendBroadcast(sIntent);
					
					IRCConnection connection = getService().getConnection(server.getId());
					
					Message message;
					
					if (e instanceof NickAlreadyInUseException) {
						message = new Message("Nickname " + connection.getNick() + " already in use");
					} else if (e instanceof IrcException) {
						message = new Message("Could not log into the IRC server " + server.getHost() + ":" + server.getPort());
					} else {
						message = new Message("Could not connect to " + server.getHost() + ":" + server.getPort());
					}
					
					message.setColor(Message.COLOR_RED);
					message.setIcon(R.drawable.error);
					server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);
					
					Intent cIntent = Broadcast.createConversationIntent(
						Broadcast.CONVERSATION_MESSAGE,
						server.getId(),
						ServerInfo.DEFAULT_NAME
					);
					service.sendBroadcast(cIntent);
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
