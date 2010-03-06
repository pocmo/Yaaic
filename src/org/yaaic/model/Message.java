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
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

/**
 * A channel or server message
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Message {
	public static final int COLOR_GREEN = 0xFF458509;
	public static final int COLOR_RED   = 0xFFcc0000;
	public static final int COLOR_BLUE  = 0xFF729fcf;
	
	private int icon = -1;
	private String text;
	private SpannableString canvas;
	private int color = -1;
	
	/**
	 * Create a new message without an icon
	 * 
	 * @param text
	 */
	public Message(String text)
	{
		this.text = text;
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
	 * Set the color of this message
	 */
	public void setColor(int color)
	{
		this.color = color;
	}
	
	/**
	 * Render message as spannable string
	 * 
	 * @return
	 */
	public SpannableString render(Context context)
	{
		if (canvas == null) {
			String prefix = hasIcon() ? "\n  " : "\n";
			canvas = new SpannableString(prefix + text);
			if (hasIcon()) {
				Drawable drawable = context.getResources().getDrawable(icon);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				canvas.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (color != -1) {
				canvas.setSpan(new ForegroundColorSpan(color), 0, canvas.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
		return canvas;
	}
}
