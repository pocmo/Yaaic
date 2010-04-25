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

import org.yaaic.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Adding channels to a server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AddChannelActivity extends Activity implements OnClickListener
{
	private ListView channelList;
	private EditText channelInput;
	private ArrayAdapter<String> adapter;
	
	/**
	 * On create
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.channeladd);
		
		channelList = (ListView) findViewById(R.id.channels);
		channelInput = (EditText) findViewById(R.id.channel);
		
		adapter = new ArrayAdapter<String>(this, R.layout.channelitem);
		channelList.setAdapter(adapter);
		
		((Button) findViewById(R.id.add)).setOnClickListener(this);
		((Button) findViewById(R.id.save)).setOnClickListener(this);
		((Button) findViewById(R.id.cancel)).setOnClickListener(this);
	}

	/**
	 * On Click
	 */
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.add:
				adapter.add(channelInput.getText().toString());
				break;
			case R.id.cancel:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case R.id.save:
				// Get list and return as result
				
				setResult(RESULT_OK);
				finish();
				break;
		}
	}
}
