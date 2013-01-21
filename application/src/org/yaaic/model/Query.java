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

/**
 * A query (a private chat between to users)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Query extends Conversation
{
    /**
     * Create a new query
     * 
     * @param name The user's nickname
     */
    public Query(String name)
    {
        super(name);
    }

    /**
     * Get the type of this conversation
     */
    @Override
    public int getType()
    {
        return Conversation.TYPE_QUERY;
    }
}
