/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2010 Sebastian Kaspari

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
package org.yaaic.test.model;

import org.yaaic.model.Message;

import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Message
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageTest extends TestCase
{
	private Message message;
	private String text = "<pocmo> Hello World!";
	
	@Override
	protected void setUp()
	{
		this.message = new Message(text);
	}
	
	public void testSetAndGetIcon()
	{
		assertEquals(-1, message.getIcon());
		
		message.setIcon(25); // Normally we use an ressource id
		assertEquals(25, message.getIcon());
		
		message.setIcon(-1); // The message class uses -1 as "no icon"
		assertEquals(-1, message.getIcon());
	}
	
	public void testGetText()
	{
		assertEquals(text, message.getText());
	}
	
	public void testTimestamp()
	{
		Message message = new Message("");

		message.setTimestamp(1270505773862l);

		assertEquals("[00:16] ", message.renderTimeStamp(false));
		assertEquals("[00:16] ", message.renderTimeStamp(true));

		message.setTimestamp(1270908275000l);

		assertEquals("[04:04] ", message.renderTimeStamp(false));
		assertEquals("[16:04] ", message.renderTimeStamp(true));
	}
}
