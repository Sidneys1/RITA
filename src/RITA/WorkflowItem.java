package RITA;

/**
 * Describes a single work item on the workflow
 * @author Sid
 *
 */
public class WorkflowItem
{
	ICommand command;
	Input args;
	public ICommand getCommand()
	{
		return command;
	}
	
	public Input getArgs()
	{
		return args;
	}
	
	/**
	 * @param command The command called in this Workflow Item
	 * @param args The arguments to pass to the command
	 */
	public WorkflowItem(ICommand command, Input args)
	{
		this.command = command;
		this.args = args;
	}
	
}
