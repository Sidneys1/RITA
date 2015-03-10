package RITA;

public class CommandListing
{
	String[] names;
	Class<? extends ICommand> theClass;
	public String[] getNames()
	{
		return names;
	}
	public Class<? extends ICommand> getTheClass()
	{
		return theClass;
	}
	
	/**
	 * @param names The names this command responds to
	 * @param theClass The class definition
	 */
	public CommandListing(String[] names, Class<? extends ICommand> theClass)
	{
		this.names = names;
		this.theClass = theClass;
	}
	
}
