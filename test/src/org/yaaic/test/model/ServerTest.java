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
package org.yaaic.test.model;

import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Identity;
import org.yaaic.model.Query;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Status;

import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerTest extends TestCase
{
	private Server server;
	
	@Override
	public void setUp()
	{
		this.server = new Server();
	}
	
	public void testInstance()
	{
		assertEquals(1, server.getConversations().size());
		
		assertNotNull(server.getConversation(ServerInfo.DEFAULT_NAME));
		assertEquals(Conversation.TYPE_SERVER, server.getConversation(ServerInfo.DEFAULT_NAME).getType());
	}
	
	public void testSetAndGetHost()
	{
		assertNull(server.getHost());
		
		server.setHost("irc.epd-me.net");
		assertEquals("irc.epd-me.net", server.getHost());
	}
	
	public void testSetAndGetIdentity()
	{
		assertNull(server.getIdentity());
		
		Identity identity = new Identity();
		identity.setNickname("ServerTest");
		server.setIdentity(identity);
		
		assertEquals("ServerTest", server.getIdentity().getNickname());
	}
	
	public void testSetAndGetId()
	{
		assertEquals(0, server.getId());
		
		server.setId(42);
		assertEquals(42, server.getId());
		
		server.setId(23);
		assertEquals(23, server.getId());
	}
	
	public void testSetAndGetPassword()
	{
		assertEquals(null, server.getPassword());
		
		server.setPassword("secret");
		assertEquals("secret", server.getPassword());
	}
	
	public void testSetAndGetTitle()
	{
		assertEquals(null, server.getTitle());
		
		server.setTitle("MyServer");
		assertEquals("MyServer", server.getTitle());
	}
	
	public void testSetAndGetPort()
	{
		assertEquals(0, server.getPort());
		
		server.setPort(12345);
		assertEquals(12345, server.getPort());
	}
	
	public void testSetAndGetCharset()
	{
		assertNull(server.getCharset());
		
		server.setCharset("UTF-16");
		assertEquals("UTF-16", server.getCharset());
	}
	
	public void testSetAndGetStatus()
	{
		assertEquals(Status.DISCONNECTED, server.getStatus());
		assertTrue(server.isDisconnected());
		assertFalse(server.isConnected());
		
		server.setStatus(Status.PRE_CONNECTING);
		assertEquals(Status.PRE_CONNECTING, server.getStatus());
		assertFalse(server.isDisconnected());
		assertFalse(server.isConnected());
		
		server.setStatus(Status.CONNECTING);
		assertEquals(Status.CONNECTING, server.getStatus());
		assertFalse(server.isDisconnected());
		assertFalse(server.isConnected());
		
		server.setStatus(Status.CONNECTED);
		assertEquals(Status.CONNECTED, server.getStatus());
		assertFalse(server.isDisconnected());
		assertTrue(server.isConnected());
	}
	
	public void testConversationHandling()
	{
		Channel channel = new Channel("#yaaic");
		
		server.addConversationl(channel);
		assertEquals(2, server.getConversations().size());
		assertNotNull(server.getConversation("#yaaic"));
		
		// ignore case
		assertNotNull(server.getConversation("#yAAic"));
		
		server.addConversationl(new Query("pocmo"));
		assertEquals(3, server.getConversations().size());
		
		// test order
		String[] names = { ServerInfo.DEFAULT_NAME, "#yaaic", "pocmo" };
		int i = 0;
		for (Conversation conversation : server.getConversations()) {
			assertEquals(names[i], conversation.getName());
			i++;
		}
		
		server.removeConversation("#yaaic");
		assertEquals(2, server.getConversations().size());
		
		String[] names2 = { ServerInfo.DEFAULT_NAME, "pocmo" };
		int j = 0;
		for (Conversation conversation : server.getConversations()) {
			assertEquals(names2[j], conversation.getName());
			j++;
		}
		
		// There's a new ServerInfo object after clear
		server.clearConversations();
		assertEquals(1, server.getConversations().size());
		assertNotNull(server.getConversation(ServerInfo.DEFAULT_NAME));
	}
}