/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

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

import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Server;
import org.yaaic.view.ConversationSwitcher;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

/**
 * Listener for conversation selections
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationSelectedListener implements OnItemSelectedListener
{
    private final Server server;
    private final TextView titleView;
    private final ConversationSwitcher switcher;

    /**
     * Create a new ConversationSelectedListener
     * 
     * @param server
     * @param titleView
     */
    public ConversationSelectedListener(Server server, TextView titleView, ConversationSwitcher switcher)
    {
        this.server = server;
        this.titleView = titleView;
        this.switcher = switcher;
    }

    /**
     * On conversation selected/focused
     */
    @Override
    public void onItemSelected(AdapterView<?> deck, View view, int position, long id)
    {
        Conversation conversation = (Conversation) deck.getItemAtPosition(position);

        if (conversation != null && conversation.getType() != Conversation.TYPE_SERVER) {
            StringBuilder sb = new StringBuilder();
            sb.append(server.getTitle() + " - " + conversation.getName());
            if (conversation.getType() == Conversation.TYPE_CHANNEL && !((Channel)conversation).getTopic().equals(""))
                sb.append(" - " + ((Channel)conversation).getTopic());
            titleView.setText(sb.toString());
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

        switcher.invalidate();
    }

    /**
     * On no conversation selected/focused
     */
    @Override
    public void onNothingSelected(AdapterView<?> deck)
    {
        titleView.setText(server.getTitle());
    }
}
