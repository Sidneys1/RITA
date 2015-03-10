package RITA;


import java.util.ArrayList;

/**
 * Describes a line of user input as a command followed by Parameter pairs
 * @author sborne1
 *
 */
public class Input
{
	String Command;
	ArrayList<Parameter> Parameters;
	
	public String getCommand()
	{
		return Command;
	}
	
	public void setCommand(String command)
	{
		Command = command;
	}
	
	public ArrayList<Parameter> getParameters()
	{
		return Parameters;
	}
	
	public void addParameter(Parameter param)
	{
		Parameters.add(param);
	}
	
	public void setParameters(ArrayList<Parameter> parameters)
	{
		Parameters = parameters;
	}
	
	public Input()
	{
		Parameters = new ArrayList<Parameter>();
	}
}
