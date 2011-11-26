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
package org.yaaic.test.receiver;

import org.yaaic.listener.ConversationListener;
import org.yaaic.model.Broadcast;
import org.yaaic.receiver.ConversationReceiver;

import android.content.Intent;
import android.test.AndroidTestCase;

/**
 * Test case for org.yaaic.receiver.ConversationReceiver
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationReceiverTest extends AndroidTestCase implements ConversationListener
{
	private boolean onConversationMessageCalled;
	private boolean onNewConversationCalled;
	private boolean onRemoveConversationCalled;

	private ConversationReceiver receiver;
	private String testTarget = "#unittest";
	private int serverId = 42;

	public void setUp()
	{
		onConversationMessageCalled = false;
		onNewConversationCalled = false;
		onRemoveConversationCalled = false;

		receiver = new ConversationReceiver(serverId, this);
	}

	public void testMessageBroadcast()
	{
		Intent intent = Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, serverId, testTarget);
		receiver.onReceive(getContext(), intent);

		assertTrue(onConversationMessageCalled);
		assertFalse(onNewConversationCalled);
		assertFalse(onRemoveConversationCalled);
	}

	public void testNewBroadcast()
	{
		Intent intent = Broadcast.createConversationIntent(Broadcast.CONVERSATION_NEW, serverId, testTarget);
		receiver.onReceive(getContext(), intent);

		assertFalse(onConversationMessageCalled);
		assertTrue(onNewConversationCalled);
		assertFalse(onRemoveConversationCalled);
	}

	public void testRemoveBroadcast()
	{
		Intent intent = Broadcast.createConversationIntent(Broadcast.CONVERSATION_REMOVE, serverId, testTarget);
		receiver.onReceive(getContext(), intent);

		assertFalse(onConversationMessageCalled);
		assertFalse(onNewConversationCalled);
		assertTrue(onRemoveConversationCalled);
	}

	@Override
	public void onConversationMessage(String target)
	{
		assertEquals(testTarget, target);

		onConversationMessageCalled = true;
	}

	@Override
	public void onNewConversation(String target)
	{
		assertEquals(testTarget, target);

		onNewConversationCalled = true;
	}

	@Override
	public void onRemoveConversation(String target)
	{
		assertEquals(testTarget, target);

		onRemoveConversationCalled = true;
	}

	@Override
	public void onTopicChanged(String topic)
	{
		// XXX: Implement me!
	}
}
