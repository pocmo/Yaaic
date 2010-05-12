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

import java.util.Collection;

import org.yaaic.model.Conversation;
import org.yaaic.model.Server;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * The ConversationSwitcher - The small funny dots at the top ;)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationSwitcher extends View
{
	private static final boolean DEBUG_MODE = false;
	
	private Server server;
	private Paint paint;
	
	/**
	 * Create a new ConversationSwitcher
	 * 
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 */
	public ConversationSwitcher(Context context, AttributeSet attributes)
	{
		super(context, attributes);
		
		paint = new Paint();
	}
	
	/**
	 * Set the server whos conversations should be displayed
	 * 
	 * @param server
	 */
	public void setServer(Server server)
	{
		this.server = server;
	}
	
	/**
	 * Measure the size of the view
	 * 
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = MeasureSpec.getSize(widthMeasureSpec);
		Log.d("Yaaic", width + " x " + 20);
		setMeasuredDimension(width, 20);
	}
	
	/**
	 * On draw
	 */
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	
		if (DEBUG_MODE) {
			// Draw debug lines
			paint.setColor(0xFFFF0000);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawRect(new Rect(0, 0, getWidth() - 1, getHeight() - 1), paint);
		}
		
		//Log.d("Yaaic", "Drawing...");
		
		if (server == null) {
			return;
		}

		int width = getWidth();
		int height = getHeight();
		
		Collection<Conversation> conversations = server.getConversations();
		int circles = conversations.size();
		
		int startX = width / 2 - circles * 14;
		
		paint.setColor(0xFFDDDDDD);
		paint.setStyle(Paint.Style.FILL);
		
		int i = 0;
		
		for (Conversation conversation : conversations) {
			switch (conversation.getStatus()) {
				case Conversation.STATUS_DEFAULT:
					paint.setColor(0xFF888888);
					break;
				case Conversation.STATUS_HIGHLIGHT:
					paint.setColor(0xFFDD0000);
					break;
				case Conversation.STATUS_MESSAGE:
					paint.setColor(0xFF00DD00);
					break;
				case Conversation.STATUS_SELECTED:
					paint.setColor(0xFFFFFFFF);
					break;
			}
			
			canvas.drawCircle(startX + 14 * i, height / 2, 5, paint);
			i++;
		}
		
	}
}
