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

import org.yaaic.R;
import org.yaaic.activity.ServersActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

/**
 * Scenario Tests for the ServersActivity
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
@SuppressWarnings("rawtypes")
public class ServerListScenarios extends ActivityInstrumentationTestCase2
{
	private Solo solo;
	private ScenarioHelper helper;
	
	/**
	 * Create a new ServerListScenarios instance
	 * 
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public ServerListScenarios() throws ClassNotFoundException
	{
	    super("org.yaaic", ServersActivity.class);
	}
	
	/**
	 * Setup test case
	 */
	@Override
	protected void setUp()
	{
		if (solo == null) {
			solo   = new Solo(getInstrumentation(), getActivity());
			helper = new ScenarioHelper(solo);
		}
	}
	
	/**
	 * Cleanup after run
	 */
	@Override
	protected void tearDown()
	{
		solo.finishOpenedActivities();
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
		// Delete Testserver if already exists
		helper.deleteTestServer();

		// Assert server does not exist
        assertFalse(solo.searchText("RobotiumTest"));

		// Add server
		View view = solo.getView(R.id.add);
		assert view != null;
		solo.clickOnView(view);
		
		solo.waitForActivity("AddServerActivity", 2000);
		
		solo.assertCurrentActivity("Switched to AddServerActivity", "AddServerActivity");
		
		solo.enterText(0, "RobotiumTest");
		solo.enterText(1, "irc.epd-me.net");
		
		solo.enterText(4, "YaaicBotium");

		// Somehow robotium doesn't detect the field when using index 5
		EditText realname = (EditText) solo.getView(R.id.realname);
		solo.enterText(realname, "Robotium and Yaaic");
		
		solo.clickOnButton(solo.getString(R.string.server_save));
		
		solo.waitForActivity("ServersActivity", 1000);
		solo.assertCurrentActivity("Switched back to ServersActivity", "ServersActivity");

		// Assert new server exists
		assertTrue(solo.searchText("RobotiumTest"));
		
		// Remove new server again
		solo.clickLongOnText("RobotiumTest");

		solo.clickOnText("Delete");
		
		solo.waitForActivity("ServersActivity", 1000);
		solo.assertCurrentActivity("Switched back to ServersActivity", "ServersActivity");
	}
}
