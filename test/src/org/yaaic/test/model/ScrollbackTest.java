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

import org.yaaic.model.Scrollback;

import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Scrollback
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ScrollbackTest extends TestCase
{
	private Scrollback scrollback;
	
	@Override
	public void setUp()
	{
		scrollback = new Scrollback();
	}
	
	public void testEmptyHistoryBack()
	{
		assertNull(scrollback.goBack());
		assertNull(scrollback.goBack());
	}
	
	public void testEmptyHistoryForward()
	{
		assertEquals("", scrollback.goForward());
		assertEquals("", scrollback.goForward());
	}
	
	public void testHistory()
	{
		scrollback.addMessage("Message One");
		
		assertEquals("Message One", scrollback.goBack());
		assertEquals("Message One", scrollback.goBack());
		
		assertEquals("", scrollback.goForward());
		
		scrollback.addMessage("Message Two");
		assertEquals("Message Two", scrollback.goBack());
		assertEquals("Message One", scrollback.goBack());
		
		assertEquals("Message Two", scrollback.goForward());
		assertEquals("", scrollback.goForward());
		
		scrollback.addMessage("Message Three");
		scrollback.addMessage("Message Four");

		assertEquals("Message Four", scrollback.goBack());
		assertEquals("Message Three", scrollback.goBack());
		assertEquals("Message Two", scrollback.goBack());
		assertEquals("Message One", scrollback.goBack());
		assertEquals("Message One", scrollback.goBack());
		
		assertEquals("Message Two", scrollback.goForward());
		assertEquals("Message Three", scrollback.goForward());
		assertEquals("Message Four", scrollback.goForward());
		assertEquals("", scrollback.goForward());
	}
}
