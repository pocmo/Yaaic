/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
package org.yaaic.command.handler;

import org.yaaic.R;
import org.yaaic.command.BaseHandler;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Context;
import android.content.Intent;

/**
 * Command: /msg <target> <message>
 * 
 * Send a message to a channel or user
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MsgHandler extends BaseHandler
{
    /**
     * Execute /msg
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (params.length > 2) {
            String text = BaseHandler.mergeParams(params, 2);
            service.getConnection(server.getId()).sendMessage(params[1], text);

            Conversation targetConversation = server.getConversation(params[1]);

            if (targetConversation != null) {
                Message message = new Message("<" + service.getConnection(server.getId()).getNick() + "> " + text);
                targetConversation.addMessage(message);

                Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    server.getId(),
                    targetConversation.getName()
                );

                service.sendBroadcast(intent);
            }
        } else {
            throw new CommandException(service.getString(R.string.invalid_number_of_params));
        }
    }

    /**
     * Usage of /msg
     */
    @Override
    public String getUsage()
    {
        return "/msg <target> <message>";
    }

    /**
     * Description of /msg
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_msg);
    }
}
