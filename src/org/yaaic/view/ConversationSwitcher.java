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
package org.yaaic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * The ConversationSwitcher - The small funny dots at the top ;)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationSwitcher extends View
{
	/**
	 * Create a new ConversationSwitcher
	 * 
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 */
	public ConversationSwitcher(Context context)
	{
		super(context);
	}
	
	/**
	 * On draw
	 */
	protected void onDraw(Canvas canvas)
	{
		
	}
}
