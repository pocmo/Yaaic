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
package org.yaaic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * Conversation gallery for horizontal scrolling
 * 
 * @author Thomas Martitz
 */
public class ConversationGallery extends Gallery
{
    /**
     * Create a new conversation gallery
     * 
     * @param context
     * @param attrs
     */
    public ConversationGallery(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * On Fling: Reduce sensitivity of the channel gallery view
     */
    @Override
    public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        /* Reduce sensitivity based on f(x) = x * 0.925^y with y = (|x/500| - 1)
         * The goal is to reduce higher velocities stronger than low ones
         * 500 is the base, i.e. it will not reduced. */
        final float fexp = Math.abs(velocityX / 500.0f) - 1;
        velocityX *= Math.pow(0.925, fexp);

        return super.onFling(e1, e2, velocityX, velocityY);
    }
}
