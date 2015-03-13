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
package org.yaaic.listener;

/**
 * Listener for conversations
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public interface ConversationListener
{
    /**
     * On new conversation message for given target
     *
     * @param target
     */
    public void onConversationMessage(String target);

    /**
     * On new conversation created (for given target)
     *
     * @param target
     */
    public void onNewConversation(String target);

    /**
     * On conversation removed (for given target)
     *
     * @param target
     */
    public void onRemoveConversation(String target);

    /**
     * On topic changed (for given target)
     *
     * @param target
     */
    public void onTopicChanged(String target);
}
