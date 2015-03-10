package RITA;


/**
 * Describes a parameter/argument pair
 * @author sborne1
 *
 */
public class Parameter
{
	String identifier;
	String argument;
	
	public String getIdentifier()
	{
		return identifier;
	}
	
	public String getArgument()
	{
		return argument;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public void setArgument(String argument)
	{
		this.argument = argument;
	}
}
