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
package org.yaaic.command.handler;

import org.yaaic.command.BaseHandler;
import org.yaaic.command.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Query;
import org.yaaic.model.Server;

import android.content.Intent;

/**
 * Command: /query <nickname>
 * 
 * Opens a private chat with the given user
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class QueryHandler extends BaseHandler
{
	/**
	 * Execute /query
	 */
	@Override
	public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException 
	{
		if (params.length == 2) {
			// Simple validation
			if (params[1].startsWith("#")) {
				throw new CommandException("You cannot open queries to channels");
			}
			
			Conversation query = server.getConversation(params[1]);
			
			if (query != null) {
				throw new CommandException("Query already exists");
			}
			
			server.addConversationl(new Query(params[1]));
			
			Intent intent = new Intent(Broadcast.CHANNEL_NEW);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CHANNEL, params[1]);
			service.sendBroadcast(intent);
		} else {
			throw new CommandException("Invalid number of params");
		}
	}
	
	/**
	 * Usage of /query
	 */
	@Override
	public String getUsage()
	{
		return "/query <nickname>";
	}
}
