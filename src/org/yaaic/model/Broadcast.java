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
package org.yaaic.model;

import android.content.Intent;

/**
 * Constants and helpers for Broadcasts
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public abstract class Broadcast
{
	public static final String SERVER_UPDATE 		= "org.yaaic.server.status";
	
	public static final String CONVERSATION_MESSAGE	= "org.yaaic.conversation.message";
	public static final String CONVERSATION_NEW		= "org.yaaic.conversation.new";
	public static final String CONVERSATION_REMOVE	= "org.yaaic.conversation.remove";

	public static final String EXTRA_SERVER			= "server";
	public static final String EXTRA_CONVERSATION	= "conversation";
	
	/**
	 * Create an intent for conversation broadcasting
	 * 
	 * @param broadcastType The type of the broadcast, some constant of Broadcast.*
	 * @param serverId The id of the server
	 * @param conversationName The unique name of the conversation
	 * @return
	 */
	public static Intent createConversationIntent(String broadcastType, int serverId, String conversationName)
	{
		Intent intent = new Intent(broadcastType);
		
		intent.putExtra(Broadcast.EXTRA_SERVER, serverId);
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, conversationName);
		
		return intent;
	}
}
