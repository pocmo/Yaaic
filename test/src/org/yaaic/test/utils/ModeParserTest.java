/*
Yaaic - Yet Another Android IRC Client

Copyright 2011 Michael Kowalchuk

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
package org.yaaic.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.yaaic.utils.ModeParser;
import org.yaaic.utils.ModeParser.ChannelModeReply;
import org.yaaic.utils.ModeParser.ModeChange;
import org.yaaic.utils.ModeParser.InvalidModeStringException;

public class ModeParserTest extends TestCase {

	public void testEmptyModeString() throws InvalidModeStringException
	{
		List<ModeChange> modeChanges = ModeParser.parseModeChanges("+", new ArrayList<String>());
		assertEquals( 0, modeChanges.size() );
	}
	
	public void testAddingAction() throws InvalidModeStringException
	{
		List<ModeChange> modeChanges = ModeParser.parseModeChanges("+s", new ArrayList<String>());
		assertEquals( 1, modeChanges.size() );
		
		ModeChange modeChange = modeChanges.get(0);
		assertEquals( 's', modeChange.getMode());
		assertEquals( ModeChange.ACTION.ADDING_MODE, modeChange.getAction() );
	}
	
	public void testRemovingAction() throws InvalidModeStringException
	{
		List<ModeChange> modeChanges = ModeParser.parseModeChanges("-s", new ArrayList<String>());
		assertEquals( 1, modeChanges.size() );
		
		ModeChange modeChange = modeChanges.get(0);
		assertEquals( 's', modeChange.getMode());
		assertEquals( ModeChange.ACTION.REMOVING_MODE, modeChange.getAction() );
	}
	
	public void testMissingParameter()
	{
		try {
			ModeParser.parseModeChanges("+k", new ArrayList<String>());
			fail();
		}
		catch(InvalidModeStringException ime) {
		}
	}
	
	public void testChannelName() throws InvalidModeStringException
	{
		String replyString = "testuser #test +ns";
	    ChannelModeReply reply = ModeParser.parseChannelModeReply(replyString);
	    assertEquals("#test", reply.getChannelName());
	}
	
	public void testNoModesSpecified() throws InvalidModeStringException
	{
		String replyString = "testuser #test +";
		ChannelModeReply reply = ModeParser.parseChannelModeReply(replyString);
		assertEquals( 0, reply.getChannelModes().size());
	}
	
	public void testModesWithoutParameters() throws InvalidModeStringException
	{
		String replyString = "testuser #test +ns";
	    ChannelModeReply reply = ModeParser.parseChannelModeReply(replyString);
	    Map<Character, String> modes = reply.getChannelModes();
	    assertTrue( modes.containsKey('n'));
	    assertTrue( modes.containsKey('s'));
	}
	
	public void testModesWithParameters() throws InvalidModeStringException
	{
		String replyString = "testuser #test +ks passkey";
	    ChannelModeReply reply = ModeParser.parseChannelModeReply(replyString);
	    Map<Character, String> modes = reply.getChannelModes();
	    assertTrue( modes.containsKey('s'));
	    assertEquals( "passkey", modes.get('k') );
	}
	
	public void testMissingModes()
	{
		String replyString = "testuser #test";
		try {
			ModeParser.parseChannelModeReply(replyString);
			fail();
		}
		catch(InvalidModeStringException ime) {
		}
	}
	
	
}
