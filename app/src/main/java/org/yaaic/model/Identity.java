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
import java.util.Collections;
import java.util.List;

/**
 * An identity containing a nickname, an ident and a real name
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Identity
{
    private String nickname;
    private final List<String> aliases = new ArrayList<String>();
    private String ident;
    private String realname;

    /**
     * Set the nickname of this identity
     * 
     * @param nickname The nickname to be set
     */
    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    /**
     * Get the nickname of this identity
     *
     * @return The nickname
     */
    public String getNickname()
    {
        return nickname;
    }

    /**
     * Set a collection of aliases for this identity
     *
     * @param aliases
     */
    public void setAliases(Collection<String> aliases)
    {
        this.aliases.clear();
        this.aliases.addAll(aliases);
    }

    /**
     * Get all aliases for this identity
     *
     * @return
     */
    public List<String> getAliases()
    {
        return Collections.unmodifiableList(aliases);
    }

    /**
     * Set the ident of this identity
     * 
     * @param ident The ident to be set
     */
    public void setIdent(String ident)
    {
        this.ident = ident;
    }

    /**
     * Get the ident of this identity
     * 
     * @return The identity
     */
    public String getIdent()
    {
        return ident;
    }

    /**
     * Set the real name of this identity
     * 
     * @param realname The real name to be set
     */
    public void setRealName(String realname)
    {
        this.realname = realname;
    }

    /**
     * Get the real name of this identity
     * 
     * @return The realname
     */
    public String getRealName()
    {
        return realname;
    }
}
