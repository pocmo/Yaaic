package org.yaaic.client.irc;

import java.util.ArrayList;

import android.text.SpannableString;

/**
 * MessageBuffer
 * 
 * @author Sebastian Kaspari <pocmo@yaaic.org>
 */
public class MessageBuffer
{
	private ArrayList<SpannableString> messageBuffer;
	
	/**
	 * Maximum Buffer Size
	 */
	private static final int MAX_SIZE = 30;
	
	/**
	 * Maximum Buffer Size in Emergencies, e.g. if we are low on memory
	 */
	private static final int EMERGENCY_MAX_SIZE = 10;
	
	/**
	 * 
	 */
	public MessageBuffer()
	{
		messageBuffer = new ArrayList<SpannableString>();
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public boolean add(SpannableString s)
	{
		if (messageBuffer.size() > MAX_SIZE) {
			messageBuffer.remove(0);
		}
		return messageBuffer.add(s);
	}
	
	/**
	 * Truncate the buffer to the maximum size (emergency)
	 * 
	 * Use this if we are low on memory
	 */
	public void truncate()
	{
		while (messageBuffer.size() > EMERGENCY_MAX_SIZE) {
			messageBuffer.remove(0);
		}
	}
}
