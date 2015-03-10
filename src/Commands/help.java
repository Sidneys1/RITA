package Commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

import RITA.CommandListing;
import RITA.DataFormats;
import RITA.Host;
import RITA.ICommand;
import RITA.Parameter;

public class help implements ICommand
{
	List<Parameter> params = null;
	Host host = null;
	PipedWriter output = new PipedWriter();
	BufferedWriter bufOut = new BufferedWriter(output);
	PipedReader input = null;
	boolean doOutput = true;

	@Override
	public void run()
	{
		for (Parameter p : params)
		{
			if (p.getIdentifier() == "-default")
			{
				if (host.CommandExists(p.getArgument()))
				{
					CommandListing c = host.getCommandByName(p.getArgument());
					try
					{
						TryPrint(c.getTheClass().getConstructor().newInstance()
								.getHelp());
					} catch (Exception e)
					{
						// System.out.println("Error printing help...");
						TryPrint("Error printing help...");
					}
				} else
					TryPrint("No such command exists!");
				// System.out.println("No such command exists!");

				
			}
		}

		// List

		// System.out.println("Available Commands:");
		TryPrint("Available Commands:\r\n");
		for (CommandListing c : host.getCommands())
		{
			String[] names = c.getNames();
			// System.out.print("\t"+ names[0]);
			TryPrint('\t' + names[0]);

			if (names.length > 1)
			{
				// System.out.print(" \t(Aliases: " + names[1]);
				TryPrint(" \t(Aliases: " + names[1]);
				for (int i = 2; i < names.length; i++)
				{
					// System.out.print(", " + names[i]);
					TryPrint(", " + names[i]);
				}
				// System.out.print(")");
				TryPrint(')');
			}
			// System.out.println();
			TryPrint("\r\n");
		}
		// System.out.println("Enter \"help help\" for information on the Help format.");
		TryPrint("Enter \"help help\" for more information on the Help format.");
		
		try
		{
			output.close();
			bufOut.close();
		} catch (IOException e)
		{
			
		}
	}

	private void TryPrint(char c)
	{
		try
		{
			bufOut.write(c);
			bufOut.flush();
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println("Error writing command output! " + e.getMessage());
		}
	}

	public void TryPrint(String out)
	{
		try
		{
			bufOut.write(out);
			bufOut.flush();
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println("Error writing Command output!");
		}
	}

	@Override
	public String getHelp()
	{
		StringBuilder build = new StringBuilder();
		build.append("Usage:\r\n");
		build.append("\thelp [command]\r\n\r\n");
		build.append("\tLists the available commands.\r\n");
		build.append("\tAlternately displays information about [command].\r\n");
		build.append("\tHelp format:\r\n");
		build.append("\t\t[bracketed] items are optional, (parenthetical) items are required.\r\n");
		build.append("\t\tItems with a \"-\" identifyer are flags, and may have additional arguments of their own.\r\n");
		build.append("\t\tItems without a dash are plain-text arguments.\r\n");
		build.append("\t\t\"(-a or -b)\" indicates that either flag a or flag b are required, while \"(-a and/or -b)\" indicats that a and b, or a or b is valid.\r\n");
		return build.toString();
	}

	@Override
	public String[] getCommandName()
	{
		return new String[] { "help" };
	}

	@Override
	public void Prepare(Host host, List<Parameter> params)
	{
		this.params = params;
		this.host = host;
	}

	@Override
	public PipedReader getInput()
	{
		return input;
	}

	@Override
	public PipedWriter getOutput()
	{
		return output;
	}

	@Override
	public DataFormats startInput(PipedWriter in, EnumSet<DataFormats> inType)
			throws IOException
	{
		input = new PipedReader(in);
		return DataFormats.NULL;
	}

	@Override
	public EnumSet<DataFormats> getOutputFormats()
	{
		return EnumSet.of(DataFormats.USER);
	}

	@Override
	public void startOutput(DataFormats outForm)
	{
		doOutput = outForm == DataFormats.USER; 
	}

	@Override
	public EnumSet<DataFormats> getInputFormats()
	{
		return EnumSet.of(DataFormats.NULL);
	}

	@Override
	public DataFormats getCurrentFormat()
	{
		return doOutput ? DataFormats.USER : DataFormats.NULL;
	}
}
