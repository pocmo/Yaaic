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

/**
 * A channel or server message
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Message {
	private int icon;
	private String text;
	
	/**
	 * Create a new message without an icon
	 * 
	 * @param text
	 */
	public Message(String text)
	{
		this.text = text;
		this.icon = -1;
	}
	
	/**
	 * Create a new message with an icon
	 * 
	 * @param icon
	 * @param text
	 */
	public Message(int icon, String text)
	{
		this.icon = icon;
		this.text = text;
	}
	
	/**
	 * Get the message's icon
	 * 
	 * @return
	 */
	public int getIcon()
	{
		return icon;
	}
	
	/**
	 * Does this message have an icon?
	 * 
	 * @return
	 */
	public boolean hasIcon()
	{
		return icon != -1;
	}
	
	/**
	 * Get the text of this message
	 * 
	 * @return
	 */
	public String getText()
	{
		return text;
	}
}
