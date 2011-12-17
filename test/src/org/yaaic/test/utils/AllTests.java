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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All util tests 
 *  
 * @author Michael Kowalchuk <michael.kowalchuk@gmail.com>
 */
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Utils-Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ModeParserTest.class);
		//$JUnit-END$
		return suite;
	}
}
