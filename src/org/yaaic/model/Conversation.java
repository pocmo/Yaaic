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
package org.yaaic.model;

import java.util.LinkedList;

/**
 * Base class for conversations
 * 
 * A conversation can be a channel, a query or server messages
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public abstract class Conversation
{
	private static final int HISTORY_SIZE = 30;
	
	private LinkedList<Message> buffer = new LinkedList<Message>();
	private LinkedList<Message> history = new LinkedList<Message>();
	private String name;
	
	/**
	 * Create a new conversation with the given name
	 * 
	 * @param name The name of the conversation (channel, user)
	 */
	public Conversation(String name)
	{
		this.name = name;
	}
	
	/**
	 * Get name of the conversation (channel, user)
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Add a message to the channel
	 */
	public void addMessage(Message message)
	{
		buffer.addFirst(message);
		history.addLast(message);
		
		if (history.size() > HISTORY_SIZE) {
			history.removeFirst();
		}
	}
	
	/**
	 * Get channel history
	 * 
	 * @return
	 */
	public LinkedList<Message> getHistory()
	{
		return history;
	}
	
	/**
	 * Get last buffered message
	 * 
	 * @return
	 */
	public Message pollBufferedMessage()
	{
		Message message = buffer.getLast();
		buffer.removeLast();
		return message;
	}
	
	/**
	 * Does the channel have buffered messages?
	 */
	public boolean hasBufferedMessages()
	{
		return buffer.size() > 0;
	}
	
	/**
	 * Clear the message buffer
	 */
	public void clearBuffer()
	{
		buffer.clear();
	}
}
