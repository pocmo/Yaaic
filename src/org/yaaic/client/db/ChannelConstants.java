package org.yaaic.client.db;

import android.provider.BaseColumns;

/**
 * ChannelConstants
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public interface ChannelConstants extends BaseColumns
{
	public static final String TABLE_NAME = "channels";
	
	public static final String NAME		  = "name";
	public static final String PASSWORD   = "password";
	public static final String SERVER     = "server";
	
	public static final String[] ALL = {
		NAME,
		PASSWORD,
		SERVER
	};
}
