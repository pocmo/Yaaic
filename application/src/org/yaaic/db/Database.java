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
package org.yaaic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yaaic.model.Authentication;
import org.yaaic.model.Identity;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper for the servers and channels tables
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Database extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "servers.db";
    private static final int DATABASE_VERSION = 5;

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
            + ServerConstants.AUTOCONNECT + " BOOLEAN, "
            + ServerConstants.USE_SSL + " BOOLEAN, "
            + ServerConstants.CHARSET + " TEXT, "
            + ServerConstants.IDENTITY + " INTEGER, "
            + ServerConstants.NICKSERV_PASSWORD + " TEXT, "
            + ServerConstants.SASL_USERNAME + " TEXT, "
            + ServerConstants.SASL_PASSWORD + " TEXT"
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

        db.execSQL("CREATE TABLE " + CommandConstants.TABLE_NAME + " ("
            + CommandConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CommandConstants.COMMAND + " TEXT NOT NULL, "
            + ChannelConstants.SERVER + " INTEGER"
            + ");"
        );

        db.execSQL("CREATE TABLE " + AliasConstants.TABLE_NAME + " ("
            + AliasConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AliasConstants.ALIAS + " TEXT NOT NULL, "
            + AliasConstants.IDENTITY + " INTEGER"
            + ");"
        );
    }

    /**
     * Migrate existing databases to
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // XXX: Do not delete databases (release version)

        // db.execSQL("DROP TABLE IF EXISTS " + ServerConstants.TABLE_NAME + ";");
        // db.execSQL("DROP TABLE IF EXISTS " + ChannelConstants.TABLE_NAME + ";");
        // db.execSQL("DROP TABLE IF EXISTS " + IdentityConstants.TABLE_NAME + ";");

        // onCreate(db);

        if (oldVersion == 1) {
            // Add charset field to server table
            db.execSQL("ALTER TABLE " + ServerConstants.TABLE_NAME + " ADD " + ServerConstants.CHARSET + " TEXT AFTER " + ServerConstants.USE_SSL + ";");

            oldVersion = 2; // now do the updates for version 2
        }

        if (oldVersion == 2) {
            // Add new commands table (copy&paste from onCreate())
            db.execSQL("CREATE TABLE " + CommandConstants.TABLE_NAME + " ("
                + CommandConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CommandConstants.COMMAND + " TEXT NOT NULL, "
                + ChannelConstants.SERVER + " INTEGER"
                + ");"
            );

            oldVersion = 3;
        }

        if (oldVersion == 3) {
            db.execSQL("CREATE TABLE " + AliasConstants.TABLE_NAME + " ("
                + AliasConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AliasConstants.ALIAS + " TEXT NOT NULL, "
                + AliasConstants.IDENTITY + " INTEGER"
                + ");"
            );

            oldVersion = 4;
        }

        if (oldVersion == 4) {
            // Add authentication fields to database
            db.execSQL("ALTER TABLE " + ServerConstants.TABLE_NAME + " ADD " + ServerConstants.NICKSERV_PASSWORD + " TEXT AFTER " + ServerConstants.CHARSET + ";");
            db.execSQL("ALTER TABLE " + ServerConstants.TABLE_NAME + " ADD " + ServerConstants.SASL_USERNAME + " TEXT AFTER " + ServerConstants.NICKSERV_PASSWORD + ";");
            db.execSQL("ALTER TABLE " + ServerConstants.TABLE_NAME + " ADD " + ServerConstants.SASL_PASSWORD + " TEXT AFTER " + ServerConstants.SASL_USERNAME + ";");
        }
    }

    /**
     * Add a new server to the database
     * 
     * @param server     The server to add.
     * @param identityId The id of the assigned identity
     */
    public long addServer(Server server, int identityId)
    {
        ContentValues values = new ContentValues();

        values.put(ServerConstants.TITLE, server.getTitle());
        values.put(ServerConstants.HOST, server.getHost());
        values.put(ServerConstants.PORT, server.getPort());
        values.put(ServerConstants.PASSWORD, server.getPassword());
        values.put(ServerConstants.AUTOCONNECT, false);
        values.put(ServerConstants.USE_SSL, server.useSSL());
        values.put(ServerConstants.IDENTITY, identityId);
        values.put(ServerConstants.CHARSET, server.getCharset());

        Authentication authentication = server.getAuthentication();
        values.put(ServerConstants.NICKSERV_PASSWORD, authentication.getNickservPassword());
        values.put(ServerConstants.SASL_USERNAME, authentication.getSaslUsername());
        values.put(ServerConstants.SASL_PASSWORD, authentication.getSaslPassword());

        return this.getWritableDatabase().insert(ServerConstants.TABLE_NAME, null, values);
    }

    /**
     * Update the server record in the database
     * 
     * @param serverId   The primary key of the server to update.
     * @param server     The server to update.
     * @param identityId The identity of the server record
     */
    public void updateServer(int serverId, Server server, int identityId)
    {
        ContentValues values = new ContentValues();

        values.put(ServerConstants.TITLE, server.getTitle());
        values.put(ServerConstants.HOST, server.getHost());
        values.put(ServerConstants.PORT, server.getPort());
        values.put(ServerConstants.PASSWORD, server.getPassword());
        values.put(ServerConstants.AUTOCONNECT, false);
        values.put(ServerConstants.USE_SSL, server.useSSL());
        values.put(ServerConstants.IDENTITY, identityId);
        values.put(ServerConstants.CHARSET, server.getCharset());

        Authentication authentication = server.getAuthentication();

        values.put(ServerConstants.NICKSERV_PASSWORD, authentication.getNickservPassword());
        values.put(ServerConstants.SASL_USERNAME, authentication.getSaslUsername());
        values.put(ServerConstants.SASL_PASSWORD, authentication.getSaslPassword());

        this.getWritableDatabase().update(
            ServerConstants.TABLE_NAME,
            values,
            ServerConstants._ID + " = " + serverId,
            null
        );
    }

    /**
     * Add a channel to the database
     * 
     * @param serverId Unique id of server
     * @param name Name of channel
     * @param password Password to join (if needed)
     */
    public void addChannel(int serverId, String name, String password)
    {
        ContentValues values = new ContentValues();

        values.put(ChannelConstants.NAME, name);
        values.put(ChannelConstants.PASSWORD, password);
        values.put(ChannelConstants.SERVER, serverId);

        this.getWritableDatabase().insert(ChannelConstants.TABLE_NAME, null, values);
    }

    /**
     * Replace list of channels for the given server
     * 
     * @param serverId Unique id of server
     * @param channels List of channel names
     */
    public void setChannels(int serverId, ArrayList<String> channels)
    {
        // Remove old channels
        this.getWritableDatabase().delete(
            ChannelConstants.TABLE_NAME,
            ChannelConstants.SERVER + " = " + serverId,
            null
        );

        // Add new channels
        for (String channel : channels) {
            addChannel(serverId, channel, "");
        }
    }

    /**
     * Get all commands to execute on connect
     * 
     * @param serverId Unique id of server
     * @return List of commands
     */
    public ArrayList<String> getCommandsByServerId(int serverId)
    {
        ArrayList<String> commands = new ArrayList<String>();

        Cursor cursor = this.getReadableDatabase().query(
            CommandConstants.TABLE_NAME,
            CommandConstants.ALL,
            CommandConstants.SERVER + " = " + serverId,
            null,
            null,
            null,
            null
        );

        while (cursor.moveToNext()) {
            String command = cursor.getString(cursor.getColumnIndex(CommandConstants.COMMAND));
            commands.add(command);
        }

        cursor.close();

        return commands;
    }

    /**
     * Add a command to a server
     * 
     * @param serverId Unique id of server
     * @param command The command to execute after connect
     */
    public void addCommand(int serverId, String command)
    {
        ContentValues values = new ContentValues();

        values.put(CommandConstants.COMMAND, command);
        values.put(CommandConstants.SERVER, serverId);

        this.getWritableDatabase().insert(CommandConstants.TABLE_NAME, null, values);
    }

    /**
     * Replace list of commands for the given server
     * 
     * @param serverId Unique id of server
     * @param commands List of commands to execute after connect
     */
    public void setCommands(int serverId, ArrayList<String> commands)
    {
        // Remove old commands
        this.getWritableDatabase().delete(
            CommandConstants.TABLE_NAME,
            CommandConstants.SERVER + " = " + serverId,
            null
        );

        // Add new commands
        for (String command : commands) {
            addCommand(serverId, command);
        }
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
            Server server = populateServer(cursor);
            servers.put(server.getId(), server);
        }
        cursor.close();

        return servers;
    }

    public Server getServerById(int serverId)
    {
        Server server = null;

        Cursor cursor = this.getReadableDatabase().query(
            ServerConstants.TABLE_NAME,
            ServerConstants.ALL,
            ServerConstants._ID + " = " + serverId,
            null,
            null,
            null,
            ServerConstants.TITLE + " ASC"
        );

        if (cursor.moveToNext()) {
            server = populateServer(cursor);
        }

        cursor.close();

        return server;
    }

    /**
     * Check if the given server title is currently used
     * 
     * @param title The server title
     * @return true if there's a server with this title, false otherwise
     */
    public boolean isTitleUsed(String title)
    {
        boolean isTitleUsed = false;

        Cursor cursor = this.getReadableDatabase().query(
            ServerConstants.TABLE_NAME,
            ServerConstants.ALL,
            ServerConstants.TITLE + " = " + DatabaseUtils.sqlEscapeString(title),
            null,
            null,
            null,
            null
        );

        if (cursor.moveToNext()) {
            isTitleUsed = true;
        }

        cursor.close();

        return isTitleUsed;
    }

    /**
     * Populate a server object from the given database cursor
     * @param cursor
     * @return
     */
    private Server populateServer(Cursor cursor)
    {
        Server server = new Server();

        server.setTitle(cursor.getString(cursor.getColumnIndex((ServerConstants.TITLE))));
        server.setHost(cursor.getString(cursor.getColumnIndex((ServerConstants.HOST))));
        server.setPort(cursor.getInt(cursor.getColumnIndex((ServerConstants.PORT))));
        server.setPassword(cursor.getString(cursor.getColumnIndex(ServerConstants.PASSWORD)));
        server.setId(cursor.getInt(cursor.getColumnIndex((ServerConstants._ID))));
        server.setCharset(cursor.getString(cursor.getColumnIndex(ServerConstants.CHARSET)));

        String useSSLvalue = cursor.getString(cursor.getColumnIndex(ServerConstants.USE_SSL));
        if (useSSLvalue != null && useSSLvalue.equals("1")) {
            server.setUseSSL(true);
        }

        server.setStatus(Status.DISCONNECTED);

        Authentication authentication = new Authentication();
        authentication.setNickservPassword(cursor.getString(cursor.getColumnIndex(ServerConstants.NICKSERV_PASSWORD)));
        authentication.setSaslUsername(cursor.getString(cursor.getColumnIndex(ServerConstants.SASL_USERNAME)));
        authentication.setSaslPassword(cursor.getString(cursor.getColumnIndex(ServerConstants.SASL_PASSWORD)));
        server.setAuthentication(authentication);

        // Load identity for server
        Identity identity = this.getIdentityById(cursor.getInt(cursor.getColumnIndex(ServerConstants.IDENTITY)));
        server.setIdentity(identity);

        // Load auto join channels
        ArrayList<String> channels = this.getChannelsByServerId(server.getId());
        server.setAutoJoinChannels(channels);

        // Load commands to execute after connect
        ArrayList<String> commands = this.getCommandsByServerId(server.getId());
        server.setConnectCommands(commands);

        return server;
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
     * Get all channels of server
     * 
     * @param server Unique id of server
     * @return list of channel names
     */
    public ArrayList<String> getChannelsByServerId(int serverId)
    {
        ArrayList<String> channels = new ArrayList<String>();

        Cursor cursor = this.getReadableDatabase().query(
            ChannelConstants.TABLE_NAME,
            ChannelConstants.ALL,
            ChannelConstants.SERVER + " = " + serverId,
            null,
            null,
            null,
            ChannelConstants.NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            String channel = cursor.getString(cursor.getColumnIndex(ChannelConstants.NAME));
            channels.add(channel);
        }

        cursor.close();

        return channels;
    }

    /**
     * Remove server from database by unique id
     * 
     * @param title
     */
    public void removeServerById(int serverId)
    {
        // XXX: Workaround: Remove identity assigned to this server
        //      until we have some kind of identity manager
        int identityId = this.getIdentityIdByServerId(serverId);
        if (identityId != -1) {
            deleteAliases(identityId);
            this.getWritableDatabase().execSQL(
                "DELETE FROM " + IdentityConstants.TABLE_NAME + " WHERE " + IdentityConstants._ID + " = " + identityId + ";"
            );
        }

        // Now delete the server entry
        this.getWritableDatabase().execSQL(
            "DELETE FROM " + ServerConstants.TABLE_NAME + " WHERE " + ServerConstants._ID + " = " + serverId + ";"
        );
    }

    protected void setAliases(long identityId, List<String> aliases)
    {
        deleteAliases(identityId);

        ContentValues values = new ContentValues();

        for (String alias : aliases) {
            values.clear();
            values.put(AliasConstants.ALIAS, alias);
            values.put(AliasConstants.IDENTITY, identityId);
            getWritableDatabase().insert(AliasConstants.TABLE_NAME, null, values);
        }
    }

    protected void deleteAliases(long identityId)
    {
        getWritableDatabase().execSQL(
            "DELETE FROM " + AliasConstants.TABLE_NAME + " WHERE " + AliasConstants.IDENTITY + " = " + identityId
        );
    }

    protected List<String> getAliasesByIdentityId(long identityId)
    {
        List<String> aliases = new ArrayList<String>();

        Cursor cursor = this.getReadableDatabase().query(
            AliasConstants.TABLE_NAME,
            AliasConstants.ALL,
            AliasConstants.IDENTITY + " = " + identityId,
            null,
            null,
            null,
            null
        );

        while (cursor.moveToNext()) {
            aliases.add(cursor.getString(cursor.getColumnIndex(AliasConstants.ALIAS)));
        }
        cursor.close();

        return aliases;
    }

    /**
     * Add a new identity
     * 
     * @param identityId
     * @param nickname
     * @param ident
     * @param realname
     * @param aliases
     */
    public long addIdentity(String nickname, String ident, String realname, List<String> aliases)
    {
        ContentValues values = new ContentValues();

        values.put(IdentityConstants.NICKNAME, nickname);
        values.put(IdentityConstants.IDENT, ident);
        values.put(IdentityConstants.REALNAME, realname);

        long identityId = this.getWritableDatabase().insert(IdentityConstants.TABLE_NAME, null, values);

        setAliases(identityId, aliases);

        return identityId;
    }

    /**
     * Update the identity with the given id
     * 
     * @param identityId
     * @param nickname
     * @param ident
     * @param realname
     */
    public void updateIdentity(int identityId, String nickname, String ident, String realname, List<String> aliases)
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

        setAliases(identityId, aliases);
    }

    /**
     * Get an identity by its id
     * 
     * @param identityId
     * @return
     */
    public Identity getIdentityById(int identityId)
    {
        Identity identity = null;

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
            identity = new Identity();

            identity.setNickname(cursor.getString(cursor.getColumnIndex(IdentityConstants.NICKNAME)));
            identity.setIdent(cursor.getString(cursor.getColumnIndex(IdentityConstants.IDENT)));
            identity.setRealName(cursor.getString(cursor.getColumnIndex(IdentityConstants.REALNAME)));

            identity.setAliases(getAliasesByIdentityId(identityId));
        }

        cursor.close();

        return identity;
    }

    /**
     * Get a server by its id
     * 
     * @param serverId
     * @return
     */
    public int getIdentityIdByServerId(int serverId)
    {
        int identityId = -1;

        Cursor cursor = this.getReadableDatabase().query(
            ServerConstants.TABLE_NAME,
            ServerConstants.ALL,
            ServerConstants._ID + " = " + serverId,
            null,
            null,
            null,
            null
        );

        if (cursor.moveToNext()) {
            identityId = cursor.getInt(cursor.getColumnIndex(ServerConstants.IDENTITY));
        }

        cursor.close();

        return identityId;
    }
}
