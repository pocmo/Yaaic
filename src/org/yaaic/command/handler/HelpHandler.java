package org.yaaic.command.handler;

import java.util.HashMap;
import java.util.Set;

import org.yaaic.command.BaseHandler;
import org.yaaic.command.CommandParser;
import org.yaaic.exception.CommandException;
import org.yaaic.irc.IRCService;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Conversation;
import org.yaaic.model.Message;
import org.yaaic.model.Server;

import android.content.Intent;

/**
 * Command: /help 
 * 
 * @author Karol Gliniecki <karol.gliniecki@googlemail.com>
 */
public class HelpHandler extends BaseHandler {
	
	private String desc = "lists all available commands";

	/**
	 * Execute /help
	 */
	@Override
	public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException {
		if (conversation.getType() != Conversation.TYPE_CHANNEL) {
			throw new CommandException("Only usable from within a channel");
		}
		
		CommandParser cp = CommandParser.getInstance();
		
		StringBuffer commandList = new StringBuffer("available commands: \n");
		HashMap<String, BaseHandler> commands = cp.getCommands();
		
		Object[] commandKeys = commands.keySet().toArray();
		
		for (Object command: commandKeys) {
			commandList.append("/"+command.toString() + " - "+commands.get(command).getDescription()+"\n");
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
	 * 
	 *Usage of /help
	 */
	@Override
	public String getUsage() {
		return "/help";
	}

	/**
	 * Description of /help
	 */
	@Override
	public String getDescription() {
		return desc;
	}

}
