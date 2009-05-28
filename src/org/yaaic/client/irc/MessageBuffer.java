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
package org.yaaic.client.irc;

import java.util.ArrayList;

import android.text.SpannableString;

/**
 * A MessageBuffer for storing SpannableString Objects
 * 
 * @author Sebastian Kaspari <pocmo@yaaic.org>
 */
public class MessageBuffer
{
	private ArrayList<SpannableString> messageBuffer;
	
	/**
	 * Maximum Buffer Size
	 */
	private static final int MAX_SIZE = 30;
	
	/**
	 * Maximum Buffer Size in Emergencies, e.g. if we are low on memory
	 */
	private static final int EMERGENCY_MAX_SIZE = 10;
	
	/**
	 * Constructor - Create a fresh ne MessageBuffer
	 */
	public MessageBuffer()
	{
		messageBuffer = new ArrayList<SpannableString>();
	}
	
	/**
	 * Add a SpannableString to the Buffer
	 * @param s
	 * @return
	 */
	public boolean add(SpannableString s)
	{
		if (messageBuffer.size() > MAX_SIZE) {
			messageBuffer.remove(0);
		}
		return messageBuffer.add(s);
	}
	
	/**
	 * Truncate the buffer to the maximum size (emergency)
	 * 
	 * Use this if we are low on memory
	 */
	public void truncate()
	{
		while (messageBuffer.size() > EMERGENCY_MAX_SIZE) {
			messageBuffer.remove(0);
		}
	}
}
