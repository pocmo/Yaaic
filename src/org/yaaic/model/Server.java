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

import java.util.ArrayList;

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
	
	private ArrayList<Channel> channels = new ArrayList<Channel>();
	
	private int status = Status.DISCONNECTED;
	
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
	 * Get all (joined) channels
	 * 
	 * @return
	 */
	public ArrayList<Channel> getChannels()
	{
		return channels;
	}
	
	/**
	 * Add a new (joined) channel
	 * 
	 * @param channel
	 */
	public void addChannel(Channel channel)
	{
		channels.add(channel);
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
