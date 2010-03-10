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

import java.util.Vector;

import android.content.Intent;
import android.util.Log;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Query;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
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
		
		Message message = new Message("Connected to " + server.getTitle());
		message.setColor(Message.COLOR_GREEN);
		server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, ServerInfo.DEFAULT_NAME);
		service.sendBroadcast(intent);
	}
	
	/**
	 * On channel action
	 */
	@Override
	protected void onAction(String sender, String login, String hostname, String target, String action)
	{
		debug("Action", target + " " + sender + " " + action);

		// Strip mIRC colors and formatting
		action = Colors.removeFormattingAndColors(action);

		Message message = new Message(sender + " " + action);
		message.setIcon(R.drawable.action);
		
		if (target.equals(this.getNick())) {
			// We are the target - this is an action in a query
			Conversation conversation = server.getConversation(sender); 
			if (conversation == null) { 
				// Open a query if there's none yet
				conversation = new Query(sender);
				server.addConversationl(conversation);
				conversation.addMessage(message);
				
				Intent intent = new Intent(Broadcast.CONVERSATION_NEW);
				intent.putExtra(Broadcast.EXTRA_CONVERSATION, sender);
				intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
				service.sendBroadcast(intent);
			} else {
				Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
				intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
				intent.putExtra(Broadcast.EXTRA_CONVERSATION, sender);
				service.sendBroadcast(intent);
			}
		} else {
			// A action in a channel
			server.getConversation(target).addMessage(message);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);
		}
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
		
		Message message = new Message(sourceNick + " deoped " + recipient);
		message.setIcon(R.drawable.op);
		message.setColor(Message.COLOR_BLUE);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On DeVoice
	 */
	@Override
	protected void onDeVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("DeVoice", target + " " + recipient + "(" + sourceNick + ")");
		
		Message message = new Message(sourceNick + " devoiced " + recipient);
		message.setColor(Message.COLOR_BLUE);
		message.setIcon(R.drawable.voice);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Invite
	 */
	@Override
	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String target)
	{
		debug("Invite", target + " " + targetNick + "(" + sourceNick + ")");
		
		Message message = new Message(sourceNick + " invited " + targetNick);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
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
			server.addConversationl(new Channel(target));
			
			Intent intent = new Intent(Broadcast.CONVERSATION_NEW);
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			service.sendBroadcast(intent);
		} else {
			Message message = new Message(sender + " joined");
			message.setIcon(R.drawable.join);
			message.setColor(Message.COLOR_GREEN);
			server.getConversation(target).addMessage(message);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
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
			server.removeConversation(target);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_REMOVE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);
		} else {
			Message message = new Message(kickerNick + " kicked " + recipientNick);
			message.setColor(Message.COLOR_GREEN);
			server.getConversation(target).addMessage(message);

			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);			
		}
	}

	/**
	 * On Message
	 */
	@Override
	protected void onMessage(String target, String sender, String login, String hostname, String text)
	{
		debug("Message", target + " " + sender + " " + text);
		
		// Strip mIRC colors and formatting
		text = Colors.removeFormattingAndColors(text);

		Message message = new Message("<" + sender + "> " + text);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On Mode
	 */
	@Override
	protected void onMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode)
	{
		debug("Mode", target + " " + sourceNick + " " + mode);
		
		/*//Disabled as it doubles events (e.g. onOp and onMode will be called)
		Message message = new Message(sourceNick + " sets mode " + mode);
		server.getChannel(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CHANNEL_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CHANNEL, target);
		service.sendBroadcast(intent);
		*/
	}

	/**
	 * On Nick Change
	 */
	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick)
	{
		debug("Nick", oldNick + " " + newNick);
		
		for (String target : getChannelsByNickname(newNick)) {
			Message message = new Message(oldNick + " is now known as " + newNick);
			message.setColor(Message.COLOR_GREEN);
			server.getConversation(target).addMessage(message);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);
		}
	}

	/**
	 * On Notice
	 */
	@Override
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice)
	{
		debug("Notice", sourceNick + " " + notice);

		// Strip mIRC colors and formatting
		notice = Colors.removeFormattingAndColors(notice);
		
		// Post notice to currently selected conversation
		Conversation conversation = server.getConversation(server.getSelectedConversation());
		
		if (conversation == null) {
			// Fallback: Use ServerInfo view
			conversation = server.getConversation(ServerInfo.DEFAULT_NAME);
		}

		Message message = new Message("-" + sourceNick + "- " + notice);
		message.setIcon(R.drawable.info);
		conversation.addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, conversation.getName());
		service.sendBroadcast(intent);
	}

	/**
	 * On Op
	 */
	@Override
	protected void onOp(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("Op", target + " " + recipient + "(" + sourceNick + ")");
		
		Message message = new Message(sourceNick + " oped " + recipient);
		message.setColor(Message.COLOR_BLUE);
		message.setIcon(R.drawable.op);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
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
			server.removeConversation(target);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_REMOVE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);
		} else {
			Message message = new Message(sender + " parted");
			message.setColor(Message.COLOR_GREEN);
			message.setIcon(R.drawable.part);
			server.getConversation(target).addMessage(message);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
			service.sendBroadcast(intent);
		}
	}

	/**
	 * On Private Message
	 */
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String text)
	{
		debug("PrivateMessage", sender + " " + text);
		
		// Strip mIRC colors and formatting
		text = Colors.removeFormattingAndColors(text);

		Message message = new Message("<" + sender + "> " + text);

		Conversation conversation = server.getConversation(sender);

		if (conversation == null) { 
			// Open a query if there's none yet
			conversation = new Query(sender);
			conversation.addMessage(message);
			server.addConversationl(conversation);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_NEW);
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, sender);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			service.sendBroadcast(intent);
		} else {
			conversation.addMessage(message);
			
			Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
			intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
			intent.putExtra(Broadcast.EXTRA_CONVERSATION, sender);
			service.sendBroadcast(intent);
		}
	}

	/**
	 * On Quit
	 */
	@Override
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
	{
		debug("Quit", sourceNick);
		
		if (!sourceNick.equals(this.getNick())) {
			for (String target : getChannelsByNickname(sourceNick)) {
				Message message = new Message(sourceNick + " quitted");
				message.setColor(Message.COLOR_GREEN);
				message.setIcon(R.drawable.quit);
				server.getConversation(target).addMessage(message);
				
				Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
				intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
				intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
				service.sendBroadcast(intent);
			}
		} else {
			// XXX: We quitted
		}
	}

	/**
	 * On Topic
	 */
	@Override
	public void onTopic(String target, String topic, String setBy, long date, boolean changed)
	{
		debug("Topic", target + " " + setBy + " " + topic);
		
		if (changed) {
			Message message = new Message(setBy + " sets topic: " + topic);
			message.setColor(Message.COLOR_YELLOW);
			server.getConversation(target).addMessage(message);
		} else {
			Message message = new Message("Topic: " + topic);
			message.setColor(Message.COLOR_YELLOW);
			server.getConversation(target).addMessage(message);
		}
		
		// remember channel's topic
		((Channel) server.getConversation(target)).setTopic(topic);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
		service.sendBroadcast(intent);
	}

	/**
	 * On User List
	 */
	@Override
	protected void onUserList(String channel, User[] users)
	{
		debug("UserList", channel + " (" + users.length + ")");
		
		// XXX: Store user list somewhere and keep it updated or just broadcast some event?
	}

	/**
	 * On Voice
	 */
	@Override
	protected void onVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
	{
		debug("Voice", target + " " + recipient + "(" + sourceNick + ")");
		
		Message message = new Message(sourceNick + " voiced " + recipient);
		message.setIcon(R.drawable.voice);
		message.setColor(Message.COLOR_BLUE);
		server.getConversation(target).addMessage(message);
		
		Intent intent = new Intent(Broadcast.CONVERSATION_MESSAGE);
		intent.putExtra(Broadcast.EXTRA_SERVER, server.getId());
		intent.putExtra(Broadcast.EXTRA_CONVERSATION, target);
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
	 * Get all channels where the user with the given nickname is online
	 * 
	 * @param nickname
	 * @return Array of channel names
	 */
	private Vector<String> getChannelsByNickname(String nickname)
	{
		Vector<String> channels = new Vector<String>();
		
		for (String channel : this.getChannels()) {
			for (User user : this.getUsers(channel)) {
				if (user.getNick().equals(nickname)) {
					channels.add(channel);
					break;
				}
			}
		}
		
		Log.d(TAG, "Found " + channels.size() + " channels for nickname " + nickname);
		
		return channels;
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
