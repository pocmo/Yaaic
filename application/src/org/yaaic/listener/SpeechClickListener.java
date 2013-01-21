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
package org.yaaic.listener;

import org.yaaic.activity.ConversationActivity;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * OnClickListener for the Speech Recognition Button
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class SpeechClickListener implements OnClickListener
{
    private final Activity activity;

    /**
     * Create a new listener for speech button
     * 
     * @param activity
     * @param input
     */
    public SpeechClickListener(Activity activity)
    {
        this.activity = activity;
    }

    /**
     * On Click on speech button
     */
    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");

        activity.startActivityForResult(intent, ConversationActivity.REQUEST_CODE_SPEECH);
    }
}
