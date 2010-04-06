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
package org.yaaic.command.handler;

import java.io.File;

import org.yaaic.command.BaseHandler;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

/**
 * Command: /dcc SEND <nickname> <file>
 * 
 * Send a file to a user
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DCCHandler extends BaseHandler
{
	/**
	 * Execute /dcc
	 */
	@Override
	public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException 
	{
		if (params.length == 4) {
			if (!params[1].equalsIgnoreCase("SEND")) {
				throw new CommandException("Currently only SEND is allowed");
			}
			File file = new File(params[3]);
			if (!file.exists()) {
				throw new CommandException("File does not exist: " + params[3]);
			}
			
			service.getConnection(server.getId()).dccSendFile(file, params[2], 60000);
			
			Message message = new Message("Waiting for " + params[2] + " to accept the file transfer");
			message.setColor(Message.COLOR_GREY);
			conversation.addMessage(message);
			
			service.sendBroadcast(
				Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), conversation.getName())
			);
		} else {
			throw new CommandException("Invalid number of params");
		}
	}
	
	/**
	 * Usage of /dcc
	 */
	@Override
	public String getUsage()
	{
		return "/dcc SEND <nickname> <file>";
	}

	/**
	 * Description of /dcc
	 */
	@Override
	public String getDescription()
	{
		return "Send a file to a user";
	}
}
