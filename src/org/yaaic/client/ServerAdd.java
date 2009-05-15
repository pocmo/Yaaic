package org.yaaic.client;

import org.yaaic.client.db.ServerDatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

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
