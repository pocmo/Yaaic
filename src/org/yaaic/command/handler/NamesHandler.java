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

import org.jibble.pircbot.User;
import org.yaaic.command.BaseHandler;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Intent;

/**
 * Command: /names
 * Lists all users currently in the selected channel 
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class NamesHandler extends BaseHandler
{
	private String desc = "lists all users in channel";
	
	/**
	 * Execute /names
	 */
	@Override
	public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException 
	{
		if (conversation.getType() != Conversation.TYPE_CHANNEL) {
			throw new CommandException("Only usable from within a channel");
		}
		
		StringBuffer userList = new StringBuffer("Users " + conversation.getName() + ":");
		for (User user : service.getConnection(server.getId()).getUsers(conversation.getName())) {
			userList.append(" ");
			userList.append(user.getPrefix());
			userList.append(user.getNick());
		}
		
		Message message = new Message(userList.toString());
		message.setColor(Message.COLOR_YELLOW);
		conversation.addMessage(message);
		
		Intent intent = Broadcast.createConversationIntent(
			Broadcast.CONVERSATION_MESSAGE,
			server.getId(),
			conversation.getName()
		);
		service.sendBroadcast(intent);
	}
	
	/**
	 * Usage of /names
	 */
	@Override
	public String getUsage()
	{
		return "/names";
	}


	/**
	 * Description of /names
	 */
	@Override
	public String getDescription() {
		return desc;
	}
}
