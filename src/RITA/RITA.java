package RITA;

import java.io.File;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * RITA Is Totally an Acronym...
 * 
 * @author sborne1
 * @version 0.1.4.0
 */
public class RITA
{
	static final String version = "0.1.4.0";
	static final boolean debug = true; // Shows output to System.err... Usually
										// lots of "write end dead"

	/**
	 * The entry point of the RITA command interface
	 * 
	 * @param args
	 *            Not currently used... Will eventually launch script files or
	 *            etc. (*.rsf?)
	 * @throws Exception
	 *             Sometimes
	 */
	public static void main(String[] args) throws Exception
	{
		Host host = new Host(debug);
		PopulateHost(host);

		if (host.getCommands().size() == 0)
		{
			System.out.println("RITA " + version);
			System.out
					.println("\tNo commands loaded... Check your Commands jar!");
		} else
		{
			System.out.println("RITA " + version + ": Loaded "
					+ host.getCommands().size() + " Commands");
			System.out.println("Type \"help\" for more information.");
		}

		Scanner scan = host.getScanner();
		do
		{
			Workflow work = getInput(scan, host.getPath().concat("> "), host);
			if (work != null)
			{
				ArrayList<WorkflowItem> workflow = work.getWorkflowItems();
				ArrayList<Thread> threads = new ArrayList<Thread>();
				WorkflowItem last = workflow
						.get(work.getWorkflowItems().size() - 1);

				for (int i = 0; i < workflow.size() - 1; i++)
				{
					ICommand current = workflow.get(i).getCommand();
					ICommand next = workflow.get(i + 1).getCommand();

					// Prepare ICommand
					current.Prepare(host, workflow.get(i).args.Parameters);
					// Negotiate output format with next ICommand
					current.startOutput(next.startInput(current.getOutput(),
							current.getOutputFormats()));
					// Create thread
					threads.add(new Thread((Runnable) current));
				}

				// Prepare the last ICommand to output DataFormats.USER to the
				// console
				last.getCommand().Prepare(host, last.args.Parameters);
				Thread th = new Thread((Runnable) last.getCommand());
				PipedWriter output = last.getCommand().getOutput();
				PipedReader reader = new PipedReader(output);

				last.getCommand().startOutput(DataFormats.USER);
				threads.add(th);

				for (Thread thr : threads)
					thr.start();
				int writes = 0;

				CharBuffer buffer = CharBuffer.allocate(255);
				try
				{
					do
					{
						buffer.clear();
						// Problem: this reads. thread ends while this is
						// locked. times out?
						int ret = reader.read(buffer);
						if (ret > 0)
						{
							writes++;
							buffer.flip();
							System.out.print(buffer.toString());
						} else if (ret == -1)
							break;

					} while (th.isAlive());
				} catch (Exception ex)
				{
					if (host.debug)
						System.err.println(ex.getMessage());
				}

				reader.close();

				System.out.print("\r\n[Command End: ");

				for (WorkflowItem dev : work.getWorkflowItems())
				{
					System.out.print(dev.getCommand().getCommandName()[0]
							+ " piped " + dev.getCommand().getCurrentFormat()
							+ " to ");
				}
				System.out.println("stdout, " + writes + " writes]");
			}
		} while (host.isRunning());
	}

