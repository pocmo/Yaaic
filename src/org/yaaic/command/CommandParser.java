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
package org.yaaic.command;

import java.util.HashMap;

import org.yaaic.command.handler.CloseHandler;
import org.yaaic.command.handler.DeopHandler;
import org.yaaic.command.handler.DevoiceHandler;
import org.yaaic.command.handler.EchoHandler;
import org.yaaic.command.handler.JoinHandler;
import org.yaaic.command.handler.KickHandler;
import org.yaaic.command.handler.MeHandler;
import org.yaaic.command.handler.NamesHandler;
import org.yaaic.command.handler.NickHandler;
import org.yaaic.command.handler.NoticeHandler;
import org.yaaic.command.handler.OpHandler;
import org.yaaic.command.handler.PartHandler;
import org.yaaic.command.handler.QueryHandler;
import org.yaaic.command.handler.QuitHandler;
import org.yaaic.command.handler.TopicHandler;
import org.yaaic.command.handler.VoiceHandler;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
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
	
	private HashMap<String, BaseHandler> commands;
	private static CommandParser instance;

	/**
	 * Create a new CommandParser instance
	 */
	private CommandParser()
	{
		commands = new HashMap<String, BaseHandler>();
		
		// Commands
		commands.put("nick", new NickHandler());
		commands.put("join", new JoinHandler());
		commands.put("me", new MeHandler());
		commands.put("names", new NamesHandler());
		commands.put("echo", new EchoHandler());
		commands.put("topic", new TopicHandler());
		commands.put("quit", new QuitHandler());
		commands.put("op", new OpHandler());
		commands.put("voice", new VoiceHandler());
		commands.put("deop", new DeopHandler());
		commands.put("devoice", new DevoiceHandler());
		commands.put("kick", new KickHandler());
		commands.put("query", new QueryHandler());
		commands.put("part", new PartHandler());
		commands.put("close", new CloseHandler());
		commands.put("notice", new NoticeHandler());
		
		// Aliases
		commands.put("j", commands.get("join"));
		commands.put("q", commands.get("query"));
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
	public void parse(String line, Server server, Conversation conversation, IRCService service)
	{
		line = line.trim().substring(1); // cut the slash
		String[] params = line.split(" ");
		String type = params[0];
		
		if (isCommand(type)) {
			BaseHandler command = commands.get(type);
			try {
				command.execute(params, server, conversation, service);
			} catch(CommandException e) {
				// Wrong number of params
				if (conversation != null) {
					Message errorMessage = new Message(type + ": " + e.getMessage());
					errorMessage.setColor(Message.COLOR_RED);
					conversation.addMessage(errorMessage);
					
					Message usageMessage = new Message("Syntax: " + command.getUsage());
					conversation.addMessage(usageMessage);
					
					Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
					intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
					intent.putExtra(Broadcast.EXTRA_CONVERSATION, conversation.getName());
					service.sendBroadcast(intent);
				}
			}
		} else {
			// Unknown command
			if (conversation != null) {
				Message message = new Message("Unknown command: " + type);
				message.setColor(Message.COLOR_RED);
				conversation.addMessage(message);
				
				Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
				intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
				intent.putExtra(Broadcast.EXTRA_CONVERSATION, conversation.getName());
				service.sendBroadcast(intent);
			}
		}
	}
}
