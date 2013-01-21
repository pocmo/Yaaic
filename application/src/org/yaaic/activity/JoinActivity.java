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
package org.yaaic.activity;

import org.yaaic.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Small dialog to show an edittext for joining channels
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 *
 */
public class JoinActivity extends Activity implements OnClickListener
{
    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.join);

        ((Button) findViewById(R.id.join)).setOnClickListener(this);

        ((EditText) findViewById(R.id.channel)).setSelection(1);
    }

    /**
     * On click
     */
    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent();
        intent.putExtra("channel", ((EditText) findViewById(R.id.channel)).getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
