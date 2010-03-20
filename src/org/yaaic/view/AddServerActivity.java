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
package org.yaaic.view;

import java.util.regex.Pattern;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.db.Database;
import org.yaaic.exception.ValidationException;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Identity;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

/**
 * Add a new server to the list
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class AddServerActivity extends Activity implements OnClickListener
{
	public static final String TAG = "Yaaic/AddServerActivity";
	private Server server;
	
	/**
	 * On create
	 */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.serveradd);
        
        ((Button) findViewById(R.id.add)).setOnClickListener(this);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Broadcast.EXTRA_SERVER)) {
        	// Request to edit an existing server
        	Database db = new Database(this);
        	this.server = db.getServerById(extras.getInt(Broadcast.EXTRA_SERVER));
        	db.close();
        	
        	// Set server values
        	((EditText) findViewById(R.id.title)).setText(server.getTitle());
        	((EditText) findViewById(R.id.host)).setText(server.getHost());
        	((EditText) findViewById(R.id.port)).setText(String.valueOf(server.getPort()));
        	((EditText) findViewById(R.id.password)).setText(server.getPassword());
        	
        	((EditText) findViewById(R.id.nickname)).setText(server.getIdentity().getNickname());
        	((EditText) findViewById(R.id.ident)).setText(server.getIdentity().getIdent());
        	((EditText) findViewById(R.id.realname)).setText(server.getIdentity().getRealName());
        	
        	((Button) findViewById(R.id.add)).setText("Save");
        }
        
        Uri uri = getIntent().getData();
        if (uri != null && uri.getScheme().equals("irc")) {
        	// handling an irc:// uri
        	
        	((EditText) findViewById(R.id.host)).setText(uri.getHost());
        	if (uri.getPort() != -1) {
        		((EditText) findViewById(R.id.port)).setText(String.valueOf(uri.getPort()));
        	}
        }
    }

	/**
	 * On click add server or cancel activity
	 */
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.add:
				try {
					validateServer();
					validateIdentity();
					if (server == null) {
						addServer();
					} else {
						updateServer();
					}
					setResult(RESULT_OK);
					finish();
				} catch(ValidationException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			break;
			case R.id.cancel:
				setResult(RESULT_CANCELED);
				finish();
			break;
		}
	}
	
	/**
	 * Add server to database
	 */
	private void addServer()
	{
		Database db = new Database(this);
		
		Identity identity = getIdentityFromView();
		long identityId = db.addIdentity(
			identity.getNickname(),
			identity.getIdent(),
			identity.getRealName()
		);
		
		Server server = getServerFromView();
		long serverId = db.addServer(
			server.getTitle(),
			server.getHost(),
			server.getPort(),
			server.getPassword(),
			false, // auto connect
			false, // use ssl
			identityId
		);
		
		db.close();
		
		server.setId((int) serverId);
		server.setIdentity(identity);
		
		Yaaic.getInstance().addServer(server);
	}
	
	/**
	 * Update server
	 */
	private void updateServer()
	{
		Database db = new Database(this);
		
		int serverId = this.server.getId();
		int identityId = db.getIdentityIdByServerId(serverId);
		
		Server server = getServerFromView();
		db.updateServer(
			serverId,
			server.getTitle(),
			server.getHost(),
			server.getPort(),
			server.getPassword(),
			false, // auto connect
			false, // use ssl
			identityId
		);
		
		Identity identity = getIdentityFromView();
		db.updateIdentity(
			identityId,
			identity.getNickname(),
			identity.getIdent(),
			identity.getNickname()
		);
		
		db.close();
		
		server.setId(this.server.getId());
		server.setIdentity(identity);
		
		Yaaic.getInstance().updateServer(server);
	}
	
	/**
	 * Populate a server object from the data in the view
	 * 
	 * @return The server object
	 */
	private Server getServerFromView()
	{
		String title = ((EditText) findViewById(R.id.title)).getText().toString();
		String host = ((EditText) findViewById(R.id.host)).getText().toString();
		int port = Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString());
		String password = ((EditText) findViewById(R.id.password)).getText().toString();
		
		// not in use yet
		//boolean autoConnect = ((CheckBox) findViewById(R.id.autoconnect)).isChecked();
		//boolean useSSL = ((CheckBox) findViewById(R.id.useSSL)).isChecked();
		
		Server server = new Server();
		server.setHost(host);
		server.setPort(port);
		server.setPassword(password);
		server.setTitle(title);
		server.setStatus(Status.DISCONNECTED);

		return server;
	}
	
	/**
	 * Populate an identity object from the data in the view
	 * 
	 * @return The identity object
	 */
	private Identity getIdentityFromView()
	{
		String nickname = ((EditText) findViewById(R.id.nickname)).getText().toString();
		String ident = ((EditText) findViewById(R.id.ident)).getText().toString();
		String realname = ((EditText) findViewById(R.id.realname)).getText().toString();
		
		Identity identity = new Identity();
		identity.setNickname(nickname);
		identity.setIdent(ident);
		identity.setRealName(realname);
		
		return identity;
	}
	
	/**
	 * Validate the input for a server
	 * 
	 * @throws ValidationException
	 */
	private void validateServer() throws ValidationException
	{
		String title = ((EditText) findViewById(R.id.title)).getText().toString();
		String host = ((EditText) findViewById(R.id.host)).getText().toString();
		String port = ((EditText) findViewById(R.id.port)).getText().toString();
		
		if (title.trim().equals("")) {
			throw new ValidationException("Title cannot be blank");
		}
		
		if (host.trim().equals("")) {
			// XXX: We should use some better host validation
			throw new ValidationException("Host cannot be blank");
		}
		
		try {
			Integer.parseInt(port);
		} catch (NumberFormatException e) {
			throw new ValidationException("Enter a numeric port");
		}
		
		Database db = new Database(this);
		if (db.isTitleUsed(title) && (server == null || !server.getTitle().equals(title))) {
			db.close();
			throw new ValidationException("There is already a server with this title");
		}
		db.close();
	}
	
	/**
	 * Validate the input for a identity
	 * 
	 * @throws ValidationException
	 */
	private void validateIdentity() throws ValidationException
	{
		String nickname = ((EditText) findViewById(R.id.nickname)).getText().toString();
		String ident = ((EditText) findViewById(R.id.ident)).getText().toString();
		String realname = ((EditText) findViewById(R.id.realname)).getText().toString();
		
		if (nickname.trim().equals("")) {
			throw new ValidationException("Nickname cannot be blank");
		}
		
		if (ident.trim().equals("")) {
			throw new ValidationException("Ident cannot be blank");
		}
		
		if (realname.trim().equals("")) {
			throw new ValidationException("Realname cannot be blank");
		}
		
		// RFC 1459:  <nick> ::= <letter> { <letter> | <number> | <special> }
		// <special>    ::= '-' | '[' | ']' | '\' | '`' | '^' | '{' | '}'
		// Chars that are not in RFC 1459 but are supported too:
		// | and _ 
		Pattern nickPattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9^-`\\[\\]{}|_\\\\]*$");
		if (!nickPattern.matcher(nickname).matches()) {
			throw new ValidationException("Invalid nickname");
		}
		
		// We currently only allow chars as ident
		Pattern identPattern = Pattern.compile("^[a-zA-Z]+$");
		if (!identPattern.matcher(ident).matches()) {
			throw new ValidationException("Invalid ident");
		}
	}
}
