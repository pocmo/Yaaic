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

import org.yaaic.adapter.MessageListAdapter;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;
import android.widget.ListView;

/**
 * A customized ListView for Messages 
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageListView extends ListView
{
	public static final String TAG = "Yaaic/MessageListView";
	
	private boolean delegate = true;
	private View parent;
	private int parentWidth;
	private int parentHeight;
	
	/**
	 * Create a new MessageListView
	 * 
	 * @param context
	 */
	public MessageListView(Context context, View parent)
	{
		super(context);
		
		this.parent = parent;
		
		parentWidth = parent.getWidth();
		parentHeight = parent.getHeight();
	}
	
	/**
	 * Should all touch events delegated?
	 * 
	 * @param delegate If true all touch events will be delegated, otherwise the listview will handle them
	 */
	public void setDelegateTouchEvents(boolean delegate)
	{
		this.delegate = delegate;
	}
	
	/**
	 * Handle touch screen motion events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (delegate) {
			// We delegate the touch events to the underlying view 
			return false;
		} else {
			return super.onTouchEvent(event);
		}
	}

	/**
	 * On draw
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		if (delegate && (parent.getWidth() != parentWidth || parent.getHeight() != parentHeight)) {
			// parent size changed, resizing this child too
			
			parentWidth = parent.getWidth();
			parentHeight = parent.getHeight();
			
			this.setLayoutParams(new Gallery.LayoutParams(
				parent.getWidth() / 100 * 85,
				parent.getHeight()
			));
		}
		
		super.onDraw(canvas);
	}
	
	/**
	 * Get the adapter of this MessageListView
	 * (Helper to avoid casting)
	 * 
	 * @return The MessageListAdapter
	 */
	public MessageListAdapter getAdapter()
	{
		return (MessageListAdapter) super.getAdapter();
	}
}
