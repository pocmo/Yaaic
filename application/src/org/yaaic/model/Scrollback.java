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
package org.yaaic.model;

import java.util.LinkedList;

/**
 * Class for handling the scrollback history
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Scrollback
{
    public static final int MAX_HISTORY = 10;

    private final LinkedList<String> messages;
    private int pointer;

    /**
     * Create a new scrollback object
     */
    public Scrollback()
    {
        messages = new LinkedList<String>();
    }

    /**
     * Add a message to the history
     */
    public void addMessage(String message)
    {
        messages.addLast(message);

        if (messages.size() > MAX_HISTORY) {
            messages.removeFirst();
        }

        pointer = messages.size();
    }

    /**
     * Go back in history
     * 
     * @return
     */
    public String goBack()
    {
        if (pointer > 0) {
            pointer--;
        }

        if (messages.size() > 0) {
            return messages.get(pointer);
        }

        return null;
    }

    /**
     * Go forward in history
     * 
     * @return
     */
    public String goForward()
    {
        if (pointer < messages.size() - 1) {
            pointer++;
        } else {
            return "";
        }

        if (messages.size() > 0) {
            return messages.get(pointer);
        }

        return null;
    }
}
