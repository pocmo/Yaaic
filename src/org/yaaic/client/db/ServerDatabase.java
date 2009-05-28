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
package org.yaaic.client.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ServerDatabase
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public class ServerDatabase extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "servers.db";
	private static final int DATABASE_VERSION = 1;
	
	public ServerDatabase(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
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
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// XXX: We delete the database currently, in future we want to
		// migrate the database to the new version (add or remove rows..)
		db.execSQL("DROP TABLE IF EXISTS " + ServerConstants.TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + ChannelConstants.TABLE_NAME + ";");
		
		onCreate(db);
	}
	
	public void addServer(String title, String host, int port, String password, boolean autoConnect, boolean useSSL)
	{
		ContentValues values = new ContentValues();
		values.put(ServerConstants.TITLE, title);
		values.put(ServerConstants.HOST, host);
		values.put(ServerConstants.PORT, port);
		values.put(ServerConstants.PASSWORD, password);
		values.put(ServerConstants.AUTOCONNECT, autoConnect);
		values.put(ServerConstants.USE_SSL, useSSL);
		
		this.getWritableDatabase().insert(ServerConstants.TABLE_NAME, null, values);
	}
	
	public void addChannel(int server, String name, String password)
	{
		ContentValues values = new ContentValues();
		values.put(ChannelConstants.NAME, name);
		values.put(ChannelConstants.PASSWORD, password);
		values.put(ChannelConstants.SERVER, server);
		
		this.getWritableDatabase().insert(ServerConstants.TABLE_NAME, null, values);
	}
	
	public Cursor getServers()
	{
		return this.getReadableDatabase().query(
			ServerConstants.TABLE_NAME,
			ServerConstants.ALL,
			null,
			null,
			null,
			null,
			ServerConstants.TITLE + " ASC"
		);
	}
	
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
	
	public Cursor getChannels()
	{
		return this.getReadableDatabase().query(
			ChannelConstants.TABLE_NAME,
			ChannelConstants.ALL,
			null,
			null,
			null,
			null,
			ChannelConstants.NAME + " ASC");
	}
	
	public Cursor getChannelsByServer(int server)
	{
		return this.getReadableDatabase().query(
			ChannelConstants.TABLE_NAME,
			ChannelConstants.ALL,
			ChannelConstants.SERVER + "=" + server,
			null,
			null,
			null,
			ChannelConstants.NAME + " ASC");	
	}
	
	public Cursor getServer(String title)
	{
		return this.getReadableDatabase().query(
				ServerConstants.TABLE_NAME,
				ServerConstants.ALL,
				ServerConstants.TITLE + " =  '" + title + "'",
				null,
				null,
				null,
				ServerConstants.TITLE + " ASC"
			);
	}
	
	public void removeServer(String title)
	{
		this.getWritableDatabase().execSQL("DELETE FROM " + ServerConstants.TABLE_NAME + " WHERE title = '" + title + "';");
	}
}
