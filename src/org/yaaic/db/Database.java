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
package org.yaaic.db;

import java.util.HashMap;

import org.yaaic.model.Identity;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper for the servers and channels tables  
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Database extends SQLiteOpenHelper
{
	public static final String TAG = "Yaaic/Database";
	
	private static final String DATABASE_NAME = "servers.db";
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * Create a new helper for database access
	 * 
	 * @param context
	 */
	public Database(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * Create all needed tables on first start
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " + ServerConstants.TABLE_NAME + " ( "
				+ ServerConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ServerConstants.TITLE + " TEXT NOT NULL, "
				+ ServerConstants.HOST + " TEXT NOT NULL, "
				+ ServerConstants.PORT + " INTEGER, "
				+ ServerConstants.PASSWORD + " TEXT, "
				+ ServerConstants.AUTOCONNECT + " BOOLEAN, " // XXX: Does SQLLite support boolean?
				+ ServerConstants.USE_SSL + " BOOLEAN, "
				+ ServerConstants.IDENTITY + " INTEGER"
				+ ");"
		);
		
		db.execSQL("CREATE TABLE " + ChannelConstants.TABLE_NAME + " ("
				+ ChannelConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ChannelConstants.NAME + " TEXT NOT NULL, "
				+ ChannelConstants.PASSWORD + " TEXT, "
				+ ChannelConstants.SERVER + " INTEGER"
				+ ");"
		);
		
		db.execSQL("CREATE TABLE " + IdentityConstants.TABLE_NAME +" ("
				+ IdentityConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ IdentityConstants.NICKNAME + " TEXT NOT NULL,"
				+ IdentityConstants.IDENT + " TEXT NOT NULL,"
				+ IdentityConstants.REALNAME + " TEXT NOT NULL"
				+ ");"
		);
	}
	
	/**
	 * Migrate existing databases to
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// XXX: We delete the database currently, in future versions we want to
		// migrate the database to the new version (add or remove rows..)
		db.execSQL("DROP TABLE IF EXISTS " + ServerConstants.TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + ChannelConstants.TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + IdentityConstants.TABLE_NAME + ";");
		
		onCreate(db);
	}
	
	/**
	 * Add a new server to the database 
	 * 
	 * @param title Unique title of the server
	 * @param host Hostname of the server
	 * @param port Port (default: 3337)
	 * @param password Password if needed
	 * @param autoConnect Autoconnect to this server on startup?
	 * @param useSSL Does the server use SSL?
	 */
	public long addServer(String title, String host, int port, String password, boolean autoConnect, boolean useSSL, long identityId)
	{
		ContentValues values = new ContentValues();
		
		values.put(ServerConstants.TITLE, title);
		values.put(ServerConstants.HOST, host);
		values.put(ServerConstants.PORT, port);
		values.put(ServerConstants.PASSWORD, password);
		values.put(ServerConstants.AUTOCONNECT, autoConnect);
		values.put(ServerConstants.USE_SSL, useSSL);
		values.put(ServerConstants.IDENTITY, identityId);
		
		return this.getWritableDatabase().insert(ServerConstants.TABLE_NAME, null, values);
	}
	
	/**
	 * Add a channel to the database
	 * 
	 * @param server Unique id of server
	 * @param name Name of channel
	 * @param password Password to join (if needed)
	 */
	public void addChannel(int server, String name, String password)
	{
		ContentValues values = new ContentValues();
		
		values.put(ChannelConstants.NAME, name);
		values.put(ChannelConstants.PASSWORD, password);
		values.put(ChannelConstants.SERVER, server);
		
		this.getWritableDatabase().insert(ServerConstants.TABLE_NAME, null, values);
	}
	
	/**
	 * Get all servers from database
	 * 
	 * @return
	 */
	public HashMap<Integer, Server> getServers()
	{
		HashMap<Integer, Server> servers = new HashMap<Integer, Server>();
		
		Cursor cursor = this.getReadableDatabase().query(
			ServerConstants.TABLE_NAME,
			ServerConstants.ALL,
			null,
			null,
			null,
			null,
			ServerConstants.TITLE + " ASC"
		);
		
		while (cursor.moveToNext()) {
			Server server = new Server();
			
			server.setTitle(cursor.getString(cursor.getColumnIndex((ServerConstants.TITLE))));
			server.setHost(cursor.getString(cursor.getColumnIndex((ServerConstants.HOST))));
			server.setPort(cursor.getInt(cursor.getColumnIndex((ServerConstants.PORT))));
			server.setId(cursor.getInt(cursor.getColumnIndex((ServerConstants._ID))));
			server.setStatus(Status.DISCONNECTED);
			
			// Load identity for server
			Identity identity = this.getIdentityById(cursor.getInt(cursor.getColumnIndex(ServerConstants.IDENTITY)));
			server.setIdentity(identity);
			
			servers.put(server.getId(), server);
		}
		cursor.close();
		
		return servers;
	}
	
	/**
	 * Get all servers with autoconnect enabled
	 * 
	 * @return
	 */
	public Cursor getAutoConnectServers()
	{
		return this.getReadableDatabase().query(
			ServerConstants.TABLE_NAME,
			ServerConstants.ALL,
			ServerConstants.AUTOCONNECT + " = 1",
			null,
			null,
			null,
			ServerConstants.TITLE + " ASC"
		);
	}
	
	/**
	 * Get all channels
	 * 
	 * @return
	 */
	public Cursor getChannels()
	{
		return this.getReadableDatabase().query(
			ChannelConstants.TABLE_NAME,
			ChannelConstants.ALL,
			null,
			null,
			null,
			null,
			ChannelConstants.NAME + " ASC"
		);
	}
	
	/**
	 * Get all channels of server
	 * 
	 * @param server Unique id of server
	 * @return
	 */
	public Cursor getChannelsById(int serverId)
	{
		return this.getReadableDatabase().query(
			ChannelConstants.TABLE_NAME,
			ChannelConstants.ALL,
			ChannelConstants.SERVER + "=" + serverId,
			null,
			null,
			null,
			ChannelConstants.NAME + " ASC"
		);	
	}
	
	/**
	 * Remove server from database by unique id
	 * 
	 * @param title
	 */
	public void removeServerById(int serverId)
	{
		this.getWritableDatabase().execSQL(
			"DELETE FROM " + ServerConstants.TABLE_NAME + " WHERE " + ServerConstants._ID + " = " + serverId + ";"
		);
	}
	
	/**
	 * Add a new identity
	 * 
	 * @param identityId
	 * @param nickname
	 * @param ident
	 * @param realname
	 */
	public long addIdentity(String nickname, String ident, String realname)
	{
		ContentValues values = new ContentValues();
		
		values.put(IdentityConstants.NICKNAME, nickname);
		values.put(IdentityConstants.IDENT, ident);
		values.put(IdentityConstants.REALNAME, realname);
		
		return this.getWritableDatabase().insert(IdentityConstants.TABLE_NAME, null, values);
	}
	
	/**
	 * Update the identity with the given id
	 * 
	 * @param identityId
	 * @param nickname
	 * @param ident
	 * @param realname
	 */
	public void updateIdentity(int identityId, String nickname, String ident, String realname)
	{
		ContentValues values = new ContentValues();
		
		values.put(IdentityConstants.NICKNAME, nickname);
		values.put(IdentityConstants.IDENT, ident);
		values.put(IdentityConstants.REALNAME, realname);
		
		this.getWritableDatabase().update(
			IdentityConstants.TABLE_NAME,
			values,
			IdentityConstants._ID + " = " + identityId,
			null
		);
	}
	
	/**
	 * Get an identity by its id
	 * 
	 * @param identityId
	 * @return
	 */
	public Identity getIdentityById(int identityId)
	{
		Cursor cursor = this.getReadableDatabase().query(
			IdentityConstants.TABLE_NAME,
			IdentityConstants.ALL,
			IdentityConstants._ID + "=" + identityId,
			null,
			null,
			null,
			null
		);
		
		if (cursor.moveToNext()) {
			Identity identity = new Identity();
			
			identity.setNickname(cursor.getString(cursor.getColumnIndex(IdentityConstants.NICKNAME)));
			identity.setIdent(cursor.getString(cursor.getColumnIndex(IdentityConstants.IDENT)));
			identity.setRealName(cursor.getString(cursor.getColumnIndex(IdentityConstants.REALNAME)));
			
			cursor.close();
			
			return identity;
		}
		
		return null;
	}
}
