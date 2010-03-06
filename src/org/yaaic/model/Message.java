/*
 Yaaic - Yet Another Android IRC Client

Copyright 2009 Sebastian Kaspari

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
package org.yaaic.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

/**
 * A channel or server message
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Message {
	private int icon;
	private String text;
	private SpannableString canvas;
	
	/**
	 * Create a new message without an icon
	 * 
	 * @param text
	 */
	public Message(String text)
	{
		this.text = text;
		this.icon = -1;
	}
	
	/**
	 * Set the message's icon
	 */
	public void setIcon(int icon)
	{
		this.icon = icon;
	}
	
	/**
	 * Get the message's icon
	 * 
	 * @return
	 */
	public int getIcon()
	{
		return icon;
	}
	
	/**
	 * Does this message have an icon?
	 * 
	 * @return
	 */
	public boolean hasIcon()
	{
		return icon != -1;
	}
	
	/**
	 * Get the text of this message
	 * 
	 * @return
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * Render message as spannable string
	 * 
	 * @return
	 */
	public SpannableString render(Context context)
	{
		if (canvas == null) {
			canvas = new SpannableString("\n " + text);
			if (hasIcon()) {
				Drawable drawable = context.getResources().getDrawable(icon);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				canvas.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
		return canvas;
	}
}
