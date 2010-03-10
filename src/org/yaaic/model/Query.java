package org.yaaic.model;

/**
 * A query (a private chat between to users)
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class Query extends Conversation
{
	/**
	 * Create a new query
	 * 
	 * @param name The user's nickname
	 */
	public Query(String name)
	{
		super(name);
	}
	
	/**
	 * Get the type of this conversation
	 */
	public int getType()
	{
		return Conversation.TYPE_QUERY;
	}
}
