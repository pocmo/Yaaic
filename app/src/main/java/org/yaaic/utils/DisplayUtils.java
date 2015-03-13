/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
package org.yaaic.utils;

import android.content.Context;

/**
 * Helper class for methods regarding the display of the current device.
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class DisplayUtils
{
    private static float density = -1;

    /**
     * Convert the given density-independent pixels into real pixels for the
     * display of the device.
     *
     * @param dp
     * @return
     */
    public static int convertToPixels(Context context, int dp) {
        float density = getScreenDensity(context);

        return (int) (dp * density + 0.5f);
    }

    /**
     * Get the density of the display of the device.
     *
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context) {
        if (density == -1) {
            density = context.getResources().getDisplayMetrics().density;
        }

        return density;
    }
}
