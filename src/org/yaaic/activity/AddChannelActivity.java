/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2010 Sebastian Kaspari

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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Adding channels to a server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AddChannelActivity extends Activity implements OnClickListener
{
	private EditText channelInput;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> channels;
	
	/**
	 * On create
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.channeladd);
		
		channelInput = (EditText) findViewById(R.id.channel);
		
		adapter = new ArrayAdapter<String>(this, R.layout.channelitem);
		((ListView) findViewById(R.id.channels)).setAdapter(adapter);
		
		((Button) findViewById(R.id.add)).setOnClickListener(this);
		((Button) findViewById(R.id.ok)).setOnClickListener(this);
		((Button) findViewById(R.id.cancel)).setOnClickListener(this);
		
		channels = getIntent().getExtras().getStringArrayList(Extra.CHANNELS);
		
		for (String channel : channels) {
			adapter.add(channel);
		}
	}

	/**
	 * On Click
	 */
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.add:
				String channel = channelInput.getText().toString();
				channels.add(channel);
				adapter.add(channel);
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
}
