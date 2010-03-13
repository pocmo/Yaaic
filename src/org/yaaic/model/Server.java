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

import java.util.Collection;
import java.util.LinkedHashMap;

import org.yaaic.R;

/**
 * A server as we know it
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Server
{
	private int id;
	private String title;
	private String host;
	private int port;
	private Identity identity;
	
	private LinkedHashMap<String, Conversation> conversations = new LinkedHashMap<String, Conversation>();
	
	private int status = Status.DISCONNECTED;
	private String selected = "";
	
	/**
	 * Create a new server object
	 */
	public Server()
	{
		conversations.put(ServerInfo.DEFAULT_NAME, new ServerInfo());
		this.selected = ServerInfo.DEFAULT_NAME;
	}
	
	/**
	 * Set the identity for this server
	 * 
	 * @param identity The identity for this server
	 */
	public void setIdentity(Identity identity)
	{
		this.identity = identity;
	}
	
	/**
	 * Get the identity for this server
	 * 
	 * @return identity
	 */
	public Identity getIdentity()
	{
		return identity;
	}
	
	/**
	 * Get unique id of server
	 * 
	 * @return id
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Set unique id of server
	 * 
	 * @param id
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	
	/**
	 * Get title of server
	 * 
	 * @return
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Set title of server
	 * 
	 * @param title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	/**
	 * Get hostname of server
	 * 
	 * @return
	 */
	public String getHost()
	{
		return host;
	}
	
	/**
	 * Set hostname of server
	 * 
	 * @param host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}
	
	/**
	 * Get port of server
	 * 
	 * @return
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
	 * Set port of server
	 * 
	 * @param port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
	
	/**
	 * Set connection status of server
	 * 
	 * @status See constants Status.*
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	/**
	 * Get connection status of server
	 * 
	 * @return See constants Status.*
	 */
	public int getStatus()
	{
		return status;
	}
	
	/**
	 * Is disconnected?
	 * 
	 * @return true if the user is disconnected, false if the user is connected or currently connecting 
	 */
	public boolean isDisconnected()
	{
		return status == Status.DISCONNECTED;
	}
	
	/**
	 * Is connected?
	 * 
	 * @return true if the user is (successfully) connected to this server, false otherwise
	 */
	public boolean isConnected()
	{
		return status == Status.CONNECTED;
	}
	
	/**
	 * Get all conversations
	 * 
	 * @return
	 */
	public Collection<Conversation> getConversations()
	{
		// XXX: This is a bad idea as this is not sorted.
		return conversations.values();
	}
	
	/**
	 * Get conversation by name
	 */
	public Conversation getConversation(String name)
	{
		return conversations.get(name);
	}
	
	/**
	 * Add a new conversation
	 * 
	 * @param conversation The conversation to add
	 */
	public void addConversationl(Conversation conversation)
	{
		conversations.put(conversation.getName(), conversation);
	}
	
	/**
	 * Removes a conversation by name
	 * 
	 * @param name
	 */
	public void removeConversation(String name)
	{
		conversations.remove(name);
	}
	
	/**
	 * Remove all conversations
	 */
	public void clearConversations()
	{
		conversations.clear();
	}
	
	/**
	 * Set name of currently selected conversation
	 * 
	 * @param selected The name of the selected conversation
	 */
	public void setSelectedConversation(String selected)
	{
		this.selected = selected;
	}
	
	/**
	 * Get name of currently selected conversation 
	 * 
	 * @return The name of the selected conversation
	 */
	public String getSelectedConversation()
	{
		return selected;
	}
	
	/**
	 * Get icon for current server status
	 * 
	 * @return int Status icon ressource
	 */
	public int getStatusIcon()
	{
		switch (status) {
			case Status.CONNECTED:
				return R.drawable.connected;
			case Status.DISCONNECTED:
				return R.drawable.disconnected;
			case Status.CONNECTING:
				return R.drawable.connecting;
		}
		
		return R.drawable.connecting;
	}
}
