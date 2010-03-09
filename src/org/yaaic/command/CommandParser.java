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
package org.yaaic.command;

import java.util.HashMap;

import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Intent;

/**
 * Parser for commands
 *  
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class CommandParser
{
	public static final String TAG = "Yaaic/CommandParser";
	
	private HashMap<String, BaseCommand> commands;
	private static CommandParser instance;

	/**
	 * Create a new CommandParser instance
	 */
	private CommandParser()
	{
		commands = new HashMap<String, BaseCommand>();
		
		commands.put("nick", new NickCommand());
		commands.put("join", new JoinCommand());
	}
	
	/**
	 * Get the global CommandParser instance
	 * 
	 * @return
	 */
	public static CommandParser getInstance()
	{
		if (instance == null) {
			instance = new CommandParser();
		}
		
		return instance;
	}
	
	/**
	 * Is the given command a valid command?
	 * 
	 * @param command
	 * @return
	 */
	public boolean isCommand(String command)
	{
		return commands.containsKey(command);
	}
	
	/**
	 * Parse the given line
	 * 
	 * @param line
	 */
	public void parse(String line, Server server, Channel channel, IRCService service)
	{
		line = line.trim().substring(1); // cut the slash
		String[] params = line.split(" ");
		String type = params[0];
		
		if (isCommand(type)) {
			BaseCommand command = commands.get(type);
			try {
				command.execute(params, server, channel, service);
			} catch(CommandException e) {
				// Wrong number of params
				if (channel != null) {
					Message errorMessage = new Message(type + ": " + e.getMessage());
					errorMessage.setColor(Message.COLOR_RED);
					channel.addMessage(errorMessage);
					
					Message usageMessage = new Message("Syntax: " + command.getUsage());
					channel.addMessage(usageMessage);
					
					Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
					intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
					intent.putExtra(Broadcast.EXTRA_CHANNEL, channel.getName());
					service.sendBroadcast(intent);
				}
			}
		} else {
			// Unknown command
			if (channel != null) {
				Message message = new Message("Unknown command: " + type);
				message.setColor(Message.COLOR_RED);
				channel.addMessage(message);
				
				Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
				intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
				intent.putExtra(Broadcast.EXTRA_CHANNEL, channel.getName());
				service.sendBroadcast(intent);
			}
		}
	}
}
