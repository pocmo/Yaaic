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
package org.yaaic.view;

import android.view.View;

import org.yaaic.model.Channel;

/**
 * A channel container to group a channel object with a channel view 
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ChannelContainer
{
	private Channel channel;
	private View canvas;
	
	/**
	 * Create a new channel container
	 * 
	 * @param name The channel object
	 * @param canvas View of the channel
	 */
	public ChannelContainer(Channel channel, View canvas)
	{
		this.channel = channel;
		this.canvas = canvas;
	}
	
	/**
	 * Get the view of the channel
	 * 
	 * @return The channel view
	 */
	public View getCanvas()
	{
		return canvas;
	}
	
	/**
	 * Get the channel object
	 * 
	 * @return The channel object
	 */
	public Channel getChannel()
	{
		return channel;
	}
}
