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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Adding commands (to execute after connect) to a server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AddCommandsActivity extends Activity implements OnClickListener, OnItemClickListener
{
    private EditText commandInput;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> commands;
    private Button okButton;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.commandadd);

        commandInput = (EditText) findViewById(R.id.command);

        adapter = new ArrayAdapter<String>(this, R.layout.commanditem);

        ListView list = (ListView) findViewById(R.id.commands);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        ((Button) findViewById(R.id.add)).setOnClickListener(this);
        ((Button) findViewById(R.id.ok)).setOnClickListener(this);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);

        okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(this);
        okButton.setEnabled(false);

        commands = getIntent().getExtras().getStringArrayList(Extra.COMMANDS);

        for (String command : commands) {
            adapter.add(command);
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
                String command = commandInput.getText().toString().trim();

                if (!command.startsWith("/")) {
                    command = "/" + command;
                }

                commands.add(command);
                adapter.add(command);
                commandInput.setText("/");
                okButton.setEnabled(true);
                break;

            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.ok:
                // Get list and return as result
                Intent intent = new Intent();
                intent.putExtra(Extra.COMMANDS, commands);
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
        final String command = adapter.getItem(position);

        String[] items = { getResources().getString(R.string.action_remove) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(command);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: // Remove
                        adapter.remove(command);
                        commands.remove(command);
                        okButton.setEnabled(true);
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
