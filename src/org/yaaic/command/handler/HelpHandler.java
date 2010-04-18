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

import java.util.HashMap;
import java.util.Set;

import org.yaaic.command.BaseHandler;
import org.yaaic.command.CommandParser;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Intent;

/**
 * Command: /help 
 * 
 * @author Karol Gliniecki <karol.gliniecki@googlemail.com>
 */
public class HelpHandler extends BaseHandler
{
	/**
	 * Execute /help
	 */
	@Override
	public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
	{
		CommandParser cp = CommandParser.getInstance();
		
		StringBuffer commandList = new StringBuffer("available commands: \n");
		HashMap<String, BaseHandler> commands = cp.getCommands();
		HashMap<String, String> aliases = cp.getAliases();
		
		Set<String> commandKeys = commands.keySet();
		Set<String> aliasesKeys = aliases.keySet();

		Message message;
		if (params.length == 2) {
			try { 
				message = new Message("Usage:\n"+commands.get(params[1]).getUsage());
				message.setColor(Message.COLOR_YELLOW);
			} catch (Exception e) {
				message = new Message(params[1]+" is not a valid command");
				message.setColor(Message.COLOR_RED);
			}
		} else {
			for (Object command: commandKeys) {
				String alias = "";
				for (Object aliasCommand: aliasesKeys) {
					if (command.equals(aliases.get(aliasCommand))) {
						alias = " or /" + aliasCommand;
						break;
					}
				}
				commandList.append("/" + command.toString() + alias + " - "+commands.get(command).getDescription() + "\n");
			}
			message = new Message(commandList.toString());
			message.setColor(Message.COLOR_YELLOW);
		}
		
		conversation.addMessage(message);

		Intent intent = Broadcast.createConversationIntent(
			Broadcast.CONVERSATION_MESSAGE,
			server.getId(),
			conversation.getName()
		);
		service.sendBroadcast(intent);
	}

	/**
	 * 
	 *Usage of /help
	 */
	@Override
	public String getUsage()
	{
		return "/help [<command>]";
	}

	/**
	 * Description of /help
	 */
	@Override
	public String getDescription()
	{
		return "Lists all available commands";
	}
}
