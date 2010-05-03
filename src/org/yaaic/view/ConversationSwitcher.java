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
		
		// Draw debug lines
		paint.setColor(0xFFFF0000);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(new Rect(0, 0, getWidth() - 1, getHeight() - 1), paint);
		
		Log.d("Yaaic", "Circle at " + (getWidth() / 2) + " x " + (getHeight() / 2));

		// Draw dots
		paint.setColor(0xFFFFFFFF);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, 5, paint);
	}
}
