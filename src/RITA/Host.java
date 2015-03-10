package RITA;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

/**
 * A shell class to describe the RITA host console (since I can't reference RITA itself, as it's static
 * @author sborne1
 *
 */
public class Host
{
	public boolean isDebug()
	{
		return debug;
	}

	Stack<String> commandHistory = new Stack<String>();
	ArrayList<CommandListing> Commands = null;
	String Path;
	boolean Running = true;
	Scanner Scanner = new Scanner(System.in);
	final boolean debug;
	
	public Stack<String> getCommandHistory()
	{
		return commandHistory;
	}

	public String getPath()
	{
		return Path;
	}

	public Scanner getScanner()
	{
		return Scanner;
	}

	public void setPath(String path)
	{
		Path = path;
	}

	public ArrayList<CommandListing> getCommands()
	{
		return Commands;
	}

	public Host(boolean debug) throws IOException
	{
		this.debug = debug;
		Commands = new ArrayList<CommandListing>();
		commandHistory = new Stack<String>();
		try
		{
			Path = new File(".").getCanonicalPath();
		} catch (IOException e)
		{
			e.printStackTrace();
			Path = File.listRoots()[0].getCanonicalPath();
		}
	}
	
	public boolean isRunning()
	{
		return Running;
	}

	public void setRunning(boolean running)
	{
		Running = running;
	}

	/**
	 * Determine whether a command exists
	 * @param search the command to search for
	 * @return whether or not the command exists
	 */
	public boolean CommandExists(String search)
	{
		for(CommandListing c: Commands)
		{
			for (String s: c.getNames())
			{
				if (s.equalsIgnoreCase(search))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Retrieve the command matching the name provided. ONLY to be used after CommandExists has verified your command.
	 * @param search The command to return
	 * @return The ICommand interface for the sought command
	 */
	public CommandListing getCommandByName(String search)
	{
		for(CommandListing c: Commands)
		{
			for (String s: c.getNames())
			{
				if (s.equalsIgnoreCase(search))
					return c;
			}
		}
		
		return null;
	}
}
