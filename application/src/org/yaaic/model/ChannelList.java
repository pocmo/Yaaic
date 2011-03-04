package org.yaaic.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class ChannelList extends Gallery
{
    public ChannelList(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

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
