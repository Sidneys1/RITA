package RITA;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Test
{
	public static String[] commands = new String[] { "help", "exit", "ls" };
	public static void main(String[] args) throws IOException
	{
		File dir1 = new File(".");
		String path = dir1.getCanonicalPath();
		Scanner scan = new Scanner(System.in);
		for (String[] input = getInput(scan, path + "> "); 
			!input[0].equalsIgnoreCase("exit");
			input = getInput(scan, path + "> "))
		{
			Execute(input, path);
		}
		
		System.out.println("Test program is exiting...");
	}
	
	private static String[] getInput(Scanner scan, String Message)
	{
		ArrayList<String> input = new ArrayList<String>();
		System.out.print(Message);
		
		String line = scan.nextLine();
		Scanner sc = new Scanner(line);
		while(sc.hasNext())
		{
			input.add(sc.next());
		}
		sc.close();
		return input.toArray(new String[0]);
	}
	
	private static void Execute(String[] command, String path) throws IOException
	{
		boolean argsExist = command.length > 1;
		List<String> args = null;
		
		if (argsExist)
		{
			args = Arrays.asList(command);
		}
		
		switch(command[0])
		{
			case "help":
				if (command.length > 1)
				{
					switch (command[1])
					{
						case "ls":
							System.out.println("Usage:");
							System.out.println("\tls [-v] [-r]");
							System.out.println("\r\n\tLists the files and folders within the current path.");
							System.out.println("\t[-v]: Verbose output.");
							System.out.println("\t[-r]: Recursive output.");
							break;
						
						case "exit":
							System.out.println("Usage:");
							System.out.println("\texit");
							System.out.println("\r\n\tExits the Test terminal.");
							break;
							
						case "help":
							System.out.println("Usage:");
							System.out.println("\thelp [command]");
							System.out.println("\r\n\tLists the available commands.");
							System.out.println("\t[command]: Lists the usage of the specified command.");
							break;
						
					}
				}
				else
				{
					System.out.println("The available commands are:");
					for (int i = 0; i < commands.length; i++)
						System.out.println("\t" + commands[i]);
				}
				break;
				
			case "ls":
				boolean longOut = argsExist && args.contains("-v");
				boolean recursive = argsExist && args.contains("-r");
				File l = new File(path);
				System.out.println("Items in \"" + l.getName() + "\"");
				Ls(l, 0, recursive, longOut);
				break;
			default:
				System.out.println("\"" + command[0] + "\" is not a valid command.");
				return;
		}
		System.out.println("[End Output]");
	}
	
	private static void Ls(File dir1, int level, boolean recursive, boolean longOut) throws IOException
	{
		File[] files = dir1.listFiles();
		String levelStr = "";
		for (int l = 0; l < level; l++)
				levelStr += "│";
		levelStr += "├";
		for (int i = 0; i < files.length; i++)
		{
			String tempStr = "─ ";
			if (recursive && files[i].isDirectory() && files[i].list().length > 0)
				tempStr = "┬ ";
			System.out.println("\t" + levelStr + tempStr +
					(longOut ? files[i].getAbsoluteFile() : files[i].getName()) +
					(files[i].isDirectory() ? " (d)" : ""));
			if (recursive && files[i].isDirectory() && files[i].list().length > 0)
			{
				Ls(files[i], level + 1, recursive, longOut);
			}
		}
	}
}