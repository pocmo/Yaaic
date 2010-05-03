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
import org.yaaic.model.Extra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * UserActivity
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class UserActivity extends Activity implements OnClickListener
{
	private String nickname;
	
	/**
	 * On create
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		nickname = getIntent().getStringExtra(Extra.USER);
		setContentView(R.layout.user);
		
		// Use full width
		LayoutParams params = getWindow().getAttributes();
		params.width = WindowManager.LayoutParams.FILL_PARENT;
		getWindow().setAttributes(params);
		
		((Button) findViewById(R.id.op)).setOnClickListener(this);
		((Button) findViewById(R.id.deop)).setOnClickListener(this);
		((Button) findViewById(R.id.voice)).setOnClickListener(this);
		((Button) findViewById(R.id.devoice)).setOnClickListener(this);
		((Button) findViewById(R.id.kick)).setOnClickListener(this);
		((Button) findViewById(R.id.ban)).setOnClickListener(this);
		
		((TextView) findViewById(R.id.nickname)).setText(nickname);
	}

	/**
	 * On button click
	 */
	public void onClick(View v)
	{
		Intent intent = new Intent();
		intent.putExtra(Extra.ACTION, v.getId());
		intent.putExtra(Extra.USER, nickname);
		setResult(RESULT_OK, intent);
		finish();
	}
}
