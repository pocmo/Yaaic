/*
 Yaaic - Yet Another Android IRC Client

Copyright 2009 Sebastian Kaspari

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

public class Broadcast
{
	public static final String SERVER_UPDATE 	= "org.yaaic.server.status";
	
	public static final String CHANNEL_MESSAGE	= "org.yaaic.channel.message";
	public static final String CHANNEL_NEW		= "org.yaaic.channel.new";
	public static final String CHANNEL_REMOVE	= "org.yaaic.channel.remove";
	
	public static final String EXTRA_CHANNEL	= "channel";
}
