/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari
Copyright 2011 Steven Luo

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

import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A receiver to listen for alarms and start a reconnect attempt
 * 
 * @author Steven Luo <steven+android@steven676.net>
 */
public class ReconnectReceiver extends BroadcastReceiver
{
    private IRCService service;
    private Server server;

    /**
     * Create a new reconnect receiver
     * 
     * @param server The server to reconnect to
     */
    public ReconnectReceiver(IRCService service, Server server)
    {
        this.service = service;
        this.server = server;
    }

    /**
     * On receive broadcast
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (!intent.getAction().equals(Broadcast.SERVER_RECONNECT + server.getId())) {
            return;
        }
        service.connect(server);
    }
}
