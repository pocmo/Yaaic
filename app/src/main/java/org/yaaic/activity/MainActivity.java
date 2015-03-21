/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2015 Sebastian Kaspari

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
package org.yaaic.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.yaaic.R;
import org.yaaic.fragment.OverviewFragment;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Extra;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

/**
 * The main activity of Yaaic. We'll add, remove and replace fragments here.
 */
public class MainActivity extends Activity implements OverviewFragment.Callback, ServiceConnection {
    private IRCBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, IRCService.class);
        intent.setAction(IRCService.ACTION_BACKGROUND);
        startService(intent);

        bindService(intent, this, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (binder != null && binder.getService() != null) {
            binder.getService().checkServiceStatus();
        }

        unbindService(this);
    }

    @Override
    public void onServerSelected(Server server) {
        Intent intent = new Intent(this, ConversationActivity.class);

        if (server.getStatus() == Status.DISCONNECTED && !server.mayReconnect()) {
            server.setStatus(Status.PRE_CONNECTING);
            intent.putExtra(Extra.CONNECT, true);
        }

        intent.putExtra(Extra.SERVER_ID, server.getId());
        startActivity(intent);

    }

    @Override
    public IRCBinder getBinder() {
        return binder;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (IRCBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
    }
}
