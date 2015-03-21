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
package org.yaaic.menu;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import org.yaaic.R;
import org.yaaic.adapter.ServersAdapter;
import org.yaaic.model.Server;

/**
 * Popup menu for the server cards.
 */
public class ServerPopupMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    private Server server;
    private ServersAdapter.ClickListener listener;

    public ServerPopupMenu(Context context, View anchor, ServersAdapter.ClickListener listener) {
        super(context, anchor);

        this.listener = listener;

        getMenuInflater().inflate(R.menu.context_server, getMenu());
        setOnMenuItemClickListener(this);

        anchor.setOnClickListener(this);
    }

    public void updateServer(Server server) {
        this.server = server;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.edit:
                listener.onEditServer(server);
                break;
            case R.id.delete:
                listener.onDeleteServer(server);
                break;
            case R.id.connect:
                listener.onConnectToServer(server);
                break;
            case R.id.disconnect:
                listener.onDisconnectFromServer(server);
                break;
        }

        return true;
    }

    public void onClick(View v) {
        show();
    }
}
