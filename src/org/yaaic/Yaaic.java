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
package org.yaaic;

import java.util.ArrayList;
import java.util.HashMap;

import org.yaaic.model.Server;

/**
 * Global Master Class :)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Yaaic
{
	public static Yaaic instance;
	
	private HashMap<Integer, Server> servers;
	
	/**
	 * Private constructor, you may want to use static getInstance()
	 */
	private Yaaic()
	{
		servers = new HashMap<Integer, Server>();
	}
	
	/**
	 * Get global Yaaic instance
	 * 
	 * @return the global Yaaic instance
	 */
	public static Yaaic getInstance()
	{
		if (instance == null) {
			instance = new Yaaic(); 
		}
		
		return instance;
	}
	
	/**
	 * Get server by id
	 * 
	 * @return Server object with given unique id
	 */
	public Server getServerById(int serverId)
	{
		return servers.get(serverId);
	}
	
	/**
	 * Remove server with given unique id from list
	 * 
	 * @param serverId
	 */
	public void removeServerById(int serverId)
	{
		servers.remove(serverId);
	}
	
	/**
	 * Set servers
	 * 
	 * @param servers
	 */
	public void setServers(HashMap<Integer, Server> servers)
	{
		this.servers = servers;
	}
	
	/**
	 * Add server to list
	 */
	public void addServer(Server server)
	{
		if (!servers.containsKey(server.getId())) {
			servers.put(server.getId(), server);
		}
	}
	
	/**
	 * Get list of servers
	 * 
	 * @return list of servers
	 */
	public ArrayList<Server> getServersAsArrayList()
	{
		ArrayList<Server> serverList = new ArrayList<Server>();
		
		for (int key : servers.keySet()) {
			serverList.add(servers.get(key));
		}
		
		return serverList;
	}
}
