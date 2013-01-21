/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
package org.yaaic.command.handler;

import java.util.HashMap;
import java.util.Set;

import org.yaaic.R;
import org.yaaic.command.BaseHandler;
import org.yaaic.command.CommandParser;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Context;
import android.content.Intent;

/**
 * Command: /help
 * 
 * @author Karol Gliniecki <karol.gliniecki@googlemail.com>
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class HelpHandler extends BaseHandler
{
    /**
     * Execute /help
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (params.length == 2) {
            showCommandDetails(service, server, conversation, params[1]);
        } else if (params.length == 1) {
            showAllCommands(service, server, conversation);
        } else {
            throw new CommandException(service.getString(R.string.invalid_number_of_params));
        }
    }

    /**
     * Show all available commands
     * 
     * @param conversation
     * @param server
     * @param service
     */
    private void showAllCommands(IRCService service, Server server, Conversation conversation)
    {
        CommandParser cp = CommandParser.getInstance();

        StringBuffer commandList = new StringBuffer(service.getString(R.string.available_commands));
        commandList.append("\n");

        HashMap<String, BaseHandler> commands = cp.getCommands();
        HashMap<String, String> aliases = cp.getAliases();

        Set<String> commandKeys = commands.keySet();
        Set<String> aliasesKeys = aliases.keySet();

        for (Object command : commandKeys) {
            String alias = "";
            for (Object aliasCommand : aliasesKeys) {
                if (command.equals(aliases.get(aliasCommand))) {
                    alias = " " + service.getString(R.string.logical_or) + " /" + aliasCommand;
                    break;
                }
            }
            commandList.append("/" + command.toString() + alias + " - "+commands.get(command).getDescription(service) + "\n");
        }

        Message message = new Message(commandList.toString());
        message.setColor(Message.COLOR_YELLOW);
        conversation.addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
            Broadcast.CONVERSATION_MESSAGE,
            server.getId(),
            conversation.getName()
        );

        service.sendBroadcast(intent);
    }

    /**
     * Show details of a single command
     * 
     * @param conversation
     * @param server
     * @param service
     * @param command
     * @throws CommandException
     */
    private void showCommandDetails(IRCService service, Server server, Conversation conversation, String command) throws CommandException
    {
        CommandParser cp = CommandParser.getInstance();
        HashMap<String, BaseHandler> commands = cp.getCommands();

        if (commands.containsKey(command)) {
            // XXX:I18N - String building salad :)
            Message message = new Message("Help of /" + command + "\n" + commands.get(command).getUsage() + "\n" + commands.get(command).getDescription(service) + "\n");
            message.setColor(Message.COLOR_YELLOW);
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                conversation.getName()
            );

            service.sendBroadcast(intent);
        } else {
            throw new CommandException(service.getString(R.string.unknown_command, command));
        }
    }

    /**
     * Usage of /help
     */
    @Override
    public String getUsage()
    {
        return "/help [<command>]";
    }

    /**
     * Description of /help
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_help);
    }
}
