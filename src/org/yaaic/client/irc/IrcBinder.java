package org.yaaic.client.irc;

import android.os.Binder;
import android.util.Log;

/**
 * IrcBinder
 * 
 * @author Sebastian Kaspari <pocmo@yaaic.org>
 */
public class IrcBinder extends Binder
{
	private IrcService service;
	private String TAG = "YaaicBinder";
	
	/**
	 * 
	 * @param service
	 */
	public IrcBinder(IrcService service)
	{
		Log.d(TAG, "Binder created");
		
		this.service = service;
	}
	
	/**
	 * 
	 * @param title
	 * @param host
	 * @param port
	 * @param password
	 */
	public void connect(final String title, final String host, final int port, final String password)
	{
		Thread thread = new Thread() {
			public void run() {
				service.connect(title, host, port, password);
			}
		};
		thread.start();
	}
	
	/**
	 * 
	 * @param title
	 * @return
	 */
	public boolean isConnected(String title) {
		IrcServer server = this.service.getIrcServer(title);
		if (server != null) {
			return server.isConnected();
		}
		return false;
	}
	
	/**
	 * 
	 * @param title
	 * @return
	 */
	public boolean disconnect(String title)
	{
		IrcServer server = this.service.getIrcServer(title);
		if (server != null) {
			if (server.isConnected()) {
				server.quitServer("Yaaic - Yet Another Android IRC Client - www.yaaic.org");
				return true;
			}
		}
		return false;
	}
}
