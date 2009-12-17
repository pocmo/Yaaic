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

import android.content.Intent;

import org.jibble.pircbot.PircBot;

import org.yaaic.Yaaic;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

public class IRCConnection extends PircBot
{
	private IRCService service;
	private Server server;
	
	public IRCConnection(IRCService service, int serverId)
	{
		this.server = Yaaic.getInstance().getServerById(serverId);
		this.service = service;
		
		this.setName("Yaaic");
		this.setLogin("Yaaic");
		this.setAutoNickChange(true);
		this.setVersion("Yaaic - Yet another Android IRC client - http://www.yaaic.org");
	}

	/**
	 * On connect
	 */
	@Override
	public void onConnect()
	{
		server.setStatus(Status.CONNECTED);
		
		service.sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}
	
	/**
	 * On disconnect
	 */
	@Override
	public void onDisconnect()
	{
		server.setStatus(Status.DISCONNECTED);
		
		service.sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}

	/**
	 * Quits from the IRC server with default reason.
	 */
	@Override
	public void quitServer()
	{
		quitServer("Yaaic - Yet another Android IRC client - http://www.yaaic.org");
	}
}
