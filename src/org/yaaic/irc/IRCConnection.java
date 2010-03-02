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
package org.yaaic.irc;

import android.content.Intent;
import android.util.Log;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import org.yaaic.Yaaic;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Server;
import org.yaaic.model.Status;

public class IRCConnection extends PircBot
{
	private IRCService service;
	private Server server;
	
	// XXX: Print all IRC events to the debug console
	private static final boolean DEBUG_EVENTS = true;
	public static final String TAG = "Yaaic/IRCConnection";
	
	/**
	 * Create a new connection
	 * 
	 * @param service
	 * @param serverId
	 */
	public IRCConnection(IRCService service, int serverId)
	{
		this.server = Yaaic.getInstance().getServerById(serverId);
		this.service = service;
		
		this.setName("Yaaic");
		this.setLogin("Yaaic");
		this.setAutoNickChange(true);
		this.setVersion("Yaaic - Yet another Android IRC client - http://www.yaaic.org");
	}

	/**
	 * On connect
	 */
	@Override
	public void onConnect()
	{
		debug("Connect", "");
		
		server.setStatus(Status.CONNECTED);
		
		service.sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}
	
	/**
	 * On channel action
	 */
	@Override
	protected void onAction(String sender, String login, String hostname, String target, String action)
	{
		debug("Action", target + " " + sender + " " + action);
		
		server.getChannel(target).addMessage("* " + sender + " " + action);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Channel Info
	 */
	@Override
	protected void onChannelInfo(String channel, int userCount, String topic)
	{
		debug("ChannelInfo", channel + " " + userCount);
	}

	/**
	 * On Deop
	 */
	@Override
	protected void onDeop(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("Deop", target + " " + recipient + "(" + sourceNick + ")");
		
		server.getChannel(target).addMessage(sourceNick + " deoped " + recipient);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On DeVoice
	 */
	@Override
	protected void onDeVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("DeVoice", target + " " + recipient + "(" + sourceNick + ")");
		
		server.getChannel(target).addMessage(sourceNick + " devoiced " + recipient);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Invite
	 */
	@Override
	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String target)
	{
		debug("Invite", target + " " + targetNick + "(" + sourceNick + ")");
		
		server.getChannel(target).addMessage(sourceNick + " invited " + targetNick);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Join
	 */
	@Override
	protected void onJoin(String target, String sender, String login, String hostname)
	{
		debug("Join", target + " " + sender);
		
		if (sender.equals(getNick())) {
			// We joined a new channel
			server.addChannel(new Channel(target));
			service.sendBroadcast(new Intent(Broadcast.CHANNEL_NEW));
		} else {
			server.getChannel(target).addMessage(sender + " joined");
			
			Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
			service.sendBroadcast(intent);
		}
	}

	/**
	 * On Kick
	 */
	@Override
	protected void onKick(String target, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
	{
		debug("Kick", target + " " + recipientNick + "(" + kickerNick + ")");
		
		if (recipientNick.equals(getNick())) {
			// We are kicked
			server.removeChannel(target);
			service.sendBroadcast(new Intent(Broadcast.CHANNEL_REMOVE));
		} else {
			server.getChannel(target).addMessage(kickerNick + " kicked " + recipientNick);

			Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
			service.sendBroadcast(intent);			
		}
	}

	/**
	 * On Message
	 */
	@Override
	protected void onMessage(String target, String sender, String login, String hostname, String message)
	{
		debug("Message", target + " " + sender + " " + message);

		server.getChannel(target).addMessage("<" + sender + ">" + message);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Mode
	 */
	@Override
	protected void onMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode)
	{
		debug("Mode", target + " " + sourceNick + " " + mode);
		
		server.getChannel(target).addMessage(sourceNick + " sets mode " + mode);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Nick Change
	 */
	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick)
	{
		debug("Nick", oldNick + " " + newNick);
	}

	/**
	 * On Notice
	 */
	@Override
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice)
	{
		debug("Notice", sourceNick + " " + notice);
	}

	/**
	 * On Op
	 */
	@Override
	protected void onOp(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("Op", target + " " + recipient + "(" + sourceNick + ")");
		
		server.getChannel(target).addMessage(sourceNick + " oped " + recipient);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Part
	 */
	@Override
	protected void onPart(String target, String sender, String login, String hostname)
	{
		debug("Part", target + " " + sender);
		
		if (sender.equals(getNick())) {
			// We pareted a channel
			server.removeChannel(target);
			service.sendBroadcast(new Intent(Broadcast.CHANNEL_REMOVE));
		} else {
			server.getChannel(target).addMessage(sender + " parted");
			
			Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
			service.sendBroadcast(intent);
		}
	}

	/**
	 * On Private Message
	 */
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		debug("PrivateMessage", sender + " " + message);
	}

	/**
	 * On Quit
	 */
	@Override
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
	{
		debug("Quit", sourceNick);
	}

	/**
	 * On Topic
	 */
	@Override
	protected void onTopic(String target, String topic, String setBy, long date, boolean changed)
	{
		debug("Topic", target + " " + setBy + " " + topic);
		
		if (changed) {
			server.getChannel(target).addMessage(setBy + " sets topic: " + topic);
		} else {
			server.getChannel(target).addMessage("Topic: " + topic);
		}
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On User List
	 */
	@Override
	protected void onUserList(String channel, User[] users)
	{
		debug("UserList", channel + " (" + users.length + ")");
	}

	/**
	 * On Voice
	 */
	@Override
	protected void onVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("Voice", target + " " + recipient + "(" + sourceNick + ")");
		
		server.getChannel(target).addMessage(sourceNick + " voiced " + recipient);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
	}
	
	/**
	 * On disconnect
	 */
	@Override
	public void onDisconnect()
	{
		server.setStatus(Status.DISCONNECTED);
		service.sendBroadcast(new Intent(Broadcast.SERVER_UPDATE));
	}

	/**
	 * Print an event to the debug console 
	 */
	private void debug(String event, String params)
	{
		if (DEBUG_EVENTS) {
			Log.d(TAG, "(" + server.getTitle() + ") [" + event + "]: " + params);
		}
	}
	
	/**
	 * Quits from the IRC server with default reason.
	 */
	@Override
	public void quitServer()
	{
		quitServer("Yaaic - Yet another Android IRC client - http://www.yaaic.org");
	}
}
