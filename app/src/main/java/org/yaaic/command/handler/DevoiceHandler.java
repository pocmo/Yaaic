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
import org.yaaic.model.Conversation;
import org.yaaic.model.Server;

import android.content.Context;

/**
 * Command: /devoice <nickname>
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DevoiceHandler extends BaseHandler
{
    /**
     * Execute /devoice
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (conversation.getType() != Conversation.TYPE_CHANNEL) {
            throw new CommandException(service.getString(R.string.only_usable_from_channel));
        }

        if (params.length == 2) {
            service.getConnection(server.getId()).deVoice(conversation.getName(), params[1]);
        } else {
            throw new CommandException(service.getString(R.string.invalid_number_of_params));
        }
    }

    /**
     * Usage of /devoice
     */
    @Override
    public String getUsage()
    {
        return "/devoice <nickname>";
    }

    /**
     * Description of /devoice
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_devoice);
    }
}
