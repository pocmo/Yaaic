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

import java.util.ArrayList;

import org.yaaic.R;
import org.yaaic.model.Extra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Adding auto join channels to a server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AddChannelActivity extends Activity implements OnClickListener, OnItemClickListener
{
    private EditText channelInput;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> channels;
    private Button okButton;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.channeladd);

        channelInput = (EditText) findViewById(R.id.channel);
        channelInput.setSelection(1);

        adapter = new ArrayAdapter<String>(this, R.layout.channelitem);

        ListView list = (ListView) findViewById(R.id.channels);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        ((Button) findViewById(R.id.add)).setOnClickListener(this);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);

        okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(this);
        okButton.setEnabled(false);

        channels = getIntent().getExtras().getStringArrayList(Extra.CHANNELS);

        for (String channel : channels) {
            adapter.add(channel);
        }
    }

    /**
     * On Click
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.add:
                String channel = channelInput.getText().toString().trim();
                channels.add(channel);
                adapter.add(channel);
                channelInput.setText("#");
                channelInput.setSelection(1);
                okButton.setEnabled(true);
                break;

            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.ok:
                // Get list and return as result
                Intent intent = new Intent();
                intent.putExtra(Extra.CHANNELS, channels);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    /**
     * On item clicked
     */
    @Override
    public void onItemClick(AdapterView<?> list, View item, int position, long id)
    {
        final String channel = adapter.getItem(position);

        String[] items = { getResources().getString(R.string.action_remove) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(channel);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: // Remove
                        adapter.remove(channel);
                        channels.remove(channel);
                        okButton.setEnabled(true);
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
