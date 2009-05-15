package org.yaaic.client.db;

import android.provider.BaseColumns;

public interface ServerConstants extends BaseColumns
{
	public static final String TABLE_NAME   = "servers";
	
	public static final String TITLE    	= "title";
	public static final String HOST     	= "host";
	public static final String PORT     	= "port";
	public static final String PASSWORD 	= "password";
	public static final String AUTOCONNECT  = "autoConnect";
	public static final String USE_SSL		= "useSSL";
	public static final String IDENTITY     = "identity";
	
	public static final String[] ALL = {
		_ID,
		TITLE,
		HOST,
		PORT,
		PASSWORD,
		AUTOCONNECT,
		USE_SSL
	};
}
