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

import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Channel
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ChannelTest extends TestCase
{
	private Channel channel;
	
	@Override
	protected void setUp()
	{
		this.channel = new Channel("#yaaic");
	}

	public void testGetType()
	{
		assertEquals(Conversation.TYPE_CHANNEL, channel.getType());
	}

	public void testGetName()
	{
		assertEquals("#yaaic", channel.getName());
	}

	public void testSetAndGetTopic()
	{
		assertEquals("", channel.getTopic());
		
		String topic = "This is a topic";
		channel.setTopic(topic);
		
		assertEquals(topic, channel.getTopic());
	}
	
	
	public void testInheritance()
	{
		assertTrue(channel instanceof Conversation);
	}
}
