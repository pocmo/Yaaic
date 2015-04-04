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

import android.support.v7.widget.Toolbar;

import org.yaaic.irc.IRCBinder;
import org.yaaic.model.Server;

/**
 * Interface for fragments accessing functionality of the main activity.
 */
public interface YaaicActivity {
    IRCBinder getBinder();

    Toolbar getToolbar();

    void setToolbarTitle(String title);

    void onServerSelected(Server server);
}
