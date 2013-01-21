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
package org.yaaic.receiver;

import org.yaaic.listener.ServerListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A server receiver for receiving server updates
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerReceiver extends BroadcastReceiver
{
    private final ServerListener listener;

    /**
     * Create a new server receiver
     * 
     * @param listener
     */
    public ServerReceiver(ServerListener listener)
    {
        this.listener = listener;
    }

    /**
     * On receive broadcast
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        listener.onStatusUpdate();
    }
}
