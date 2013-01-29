/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari
Copyright 2012 Daniel E. Moctezuma <democtezuma@gmail.com>

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Pattern;

import org.jibble.pircbot.NickAlreadyInUseException;
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
import android.util.Log;

/**
 * The class that actually handles the connection to an IRC server
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCConnection extends PircBot
{
    private static final String TAG = "Yaaic/IRCConnection";
    private final IRCService service;
    private final Server server;
    private ArrayList<String> autojoinChannels;
    private Pattern mNickMatch;

    private boolean ignoreMOTD = true;
    private boolean debugTraffic = false;
    private boolean isQuitting = false;
    private boolean disposeRequested = false;
    private final Object isQuittingLock = new Object();

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

        this.debugTraffic = service.getSettings().debugTraffic();

        // XXX: Should be configurable via settings
        this.setAutoNickChange(true);

        this.setFinger("http://www.youtube.com/watch?v=oHg5SJYRHA0");
        this.updateNickMatchPattern();
    }

    /**
     * This method handles events when any line of text arrives from the server.
     *
     * We are intercepting this method call for logging the IRC traffic if
     * this debug option is set.
     */
    @Override
    protected void handleLine(String line) throws NickAlreadyInUseException, IOException {
        if (debugTraffic) {
            Log.v(TAG, server.getTitle() + " :: " + line);
        }

        super.handleLine(line);
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
            "Yaaic - Yet Another Android IRC Client - http://www.yaaic.org" +
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

        server.setMayReconnect(true);

        ignoreMOTD = service.getSettings().isIgnoreMOTDEnabled();

        service.sendBroadcast(
            Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId())
        );

        service.notifyConnected(server.getTitle());

        Message message = new Message(service.getString(R.string.message_connected, server.getTitle()));
        message.setColor(Message.COLOR_GREEN);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Message infoMessage = new Message(service.getString(R.string.message_now_login));
        infoMessage.setColor(Message.COLOR_GREY);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(infoMessage);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            ServerInfo.DEFAULT_NAME
        );

        if (server.getAuthentication().hasNickservCredentials()) {
            identify(server.getAuthentication().getNickservPassword());
        }

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

        Message infoMessage = new Message(service.getString(R.string.message_login_done));
        infoMessage.setColor(Message.COLOR_GREY);
        server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(infoMessage);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            ServerInfo.DEFAULT_NAME
        );

        service.sendBroadcast(intent);
    }
    /**
     * On channel action
     */
    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action)
    {
        Conversation conversation;

        Message message = new Message(sender + " " + action);
        message.setIcon(R.drawable.action);

        String queryNick = target;
        if (queryNick.equals(this.getNick())) {
            // We are the target - this is an action in a query
            queryNick = sender;
        }
        conversation = server.getConversation(queryNick);

        if (conversation == null) {
            // Open a query if there's none yet
            conversation = new Query(queryNick);
            conversation.setHistorySize(service.getSettings().getHistorySize());
            server.addConversation(conversation);
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_NEW,
                server.getId(),
                queryNick
            );
            service.sendBroadcast(intent);
        } else {
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                queryNick
            );
            service.sendBroadcast(intent);
        }

        if (sender.equals(this.getNick())) {
            // Don't notify for something sent in our name
            return;
        }

        boolean mentioned = isMentioned(action);
        if (mentioned || target.equals(this.getNick())) {
            if (conversation.getStatus() != Conversation.STATUS_SELECTED || !server.getIsForeground()) {
                service.addNewMention(
                    server.getId(),
                    conversation,
                    conversation.getName() + ": " + sender + " " + action,
                    service.getSettings().isVibrateHighlightEnabled(),
                    service.getSettings().isSoundHighlightEnabled(),
                    service.getSettings().isLedHighlightEnabled()
                );
            }
        }

        if (mentioned) {
            // highlight
            message.setColor(Message.COLOR_RED);
            conversation.setStatus(Conversation.STATUS_HIGHLIGHT);
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
        if (sender.equalsIgnoreCase(getNick()) && server.getConversation(target) == null) {
            // We joined a new channel
            Conversation conversation = new Channel(target);
            conversation.setHistorySize(service.getSettings().getHistorySize());
            server.addConversation(conversation);

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
            service.ackNewMentions(server.getId(), target);
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
        Conversation conversation = server.getConversation(target);

        if (isMentioned(text)) {
            // highlight
            message.setColor(Message.COLOR_RED);
            if (conversation.getStatus() != Conversation.STATUS_SELECTED || !server.getIsForeground()) {
                service.addNewMention(
                    server.getId(),
                    conversation,
                    target + ": <" + sender + "> " + text,
                    service.getSettings().isVibrateHighlightEnabled(),
                    service.getSettings().isSoundHighlightEnabled(),
                    service.getSettings().isLedHighlightEnabled()
                );
            }

            conversation.setStatus(Conversation.STATUS_HIGHLIGHT);
        }

        conversation.addMessage(message);

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
            service.ackNewMentions(server.getId(), target);
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
    protected void onPrivateMessage(String sender, String login, String hostname, String target, String text)
    {
        Message message = new Message("<" + sender + "> " + text);
        String queryNick = sender;

        if (queryNick.equals(this.getNick())) {
            queryNick = target;
        }
        Conversation conversation = server.getConversation(queryNick);

        if (conversation == null) {
            // Open a query if there's none yet
            conversation = new Query(queryNick);
            conversation.setHistorySize(service.getSettings().getHistorySize());
            conversation.addMessage(message);
            server.addConversation(conversation);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_NEW,
                server.getId(),
                queryNick
            );
            service.sendBroadcast(intent);
        } else {
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                queryNick
            );
            service.sendBroadcast(intent);
        }

        if (sender.equals(this.getNick())) {
            // Don't notify for something sent in our name
            return;
        }

        if (conversation.getStatus() != Conversation.STATUS_SELECTED || !server.getIsForeground()) {
            service.addNewMention(
                server.getId(),
                conversation,
                "<" + sender + "> " + text,
                service.getSettings().isVibrateHighlightEnabled(),
                service.getSettings().isSoundHighlightEnabled(),
                service.getSettings().isLedHighlightEnabled()
            );
        }

        if (isMentioned(text)) {
            message.setColor(Message.COLOR_RED);
            conversation.setStatus(Conversation.STATUS_HIGHLIGHT);
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

        // update the displayed conversation title if necessary
        intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_TOPIC,
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
        if ((code == 372 || code == 375) && ignoreMOTD) {
            return;
        }
        if (code == 376 && ignoreMOTD) {
            Message motdMessage = new Message(service.getString(R.string.message_motd_suppressed));
            motdMessage.setColor(Message.COLOR_GREY);
            server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(motdMessage);
            ignoreMOTD = false;
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

            server.setStatus(Status.CONNECTING);
            service.connect(server);
        } else {
            server.setStatus(Status.DISCONNECTED);
        }

        service.notifyDisconnected(server.getTitle());

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

        synchronized(isQuittingLock) {
            isQuitting = false;
            if (disposeRequested) {
                super.dispose();
            }
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
        quitServer(service.getSettings().getQuitMessage());
    }

    @Override
    public void quitServer(final String message)
    {
        synchronized(isQuittingLock) {
            isQuitting = true;
        }

        new Thread() {
            @Override
            public void run() {
                superClassQuitServer(message);
            }
        }.start();
    }

    private final void superClassQuitServer(String message)
    {
        super.quitServer(message);
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

    @Override
    public void dispose()
    {
        synchronized(isQuittingLock) {
            if (isQuitting) {
                disposeRequested = true;
            } else {
                super.dispose();
            }
        }
    }
}
