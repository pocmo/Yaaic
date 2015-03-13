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
 * An IRC channel (extends Conversation)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Channel extends Conversation
{
    private String topic;

    /**
     * Create a new channel object
     * 
     * @param name of the channel
     */
    public Channel(String name)
    {
        super(name);
        this.topic = "";
    }

    /**
     * Get the type of this conversation
     */
    @Override
    public int getType()
    {
        return Conversation.TYPE_CHANNEL;
    }

    /**
     * Set the channel's topic
     * 
     * @param topic The topic of the channel
     */
    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    /**
     * Get the topic of the channel
     * 
     * @return The channel's topic
     */
    public String getTopic()
    {
        return topic;
    }
}
