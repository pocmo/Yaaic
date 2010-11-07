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
package org.yaaic.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All tests for Yaaic (org.yaaic.test.*)
 *  
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.yaaic.test");
		//$JUnit-BEGIN$
		suite.addTest(org.yaaic.test.model.AllTests.suite());
		suite.addTest(org.yaaic.test.receiver.AllTests.suite());
		//$JUnit-END$
		return suite;
	}

}
