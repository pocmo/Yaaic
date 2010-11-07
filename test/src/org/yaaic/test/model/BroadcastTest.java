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

import org.yaaic.model.Broadcast;
import org.yaaic.model.Extra;

import android.content.Intent;
import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Broadcast
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class BroadcastTest extends TestCase
{
	public void testConversationIntentHelper()
	{
		String broadcastType = Broadcast.CONVERSATION_MESSAGE;
		int serverId = 99;
		String conversationName = "#foobar";
		
		Intent intent = Broadcast.createConversationIntent(broadcastType, serverId, conversationName);
		
		assertEquals(broadcastType, intent.getAction());
		
		assertTrue(intent.hasExtra(Extra.SERVER));
		assertEquals(serverId, intent.getExtras().getInt(Extra.SERVER));
		
		assertTrue(intent.hasExtra(Extra.CONVERSATION));
		assertEquals(conversationName, intent.getExtras().getString(Extra.CONVERSATION));
	}
	
	public void testServerIntentHelper()
	{
		String broadcastType = Broadcast.SERVER_UPDATE;
		int serverId = 42;
		
		Intent intent = Broadcast.createServerIntent(broadcastType, serverId);
		
		assertEquals(broadcastType, intent.getAction());
		
		assertTrue(intent.hasExtra(Extra.SERVER));
		assertEquals(serverId, intent.getExtras().getInt(Extra.SERVER));
	}
}
