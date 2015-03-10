package RITA;

import java.util.ArrayList;

/**
 * Describes a workflow requested by user piping
 * @author Sid
 *
 */
public class Workflow
{
	ArrayList<WorkflowItem> wfitems = null;

	public ArrayList<WorkflowItem> getWorkflowItems()
	{
		return wfitems;
	}

	public Workflow()
	{
		wfitems = new ArrayList<WorkflowItem>();
	}
}
