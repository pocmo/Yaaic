package org.yaaic.client.irc;

import org.jibble.pircbot.PircBot;

public class IrcServer extends PircBot
{
	public IrcServer()
	{
		this.setName("Yaaic");
		this.setAutoNickChange(true);
		this.setLogin("yaaic");
	}
}
