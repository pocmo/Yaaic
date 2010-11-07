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

import org.yaaic.model.Identity;

import junit.framework.TestCase;

/**
 * Test case for org.yaaic.model.Identity
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IdentityTest extends TestCase
{
	private Identity identity;
	
	@Override
	protected void setUp()
	{
		this.identity = new Identity();
	}
	
	public void testSetAndGetNickname()
	{
		assertNull(identity.getNickname());
		
		String nickname = "InvaderZim";
		identity.setNickname(nickname);
		assertEquals(nickname, identity.getNickname());
	}
	
	public void testSetAndGetIdent()
	{
		assertNull(identity.getIdent());
		
		String ident = "invader";
		identity.setIdent(ident);
		assertEquals(ident, identity.getIdent());
	}
	
	public void testSetAndGetRealName()
	{
		assertNull(identity.getRealName());
		
		String realname = "Invader Zim";
		identity.setRealName(realname);
		assertEquals(realname, identity.getRealName());
	}
}