	/**
	 * Initializes the RITA host to contain all defined commands.
	 * 
	 * @param host
	 *            The object representing the RITA host
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	private static void PopulateHost(Host host) throws Exception
	{
		ArrayList<CommandListing> commandList = host.getCommands();
		ArrayList<String> classes = new ArrayList<String>();

		JarFile jarfile;
		File file = null;

		try
		{

			file = new File("." + File.separator + "Commands.jar");
			//File.
			if (!file.exists())
			{
				System.out
						.println("Commands Jar is nonexistant! No commands loaded.");
				return;
			}

			jarfile = new JarFile("." + File.separator + "Commands.jar");
			Enumeration<JarEntry> entries = jarfile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry e = entries.nextElement();
				if (!e.isDirectory() && e.getName().endsWith(".class"))
				{
					String s = e.getName();
					classes.add(s.substring(0, s.length() - 6)
							.replace('/', '.'));
				}
			}
			jarfile.close();
		} catch (Exception e)
		{
			System.out
					.println("An unexpected error occurred while loading commands: "
							+ e.getMessage());
		}

		try
		{

			URL url = new URL("file:\\" + file.getCanonicalPath());
			URL[] urls = new URL[] { url };
			ClassLoader cl = new URLClassLoader(urls);

			for (String s : classes)
			{
				Class<?> cls = cl.loadClass(s);
				if (ICommand.class.isAssignableFrom(cls)) // ICommand.class.isInstance(item))
				{
					Object item = cls.getConstructor().newInstance();

					@SuppressWarnings("unchecked")
					CommandListing ls = new CommandListing(
							((ICommand) item).getCommandName(),
							(Class<? extends ICommand>) cls);
					commandList.add(ls);
				}
			}

		} catch (Exception e)
		{
			System.out
					.println("An unexpected error occurred while loading commands: "
							+ e.getMessage());
		}
	}

	/**
	 * Processes input that the user enters
	 * 
	 * @param scan
	 *            The Scanner to acquire input from (Allows script files!)
	 * @param Message
	 *            The message (if any) to display to the user
	 * @param host
	 *            The Host that called this method
	 * @return The workflow the user requested
	 */
	static Workflow getInput(Scanner scan, String Message, Host host)
	{
		System.out.print(Message);

		String line = scan.nextLine();

		if (line == null || line.trim() == "")
			return null;

		Workflow returnWork = new Workflow();

		ArrayList<WorkflowItem> work = returnWork.getWorkflowItems();
		String[] parts = line.split("\\|");

		for (String part : parts)
		{
			Input procPart = processSingleInput(part.trim());
			try
			{
				if (host.CommandExists(procPart.getCommand()))
				{
					ICommand c = host.getCommandByName(procPart.getCommand())
							.getTheClass().getConstructor().newInstance();
					WorkflowItem wfi = new WorkflowItem(c, procPart);
					work.add(wfi);
				} else
				{
					System.out.println("Command does not exist: \""
							+ procPart.getCommand() + "\"");
					return null;
				}
			} catch (Exception e)
			{
				System.out.println("Error Processing input: "
						+ e.getLocalizedMessage());
			}
		}

		return returnWork;
	}

	/**
	 * Processes a single command request
	 * 
	 * @param line
	 *            The text of the command request
	 * @return The processed command input
	 */
	static Input processSingleInput(String line)
	{
		Input in = new Input();

		Scanner sc = new Scanner(line);
		in.setCommand(sc.next()); // First line is always the command

		ArrayList<Parameter> params = in.getParameters();
		Parameter lastParam = null;
		String string = null;
		boolean stringing = false;
		while (sc.hasNext())
		{
			String next = sc.next();
			if (stringing)
			{
				if (next.endsWith("\""))
				{
					stringing = false;
					string += " " + next.substring(0, next.length() - 1);
					lastParam.argument = string;
					string = null;
				} else
					string += " " + next;
			} else if (next.startsWith("-"))
			{
				Parameter newParam = new Parameter();
				newParam.setIdentifier(next);
				params.add(newParam);
				lastParam = newParam;
			} else if (next.startsWith("\""))
			{
				if (lastParam == null)
				{
					Parameter newParam = new Parameter();
					newParam.setIdentifier("-default");
					params.add(newParam);
					lastParam = newParam;
				}
				stringing = true;
				string = next.substring(1);
			} else if (lastParam != null)
			{
				lastParam.setArgument(next);
			} else
			{
				Parameter newParam = new Parameter();
				newParam.setIdentifier("-default");
				newParam.setArgument(next);
				params.add(newParam);
				lastParam = newParam;
			}
		}
		sc.close();
		return in;
	}
}
