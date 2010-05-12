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
package org.yaaic.listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import org.yaaic.model.Conversation;
import org.yaaic.model.Server;

/**
 * Listener for conversation selections
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationSelectedListener implements OnItemSelectedListener
{
	private Server server;
	private TextView titleView;
	
	/**
	 * Create a new ConversationSelectedListener
	 * 
	 * @param server
	 * @param titleView
	 */
	public ConversationSelectedListener(Server server, TextView titleView)
	{
		this.server = server;
		this.titleView = titleView;
	}
	
	/**
	 * On conversation selected/focused
	 */
	public void onItemSelected(AdapterView<?> deck, View view, int position, long id)
	{
		Conversation conversation = (Conversation) deck.getItemAtPosition(position);
		
		if (conversation != null && conversation.getType() != Conversation.TYPE_SERVER) {
			titleView.setText(server.getTitle() + " - " + conversation.getName());
		} else {
			onNothingSelected(deck);
		}
		
		// Remember selection
		if (conversation != null) {
			Conversation previousConversation = server.getConversation(server.getSelectedConversation());
			
			if (previousConversation != null) {
				previousConversation.setStatus(Conversation.STATUS_DEFAULT);
			}
			
			conversation.setStatus(Conversation.STATUS_SELECTED);
			server.setSelectedConversation(conversation.getName());
		}
	}
	
	/**
	 * On no conversation selected/focused
	 */
	public void onNothingSelected(AdapterView<?> deck)
	{
		titleView.setText(server.getTitle());
	}
}
