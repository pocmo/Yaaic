package org.yaaic.client.irc;

import java.io.IOException;
import java.util.HashMap;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.yaaic.client.db.ServerDatabase;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Sebastian Kaspari
 */
public class IrcService extends Service
{
	private static final String TAG = "Yaaic/Service";
	
	private int createCounter = 0;
	private IrcBinder binder;
	private Handler handler;
	
	private HashMap<String, IrcServer> servers = new HashMap<String, IrcServer>();
	
	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate(" + createCounter + ")");
		
		super.onCreate();
		createCounter++;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d(TAG, "onBind");
		
		if (binder == null) {
			binder = new IrcBinder(this);
		}
		return binder;
	}

	
	public int getCounter()
	{
		return createCounter;
	}
	
	public Handler getHandler()
	{
		return handler;
	}
	
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy(" + createCounter + ")");
		
		super.onDestroy();
		createCounter--;
	}
	
	/**
	 * Connect to server
	 * 
	 * @param title Title of the IRC-Network
	 * @param host
	 * @param port
	 * @param password (If needed, else an empty string)
	 */
	public void connect(String title, String host, int port, String password)
	{
		if(!servers.containsKey(title)) {
			Log.d(TAG, "Connect to " + title + ": " + host + ":" + port + "[" + password + "]");
			
			IrcServer irc = new IrcServer();
			
			try {
				irc.connect(host, port);
				servers.put(title, irc);
			}
			catch(NickAlreadyInUseException e) {
				Log.d(TAG, e.getMessage());
			}
			catch(IrcException e) {
				Log.d(TAG, e.getMessage());
			}
			catch(IOException e) {
				Log.d(TAG, e.getMessage());
			}
		} else {
			Log.d(TAG, "Already connected to: " + title);
		}
	}

	/**
	 * Get IrcServer Object by Title
	 * 
	 * @param title Title of the IRC-Network (PK)
	 * @return IrcServer Object or null
	 */
	public IrcServer getIrcServer(String title)
	{
		return servers.get(title);
	}
}
