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
package org.yaaic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.yaaic.listener.ChannelListener;
import org.yaaic.model.Broadcast;

public class ChannelReceiver extends BroadcastReceiver
{
	public static final String TAG = "Yaaic/ChannelReceiver";
	
	private ChannelListener listener;
	private int serverId;
	
	/**
	 * Create a new channel reciever
	 *  
	 * @param serverId Only listen on channels of this server
	 * @param listener 
	 */
	public ChannelReceiver(int serverId, ChannelListener listener)
	{
		this.listener = listener;
		this.serverId = serverId;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		int serverId = intent.getExtras().getInt(Broadcast.EXTRA_SERVER);
		if (serverId != this.serverId) {
			return;
		}
		
		String action = intent.getAction();

		if (action.equals(Broadcast.CONVERSATION_MESSAGE)) {
			listener.onConversationMessage(intent.getExtras().getString(Broadcast.EXTRA_CONVERSATION));
		} else if (action.equals(Broadcast.CONVERSATION_NEW)) {
			listener.onNewConversation(intent.getExtras().getString(Broadcast.EXTRA_CONVERSATION));
		} else if (action.equals(Broadcast.CONVERSATION_REMOVE)) {
			listener.onRemoveConversation(intent.getExtras().getString(Broadcast.EXTRA_CONVERSATION));
		}
		
	}
}
