/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

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
package org.yaaic.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Wrapper for background drawables that should not be scaled
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class NonScalingBackgroundDrawable extends Drawable
{
    private final View view;
    private final Drawable drawable;

    /**
     * Create a new non scaling background drawable
     * 
     * @param context
     * @param view
     * @param resource
     */
    public NonScalingBackgroundDrawable(Context context, View view, int resource)
    {
        this.view = view;
        this.drawable = context.getResources().getDrawable(resource);
    }

    /**
     * Draw the background drawable
     */
    @Override
    public void draw(Canvas canvas)
    {
        int left = (view.getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
        int top = (view.getHeight() / 2) - (drawable.getIntrinsicHeight() / 2);
        int right = left + drawable.getIntrinsicWidth();
        int bottom = top + drawable.getIntrinsicHeight();

        drawable.setBounds(left, top, right, bottom);

        drawable.draw(canvas);
    }

    /**
     * Get the opacity
     */
    @Override
    public int getOpacity()
    {
        return drawable.getOpacity();
    }

    /**
     * Set the alpha
     */
    @Override
    public void setAlpha(int alpha)
    {
        drawable.setAlpha(alpha);
    }

    /**
     * Set the color filter
     */
    @Override
    public void setColorFilter(ColorFilter cf)
    {
        drawable.setColorFilter(cf);
    }
}
