/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Pattern;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.command.CommandParser;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Channel;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Query;
import org.yaaic.model.Server;
import org.yaaic.model.ServerInfo;
import org.yaaic.model.Status;

import android.content.Intent;

/**
 * The class that actually handles the connection to an IRC server
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCConnection extends PircBot
{
    private final IRCService service;
    private final Server server;
    private ArrayList<String> autojoinChannels;
    private Pattern mNickMatch;

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

        // XXX: Should be configurable via settings
        this.setAutoNickChange(true);

        this.setFinger("http://www.youtube.com/watch?v=oHg5SJYRHA0");
        this.updateNickMatchPattern();
    }

    /**
     * Set the nickname of the user
     * 
     * @param nickname The nickname to use
     */
    public void setNickname(String nickname)
    {
        this.setName(nickname);
        this.updateNickMatchPattern();
    }

    /**
     * Set the real name of the user
     * 
     * @param realname The realname to use
     */
    public void setRealName(String realname)
    {
        // XXX: Pircbot uses the version for "real name" and "version".
        //      The real "version" value is provided by onVersion()
        this.setVersion(realname);
    }

    /**
     * Set channels to autojoin after connect
     * 
     * @param channels
     */
    public void setAutojoinChannels(ArrayList<String> channels)
    {
        autojoinChannels = channels;
    }

    /**
     * On version (CTCP version)
     * 
     * This is a fix for pircbot as pircbot uses the version as "real name" and as "version"
     */
    @Override
    protected void onVersion(String sourceNick, String sourceLogin,    String sourceHostname, String target)
    {
        this.sendRawLine(
            "NOTICE " + sourceNick + " :\u0001VERSION " +
            "Yaaic - Yet another Android IRC client - http://www.yaaic.org" +
            "\u0001"
        );
    }

    /**
     * Set the ident of the user
     * 
     * @param ident The ident to use
     */
    public void setIdent(String ident)
    {
        this.setLogin(ident);
    }

    /**
     * On connect
     */
    @Override
    public void onConnect()
    {
        server.setStatus(Status.CONNECTED);

        service.sendBroadcast(
            Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId())
        );

        service.updateNotification(service.getString(R.string.notification_connected, server.getTitle()));

        Message message = new Message(service.getString(R.string.message_connected, server.getTitle()));
        message.setColor(Message.COLOR_GREEN);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            ServerInfo.DEFAULT_NAME
        );

        service.sendBroadcast(intent);
    }


    /**
     * On register
     */
    @Override
    public void onRegister()
    {
        // Call parent method to ensure "register" status is tracked
        super.onRegister();

        // execute commands
        CommandParser parser = CommandParser.getInstance();

        this.updateNickMatchPattern();
        for (String command : server.getConnectCommands()) {
            parser.parse(command, server, server.getConversation(ServerInfo.DEFAULT_NAME), service);
        }

        // TODO: Detect "You are now identified for <nick>" notices from NickServ and handle
        //       auto joins in onNotice instead if the user has chosen to wait for NickServ
        //       identification before auto joining channels.

        // delay 1 sec before auto joining channels
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            // do nothing
        }

        // join channels
        if (autojoinChannels != null) {
            for (String channel : autojoinChannels) {
                // Add support for channel keys
                joinChannel(channel);
            }
        } else {
            for (String channel : server.getAutoJoinChannels()) {
                joinChannel(channel);
            }
        }
    }
    /**
     * On channel action
     */
    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action)
    {
        Message message = new Message(sender + " " + action);
        message.setIcon(R.drawable.action);

        if (isMentioned(action)) {
            // highlight
            message.setColor(Message.COLOR_RED);
            service.updateNotification(
                target + ": " + sender + " " + action,
                service.getSettings().isVibrateHighlightEnabled(),
                service.getSettings().isSoundHighlightEnabled()
            );

            server.getConversation(target).setStatus(Conversation.STATUS_HIGHLIGHT);
        }

        if (target.equals(this.getNick())) {
            // We are the target - this is an action in a query
            Conversation conversation = server.getConversation(sender);
            if (conversation == null) {
                // Open a query if there's none yet
                conversation = new Query(sender);
                server.addConversation(conversation);
                conversation.addMessage(message);

                Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_NEW,
                    server.getId(),
                    sender
                );
                service.sendBroadcast(intent);
            } else {
                Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    server.getId(),
                    sender
                );
                service.sendBroadcast(intent);
            }
        } else {
            // A action in a channel
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Channel Info
     */
    @Override
    protected void onChannelInfo(String channel, int userCount, String topic)
    {
    }

    /**
     * On Deop
     */
    @Override
    protected void onDeop(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(service.getString(R.string.message_deop, sourceNick, recipient));
        message.setIcon(R.drawable.op);
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );

        service.sendBroadcast(intent);
    }

    /**
     * On DeVoice
     */
    @Override
    protected void onDeVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(service.getString(R.string.message_devoice, sourceNick, recipient));
        message.setColor(Message.COLOR_BLUE);
        message.setIcon(R.drawable.voice);
        server.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );

        service.sendBroadcast(intent);
    }

    /**
     * On Invite
     */
    @Override
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String target)
    {
        if (targetNick.equals(this.getNick())) {
            // We are invited
            Message message = new Message(service.getString(R.string.message_invite_you, sourceNick, target));
            server.getConversation(server.getSelectedConversation()).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                server.getSelectedConversation()
            );
            service.sendBroadcast(intent);
        } else {
            // Someone is invited
            Message message = new Message(service.getString(R.string.message_invite_someone, sourceNick, targetNick, target));
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Join
     */
    @Override
    protected void onJoin(String target, String sender, String login, String hostname)
    {
        if (sender.equalsIgnoreCase(getNick())) {
            // We joined a new channel
            server.addConversation(new Channel(target));

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_NEW,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        } else if (service.getSettings().showJoinPartAndQuit()) {
            Message message = new Message(
                service.getString(R.string.message_join, sender),
                Message.TYPE_MISC
            );

            message.setIcon(R.drawable.join);
            message.setColor(Message.COLOR_GREEN);
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Kick
     */
    @Override
    protected void onKick(String target, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
    {
        if (recipientNick.equals(getNick())) {
            // We are kicked
            server.removeConversation(target);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_REMOVE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        } else {
            Message message = new Message(service.getString(R.string.message_kick, kickerNick, recipientNick));
            message.setColor(Message.COLOR_GREEN);
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Message
     */
    @Override
    protected void onMessage(String target, String sender, String login, String hostname, String text)
    {
        Message message = new Message(text, sender);

        if (isMentioned(text)) {
            // highlight
            message.setColor(Message.COLOR_RED);
            service.updateNotification(
                target + ": <" + sender + "> " + text,
                service.getSettings().isVibrateHighlightEnabled(),
                service.getSettings().isSoundHighlightEnabled()
            );

            server.getConversation(target).setStatus(Conversation.STATUS_HIGHLIGHT);
        }

        server.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );
        service.sendBroadcast(intent);
    }

    /**
     * On Mode
     */
    @Override
    protected void onMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode)
    {
        // Disabled as it doubles events (e.g. onOp and onMode will be called)

        /*
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
        if (getNick().equalsIgnoreCase(newNick)) {
            this.updateNickMatchPattern();

            // Send message about own change to server info window
            Message message = new Message(service.getString(R.string.message_self_rename, newNick));
            message.setColor(Message.COLOR_GREEN);
            server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                ServerInfo.DEFAULT_NAME
            );

            service.sendBroadcast(intent);
        }

        Vector<String> channels = getChannelsByNickname(newNick);

        for (String target : channels) {
            Message message = new Message(service.getString(R.string.message_rename, oldNick, newNick));
            message.setColor(Message.COLOR_GREEN);
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Notice
     */
    @Override
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice)
    {
        // Post notice to currently selected conversation
        Conversation conversation;

        if (service.getSettings().showNoticeInServerWindow()) {
            conversation = server.getConversation(ServerInfo.DEFAULT_NAME);
        } else {
            conversation = server.getConversation(server.getSelectedConversation());

            if (conversation == null) {
                // Fallback: Use ServerInfo view
                conversation = server.getConversation(ServerInfo.DEFAULT_NAME);
            }
        }

        Message message = new Message("-" + sourceNick + "- " + notice);
        message.setIcon(R.drawable.info);
        conversation.addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            conversation.getName()
        );
        service.sendBroadcast(intent);
    }

    /**
     * On Op
     */
    @Override
    protected void onOp(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(service.getString(R.string.message_op, sourceNick, recipient));
        message.setColor(Message.COLOR_BLUE);
        message.setIcon(R.drawable.op);
        server.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );
        service.sendBroadcast(intent);
    }

    /**
     * On Part
     */
    @Override
    protected void onPart(String target, String sender, String login, String hostname)
    {
        if (sender.equals(getNick())) {
            // We parted a channel
            server.removeConversation(target);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_REMOVE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        } else if (service.getSettings().showJoinPartAndQuit()) {
            Message message = new Message(
                service.getString(R.string.message_part, sender),
                Message.TYPE_MISC
            );

            message.setColor(Message.COLOR_GREEN);
            message.setIcon(R.drawable.part);
            server.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                target
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Private Message
     */
    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String text)
    {
        Message message = new Message("<" + sender + "> " + text);

        if (isMentioned(text)) {
            message.setColor(Message.COLOR_RED);
            service.updateNotification(
                "<" + sender + "> " + text,
                service.getSettings().isVibrateHighlightEnabled(),
                service.getSettings().isSoundHighlightEnabled()
            );

            server.getConversation(sender).setStatus(Conversation.STATUS_HIGHLIGHT);
        }

        Conversation conversation = server.getConversation(sender);

        if (conversation == null) {
            // Open a query if there's none yet
            conversation = new Query(sender);
            conversation.addMessage(message);
            server.addConversation(conversation);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_NEW,
                server.getId(),
                sender
            );
            service.sendBroadcast(intent);
        } else {
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                sender
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * On Quit
     */
    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
    {
        if (sourceNick.equals(this.getNick())) {
            return;
        }

        if (service.getSettings().showJoinPartAndQuit()) {
            Vector<String> channels = getChannelsByNickname(sourceNick);

            for (String target : channels) {
                Message message = new Message(
                    service.getString(R.string.message_quit, sourceNick, reason),
                    Message.TYPE_MISC
                );

                message.setColor(Message.COLOR_GREEN);
                message.setIcon(R.drawable.quit);
                server.getConversation(target).addMessage(message);

                Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    server.getId(),
                    target
                );
                service.sendBroadcast(intent);
            }

            // Look if there's a query to update
            Conversation conversation = server.getConversation(sourceNick);

            if (conversation != null) {
                Message message = new Message(
                    service.getString(R.string.message_quit, sourceNick, reason),
                    Message.TYPE_MISC
                );

                message.setColor(Message.COLOR_GREEN);
                message.setIcon(R.drawable.quit);
                conversation.addMessage(message);

                Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    server.getId(),
                    conversation.getName()
                );
                service.sendBroadcast(intent);
            }
        }
    }

    /**
     * On Topic
     */
    @Override
    public void onTopic(String target, String topic, String setBy, long date, boolean changed)
    {
        if (changed) {
            Message message = new Message(service.getString(R.string.message_topic_set, setBy, topic));
            message.setColor(Message.COLOR_YELLOW);
            server.getConversation(target).addMessage(message);
        } else {
            Message message = new Message(service.getString(R.string.message_topic, topic));
            message.setColor(Message.COLOR_YELLOW);
            server.getConversation(target).addMessage(message);
        }

        // remember channel's topic
        ((Channel) server.getConversation(target)).setTopic(topic);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );
        service.sendBroadcast(intent);
    }

    /**
     * On User List
     */
    @Override
    protected void onUserList(String channel, User[] users)
    {
        // XXX: Store user list somewhere and keep it updated or just broadcast some event?
    }

    /**
     * On Voice
     */
    @Override
    protected void onVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(service.getString(R.string.message_voice, sourceNick, recipient));
        message.setIcon(R.drawable.voice);
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            target
        );
        service.sendBroadcast(intent);
    }

    /**
     * On remove channel key
     */
    @Override
    protected void onRemoveChannelKey(String target, String sourceNick, String sourceLogin, String sourceHostname, String key)
    {
        Message message = new Message(service.getString(R.string.message_remove_channel_key, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set channel key
     */
    @Override
    protected void onSetChannelKey(String target, String sourceNick, String sourceLogin, String sourceHostname, String key)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_key, sourceNick, key));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set secret
     */
    @Override
    protected void onSetSecret(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_secret, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove secret
     */
    @Override
    protected void onRemoveSecret(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_public, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set channel limit
     */
    @Override
    protected void onSetChannelLimit(String target, String sourceNick, String sourceLogin, String sourceHostname, int limit)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_limit, sourceNick, limit));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove channel limit
     */
    @Override
    protected void onRemoveChannelLimit(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_remove_channel_limit, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set channel ban
     */
    @Override
    protected void onSetChannelBan(String target, String sourceNick, String sourceLogin, String sourceHostname, String hostmask)
    {
        Message message = new Message(service.getString(R.string.message_set_ban, sourceNick, hostmask));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove channel ban
     */
    @Override
    protected void onRemoveChannelBan(String target, String sourceNick, String sourceLogin, String sourceHostname, String hostmask)
    {
        Message message = new Message(service.getString(R.string.message_remove_ban, sourceNick, hostmask));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set topic protection
     */
    @Override
    protected void onSetTopicProtection(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_topic_protection, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove topic protection
     */
    @Override
    protected void onRemoveTopicProtection(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_remove_topic_protection, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set no external messages
     */
    @Override
    protected void onSetNoExternalMessages(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_disable_external, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove no external messages
     */
    @Override
    protected void onRemoveNoExternalMessages(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_enable_external, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set invite only
     */
    @Override
    protected void onSetInviteOnly(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_invite_only, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove invite only
     */
    @Override
    protected void onRemoveInviteOnly(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_remove_invite_only, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set moderated
     */
    @Override
    protected void onSetModerated(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_moderated, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove moderated
     */
    @Override
    protected void onRemoveModerated(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_remove_moderated, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On set private
     */
    @Override
    protected void onSetPrivate(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_private, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On remove private
     */
    @Override
    protected void onRemovePrivate(String target, String sourceNick, String sourceLogin, String sourceHostname)
    {
        Message message = new Message(service.getString(R.string.message_set_channel_public, sourceNick));
        message.setColor(Message.COLOR_BLUE);
        server.getConversation(target).addMessage(message);

        service.sendBroadcast(
            Broadcast.createConversationIntent(Broadcast.CONVERSATION_MESSAGE, server.getId(), target)
        );
    }

    /**
     * On unknown
     */
    @Override
    protected void onUnknown(String line)
    {
        Message message = new Message(line);
        message.setIcon(R.drawable.action);
        message.setColor(Message.COLOR_GREY);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            ServerInfo.DEFAULT_NAME
        );
        service.sendBroadcast(intent);
    }

    /**
     * On server response
     */
    @Override
    protected void onServerResponse(int code, String response)
    {
        if (code == 4) {
            // User has registered with the server
            onRegister();
            return;
        }
        if (code == 372 || code == 375 || code == 376) {
            // Skip MOTD
            return;
        }

        if (code >= 200 && code < 300) {
            // Skip 2XX responses
            return;
        }

        if (code == 353 || code == 366 || code == 332 || code == 333) {
            return;
        }

        if (code < 10) {
            // Skip server info
            return;
        }

        // Currently disabled... to much text
        Message message = new Message(response);
        message.setColor(Message.COLOR_GREY);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            ServerInfo.DEFAULT_NAME
        );
        service.sendBroadcast(intent);
    }

    /**
     * On disconnect
     */
    @Override
    public void onDisconnect()
    {
        // Call parent method to ensure "register" status is tracked
        super.onDisconnect();

        if (service.getSettings().isReconnectEnabled() && server.getStatus() != Status.DISCONNECTED) {
            setAutojoinChannels(server.getCurrentChannelNames());

            server.clearConversations();
            server.setStatus(Status.CONNECTING);
            service.connect(server);
        } else {
            server.setStatus(Status.DISCONNECTED);
        }

        service.updateNotification(service.getString(R.string.notification_disconnected, server.getTitle()));

        Intent sIntent = Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId());
        service.sendBroadcast(sIntent);

        Collection<Conversation> conversations = server.getConversations();

        for (Conversation conversation : conversations) {
            Message message = new Message(service.getString(R.string.message_disconnected));
            message.setIcon(R.drawable.error);
            message.setColor(Message.COLOR_RED);
            server.getConversation(conversation.getName()).addMessage(message);

            Intent cIntent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                conversation.getName()
            );
            service.sendBroadcast(cIntent);
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
        String[] channelArray = getChannels();

        for (String channel : channelArray) {
            User[] userArray = getUsers(channel);
            for (User user : userArray) {
                if (user.getNick().equals(nickname)) {
                    channels.add(channel);
                    break;
                }
            }
        }

        return channels;
    }

    /**
     * Get list of users in a channel as array of strings
     * 
     * @param channel Name of the channel
     */
    public String[] getUsersAsStringArray(String channel)
    {
        User[] userArray = getUsers(channel);
        int mLength = userArray.length;
        String[] users = new String[mLength];

        for (int i = 0; i < mLength; i++) {
            users[i] = userArray[i].getPrefix() + userArray[i].getNick();
        }

        return users;
    }

    /**
     * Get a user by channel and nickname
     * 
     * @param channel The channel the user is in
     * @param nickname The nickname of the user (with or without prefix)
     * @return the User object or null if user was not found
     */
    public User getUser(String channel, String nickname)
    {
        User[] users = getUsers(channel);
        int mLength = users.length;

        for (int i = 0; i < mLength; i++) {
            if (nickname.equals(users[i].getNick())) {
                return users[i];
            }
            if (nickname.equals(users[i].getPrefix() + users[i].getNick())) {
                return users[i];
            }
        }

        return null;
    }

    /**
     * Quits from the IRC server with default reason.
     */
    @Override
    public void quitServer()
    {
        new Thread() {
            @Override
            public void run() {
                quitServer(service.getSettings().getQuitMessage());
            }
        }.start();
    }

    /**
     * Check whether the nickname has been mentioned.
     * 
     * @param text The text to check for the nickname
     * @return true if nickname was found, otherwise false
     */
    public boolean isMentioned(String text)
    {
        return mNickMatch.matcher(text).find();
    }

    /**
     * Update the nick matching pattern, should be called when the nickname changes.
     */
    private void updateNickMatchPattern()
    {
        mNickMatch = Pattern.compile("(?:^|[\\s?!'�:;,.])"+Pattern.quote(getNick())+"(?:[\\s?!'�:;,.]|$)", Pattern.CASE_INSENSITIVE);
    }
}
