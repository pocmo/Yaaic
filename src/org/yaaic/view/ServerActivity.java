/*
 Yaaic - Yet Another Android IRC Client

Copyright 2009 Sebastian Kaspari

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
import org.yaaic.Yaaic;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.FlingListener;
import org.yaaic.model.Server;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Connected to server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerActivity extends Activity implements ServiceConnection
{
	protected static final String TextView = null;
	private IRCBinder binder;
	private int serverId;
	
	private GestureDetector flingDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.serverId = getIntent().getExtras().getInt("serverId");
		
		Server server = (Server) Yaaic.getInstance().getServerById(serverId);
		
		setContentView(R.layout.server);
		
		((TextView) findViewById(R.id.title)).setText(server.getTitle());
		((ImageView) findViewById(R.id.status)).setImageResource(server.getStatusIcon());
		
		/*
		((Button) findViewById(R.id.next)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ViewFlipper vf = (ViewFlipper) v.getRootView().findViewById(R.id.channels);
				vf.setInAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_left_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_left_out));
				vf.showNext();
			}
		});
		
		((Button) findViewById(R.id.previous)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ViewFlipper vf = (ViewFlipper) v.getRootView().findViewById(R.id.channels);
				vf.setInAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_right_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_right_out));
				vf.showPrevious();
			}
		});
		*/
		
		flingDetector = new GestureDetector(new FlingListener((ViewFlipper) findViewById(R.id.channels)));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return flingDetector.onTouchEvent(event);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
        Intent intent = new Intent(this, IRCService.class);
        bindService(intent, this, 0);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		unbindService(this);
	}

	public void onServiceConnected(ComponentName name, IBinder service)
	{
		this.binder = (IRCBinder) service;
	}

	public void onServiceDisconnected(ComponentName name)
	{
		this.binder = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	// inflate from xml
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.server, menu);
    	
    	return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.disconnect:
				binder.getService().getConnection(serverId).quitServer();
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.join:
				final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.channeldialog);
				dialog.setTitle(R.string.channel);

				Button button = (Button) dialog.findViewById(R.id.join);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String channel = ((EditText) v.getRootView().findViewById(R.id.channel)).getText().toString();
						binder.getService().getConnection(serverId).joinChannel(channel);
						dialog.cancel();
					}
				});
				
				dialog.show();
				break;
		}
		
		return true;
	}
}
