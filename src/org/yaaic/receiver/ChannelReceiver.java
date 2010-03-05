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
package org.yaaic.receiver;

import org.yaaic.listener.ChannelListener;
import org.yaaic.model.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ChannelReceiver extends BroadcastReceiver
{
	private ChannelListener listener;
	
	public ChannelReceiver(ChannelListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if (action.equals(Broadcast.CHANNEL_MESSAGE)) {
			listener.onChannelMessage(intent.getExtras().getString(Broadcast.EXTRA_CHANNEL));
		} else if (action.equals(Broadcast.CHANNEL_NEW)) {
			listener.onNewChannel(intent.getExtras().getString(Broadcast.EXTRA_CHANNEL));
		} else if (action.equals(Broadcast.CHANNEL_REMOVE)) {
			intent.getExtras().getString(Broadcast.EXTRA_CHANNEL);
		}
		
	}
}
