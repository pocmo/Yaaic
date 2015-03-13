/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
package org.yaaic.indicator;


/**
 * Interface for a {@link ConversationStateProvider} that provides the apropriate
 * state color for a position in the pager.
 */
public interface ConversationStateProvider {
	/**
	 * Get the state color for all positions lower than the given position.
	 *
	 * @param position
	 * @return
	 */
    public int getColorForLowerThan(int position);

	/**
	 * Get the state color for the given position.
	 *
	 * @param position
	 * @return
	 */
	public int getColorAt(int position);

	/**
	 * Get the state color for all positions greater than the given position.
	 *
	 * @param position
	 * @return
	 */
	public int getColorForGreaterThan(int position);
}
