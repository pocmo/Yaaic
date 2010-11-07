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
package org.yaaic.test.scenario;

import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Scenario Tests for the ServersActivity
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
@SuppressWarnings("rawtypes")
public class ServerListScenarios extends ActivityInstrumentationTestCase2
{
	private Solo solo;
	
	@SuppressWarnings("unchecked")
	public ServerListScenarios() throws ClassNotFoundException
	{
		super(
			"org.yaaic",
			Class.forName("org.yaaic.activity.ServersActivity")
		);
	}
	
	protected void setUp()
	{
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * Test-Scenario:
	 * 
	 * Add server:
	 * - Select "Add server" from the menu
	 * - Add all necessary information
	 * - Click on Save
	 * - The new server appears in the list
	 * 
	 * Remove server:
	 * - Long press on the server in the list
	 * - Select delete
	 * - The server is no longer in the list
	 */
	public void testAddingAndRemovingServer()
	{
		int numberOfServersBefore = solo.getCurrentListViews().get(0).getCount();
		
		// Add server
		solo.pressMenuItem(0);
		
		solo.waitForActivity("AddServerActivity", 2000);
		
		solo.assertCurrentActivity("Switched to AddServerActivity", "AddServerActivity");
		
		solo.enterText(0, "RobotiumTest");
		solo.enterText(1, "irc.epd-me.net");
		
		solo.enterText(4, "YaaicBotium");
		solo.enterText(6, "Robotium and Yaaic");
		
		solo.clickOnButton(4);
		
		solo.waitForActivity("ServersActivity", 2000);
		solo.assertCurrentActivity("Switched back to ServersActivity", "ServersActivity");

		// Assert new server exists
		int numberOfServersAfter = solo.getCurrentListViews().get(0).getCount();
		assertEquals(numberOfServersBefore + 1, numberOfServersAfter);
		assertTrue(solo.searchText("RobotiumTest"));
		
		// Remove new server again
		solo.clickLongOnText("RobotiumTest");
		
		solo.clickOnText("Delete");
		
		solo.waitForActivity("ServersActivity", 2000);
		solo.assertCurrentActivity("Switched back to ServersActivity", "ServersActivity");

		// Assert server is gone again
		numberOfServersAfter = solo.getCurrentListViews().get(0).getCount();
		assertEquals(numberOfServersBefore, numberOfServersAfter);
		assertFalse(solo.searchText("RobotiumTest"));
	}
}
