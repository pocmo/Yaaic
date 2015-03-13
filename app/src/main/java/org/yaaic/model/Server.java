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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.yaaic.R;

/**
 * A server as we know it
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Server
{
    private int id;
    private String title;
    private String host;
    private int port;
    private String password;
    private String charset;
    private boolean useSSL = false;

    private Identity identity;
    private Authentication authentication;

    private final LinkedHashMap<String, Conversation> conversations = new LinkedHashMap<String, Conversation>();
    private ArrayList<String> autoJoinChannels;
    private ArrayList<String> connectCommands;

    private int status = Status.DISCONNECTED;
    private String selected = "";
    private boolean isForeground = false;
    private boolean mayReconnect = false;

    /**
     * Create a new server object
     */
    public Server()
    {
        conversations.put(ServerInfo.DEFAULT_NAME, new ServerInfo());
        this.selected = ServerInfo.DEFAULT_NAME;
    }

    /**
     * Set the identity for this server
     * 
     * @param identity The identity for this server
     */
    public void setIdentity(Identity identity)
    {
        this.identity = identity;
    }

    /**
     * Set the authentication methods for this server
     *
     * @param authentication
     */
    public void setAuthentication(Authentication authentication)
    {
        this.authentication = authentication;
    }

    /**
     * Get the identity for this server
     * 
     * @return identity
     */
    public Identity getIdentity()
    {
        return identity;
    }

    /**
     * Get the authentication methods for this server;
     *
     * @return authentication
     */
    public Authentication getAuthentication()
    {
        return authentication;
    }

    /**
     * Get unique id of server
     * 
     * @return id
     */
    public int getId()
    {
        return id;
    }

    /**
     * Set unique id of server
     * 
     * @param id
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Set password of the server
     * 
     * @param password The password of the server
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Get the password of the server
     * 
     * @return The password of the server
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Get title of server
     * 
     * @return
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set title of server
     * 
     * @param title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Get hostname of server
     * 
     * @return
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Set hostname of server
     * 
     * @param host
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * Get port of server
     * 
     * @return
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Set port of server
     * 
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Set the charset to be used for all messages sent to the server
     * 
     * @param charset The name of the charset
     */
    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    /**
     * Get the charset to be used with this server
     * 
     * @return String charset The name of the charset
     */
    public String getCharset()
    {
        return charset;
    }

    /**
     * Set if this connections needs to use ssl
     */
    public void setUseSSL(boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    /**
     * Does this connection use SSL?
     * 
     * @return true if SSL should be used, false otherwise
     */
    public boolean useSSL()
    {
        return useSSL;
    }

    /**
     * Set connection status of server
     * 
     * @status See constants Status.*
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Get connection status of server
     * 
     * @return See constants Status.*
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Set list of channels to auto join after connect
     * 
     * @param channels List of channel names
     */
    public void setAutoJoinChannels(ArrayList<String> autoJoinChannels)
    {
        this.autoJoinChannels = autoJoinChannels;
    }

    /**
     * Get list of channels to auto join after connect
     * 
     * @return List of channel names
     */
    public ArrayList<String> getAutoJoinChannels()
    {
        return autoJoinChannels;
    }

    /**
     * Set commands to execute after connect
     * 
     * @param commands List of commands
     */
    public void setConnectCommands(ArrayList<String> connectCommands)
    {
        this.connectCommands = connectCommands;
    }

    /**
     * Get commands to execute after connect
     * 
     * @return List of commands
     */
    public ArrayList<String> getConnectCommands()
    {
        return connectCommands;
    }

    /**
     * Is disconnected?
     * 
     * @return true if the user is disconnected, false if the user is connected or currently connecting
     */
    public boolean isDisconnected()
    {
        return status == Status.DISCONNECTED;
    }

    /**
     * Is connected?
     * 
     * @return true if the user is (successfully) connected to this server, false otherwise
     */
    public boolean isConnected()
    {
        return status == Status.CONNECTED;
    }

    /**
     * Get all conversations
     * 
     * @return
     */
    public Collection<Conversation> getConversations()
    {
        return conversations.values();
    }

    /**
     * Get conversation by name
     */
    public Conversation getConversation(String name)
    {
        return conversations.get(name.toLowerCase());
    }

    /**
     * Add a new conversation
     * 
     * @param conversation The conversation to add
     */
    public void addConversation(Conversation conversation)
    {
        conversations.put(conversation.getName().toLowerCase(), conversation);
    }

    /**
     * Removes a conversation by name
     * 
     * @param name
     */
    public void removeConversation(String name)
    {
        conversations.remove(name.toLowerCase());
    }

    /**
     * Remove all conversations
     */
    public void clearConversations()
    {
        conversations.clear();

        // reset defaults
        conversations.put(ServerInfo.DEFAULT_NAME, new ServerInfo());
        this.selected = ServerInfo.DEFAULT_NAME;
    }

    /**
     * Set name of currently selected conversation
     * 
     * @param selected The name of the selected conversation
     */
    public void setSelectedConversation(String selected)
    {
        this.selected = selected;
    }

    /**
     * Get name of currently selected conversation
     * 
     * @return The name of the selected conversation
     */
    public String getSelectedConversation()
    {
        return selected;
    }

    /**
     * Get names of the currently joined channels
     * 
     * @return
     */
    public ArrayList<String> getCurrentChannelNames()
    {
        ArrayList<String> channels = new ArrayList<String>();
        Collection<Conversation> mConversations = conversations.values();

        for (Conversation conversation : mConversations) {
            if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                channels.add(conversation.getName());
            }
        }

        return channels;
    }

    /**
     * Get icon for current server status
     * 
     * @return int Status icon ressource
     */
    public int getStatusIcon()
    {
        switch (status) {
            case Status.CONNECTED:
                return R.drawable.connected;
            case Status.DISCONNECTED:
                return R.drawable.disconnected;
            case Status.PRE_CONNECTING:
            case Status.CONNECTING:
                return R.drawable.connecting;
        }

        return R.drawable.connecting;
    }

    /**
     * Get whether a ConversationActivity for this server is currently in the foreground.
     */
    public boolean getIsForeground()
    {
        return isForeground;
    }

    /**
     * Set whether a ConversationActivity for this server is currently in the foreground.
     */
    public void setIsForeground(boolean isForeground)
    {
        this.isForeground = isForeground;
    }

    /**
     * Get whether a reconnect may be attempted if we're disconnected.
     */
    public boolean mayReconnect()
    {
        return mayReconnect;
    }

    /**
     * Set whether a reconnect may be attempted if we're disconnected.
     */
    public void setMayReconnect(boolean mayReconnect)
    {
        this.mayReconnect = mayReconnect;
    }
}
