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
package org.yaaic.client;

import org.yaaic.client.db.ServerDatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * ServerAdd Activity
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public class ServerAdd extends Activity implements OnClickListener
{
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serveradd);
        
        Button button = (Button) findViewById(R.id.server_add);
        button.setOnClickListener(this);
    }
	
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.server_add:
				
				String title = ((EditText) findViewById(R.id.server_title)).getText().toString();
				String host = ((EditText) findViewById(R.id.server_host)).getText().toString();
				int port = Integer.parseInt(((EditText) findViewById(R.id.server_port)).getText().toString());
				String password = ((EditText) findViewById(R.id.server_password)).getText().toString();
				boolean autoConnect = ((CheckBox) findViewById(R.id.server_autoconnect)).isChecked();
				boolean useSSL = ((CheckBox) findViewById(R.id.server_usessl)).isChecked();

				ServerDatabase db = new ServerDatabase(this);
				db.addServer(title, host, port, password, autoConnect, useSSL);
				db.close();
				
				this.finish();
				break;
		}
	}
}
