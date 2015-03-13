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

import java.util.Arrays;

import org.yaaic.R;
import org.yaaic.model.Extra;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

/**
 * User Activity - Shows a list of users in the current channel
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class UsersActivity extends ListActivity implements OnItemClickListener
{
    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.users);

        final String[] users = getIntent().getExtras().getStringArray(Extra.USERS);
        getListView().setOnItemClickListener(this);

        // Add sorted list of users in own thread to avoid blocking UI
        // TODO: Move to a background task and show loading indicator while sorting
        Arrays.sort(users, String.CASE_INSENSITIVE_ORDER);
        getListView().setAdapter(new ArrayAdapter<String>(UsersActivity.this, R.layout.useritem, users));
    }

    /**
     * On user selected
     */
    @Override
    public void onItemClick(AdapterView<?> list, View item, int position, long id)
    {
        Intent intent = new Intent();
        intent.putExtra(Extra.USER, (String) getListView().getAdapter().getItem(position));
        setResult(RESULT_OK, intent);
        finish();
    }
}
