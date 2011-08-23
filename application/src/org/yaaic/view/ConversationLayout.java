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

import org.yaaic.R;
import org.yaaic.model.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * ConversationLayout: LinearLayout that resizes correctly when an IME
 * comes up in fullscreen mode
 * 
 * @author Steven Luo <steven+android@steven676.net>
 */
public class ConversationLayout extends LinearLayout
{
    private Activity activity;

    int curHeight = 0;
    boolean fullscreen = false;
    boolean isLandscape = false;
    boolean redoLayout = false;

    /**
     * Create a new conversation linear layout
     * 
     * @param context
     */
    public ConversationLayout(Context context)
    {
        super(context);
        doInit(context);
    }

    /**
     * Create a new conversation linear layout
     * 
     * @param context
     * @param attrs
     */
    public ConversationLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        doInit(context);
    }

    /**
     * Initialize the ConversationLayout
     */
    private void doInit(Context context)
    {
        activity = (Activity) context;
        fullscreen = (new Settings(context)).fullscreenConversations();
        isLandscape = (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Get the height of the window's visible area
     */
    private int getWindowHeight()
    {
        Rect visible = new Rect();
        getWindowVisibleDisplayFrame(visible);
        return visible.height();
    }

    /**
     * Check if starving the gui is necessary, and starves
     * Starves when less then a vertical inch is available to us
     *
     * @return true if we are able to check, false if not.
     * @author Reynaldo Cortorreal <reyncor@gmail.com>
     */
    private boolean setStarvationMode(int height)
    {
        if (height == 0 || height == curHeight) {
            return false;
        }

        LinearLayout status = (LinearLayout) findViewById(R.id.status_layout);
        ConversationSwitcher dots = (ConversationSwitcher) findViewById(R.id.dots);

        float scale = getResources().getDisplayMetrics().density;

        //Give us at least an inch, or we'll have to make sacrifices.
        if (height < 160*scale) {
            status.setVisibility(GONE);
            dots.setVisibility(GONE);
        } else {
            status.setVisibility(VISIBLE);
            dots.setVisibility(VISIBLE);
        }
        return true;
    }

    /**
     * Adjust the height of the view to avoid scrolling and hide UI components
     * if necessary to save space
     *
     * @author Steven Luo <steven+android@steven676.net>
     * @author Reynaldo Cortorreal <reyncor@gmail.com>
     */
    private void adjustHeight()
    {
        int height = getWindowHeight();

        if (!fullscreen) {
            if (setStarvationMode(height)) {
                curHeight = height;
                redoLayout = true;
            }
            return;
        }

        //here to forth the code applies only to full screen
        if (isLandscape && !setStarvationMode(height)) {
            return;
        } else if (curHeight != height && height != 0) {
            curHeight = height;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT,
                height
            );

            params.gravity = Gravity.BOTTOM | Gravity.CLIP_VERTICAL;
            setLayoutParams(params);
            redoLayout = true;
        }
    }

    /**
     * onMeasure (ask the view how much space it wants)
     * This is called when the window size changes, so we can hook into it to
     * resize ourselves when the IME comes up
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        adjustHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * onDraw (draw the view)
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        if (redoLayout) {
            // Layout params have changed -- force a layout update
            requestLayout();
            redoLayout = false;
        }
        super.onDraw(canvas);
    }
}
