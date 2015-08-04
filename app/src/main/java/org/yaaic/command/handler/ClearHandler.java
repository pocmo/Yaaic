package org.yaaic.command.handler;

import org.yaaic.R;
import org.yaaic.command.BaseHandler;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Server;

import android.content.Context;
import android.content.Intent;

/**
 * Command: /clear
 * 
 * Clear the history of the current window
 * 
 * @author Xenega <xenega@gmx.com>
 */
public class ClearHandler extends BaseHandler
{
    /**
     * Execute /clear
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (params.length == 1) {
            conversation.clearHistory();
            conversation.clearBuffer();

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_CLEAR,
                server.getId(),
                conversation.getName()
            );
            service.sendBroadcast(intent);
        }
    }

    /**
     * Usage of /clear
     */
    @Override
    public String getUsage()
    {
        return "/clear";
    }

    /**
     * Description of /clear
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_clear);
    }
}
