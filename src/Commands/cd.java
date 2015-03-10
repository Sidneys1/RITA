package Commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

import RITA.*;


public class cd implements ICommand
{
	List<Parameter> params;
	Host host;
	PipedReader input = null;
	PipedWriter output = new PipedWriter();
	BufferedWriter bufOut = new BufferedWriter(output);
	DataFormats outputFormat = null;

	@Override
	public void run()
	{
		if (params.size() == 0)
		{
			try
			{
				host.getCommandHistory().push(host.getPath());
				host.setPath(File.listRoots()[0].getCanonicalPath());
				if (outputFormat == DataFormats.USER)
					TryPrint("Path set to \""+ host.getPath() + "\".");
				else if (outputFormat == DataFormats.FILE)
					TryPrint(host.getPath() + "\r\n");
				
				return;
			} catch (IOException e)
			{
				if (host.isDebug())
					System.err.println("An exception occurred - try again?");
			}
			
			try
			{
				output.close();
				bufOut.close();
			} catch (IOException e)
			{
				
			}
		}
		
		for(Parameter p: params)
		{
			String arg = p.getArgument();
			if (p.getIdentifier()== "-default" && arg != null)
			{
				try
				{
					File path = new File(host.getPath());
					if (new File(path.getCanonicalPath() + "\\" + arg).exists())
					{
						host.getCommandHistory().push(host.getPath());
						host.setPath(new File(path.getCanonicalPath() + "\\" + arg).getCanonicalPath());
					}
					else if (new File(arg).exists())
					{
						host.getCommandHistory().push(host.getPath());
						host.setPath(new File(arg).getCanonicalPath());
					}
					else if (outputFormat == DataFormats.USER)
						TryPrint("That path does not exist!");
					
					if (outputFormat == DataFormats.USER)
						TryPrint("Path set to \""+ host.getPath() + "\".");
					else if (outputFormat == DataFormats.FILE)
						TryPrint(host.getPath() + "\r\n");
					
					return;
				} 
				catch (IOException ex)
				{
					if (host.isDebug())
						System.err.println("An exception occurred - try again?");
					return;
				}
				
			}
			else if (p.getIdentifier().equals("-"))
			{
				if (!host.getCommandHistory().isEmpty())
				{
					host.setPath(host.getCommandHistory().pop());
					if (outputFormat == DataFormats.USER)
						TryPrint("Path set to \""+ host.getPath() + "\".");
					else if (outputFormat == DataFormats.FILE)
						TryPrint(host.getPath() + "\r\n");
					return;
				}
				else if (outputFormat == DataFormats.USER)
				{
					TryPrint("The path history is empty!");
					return;
				}
			}
			else // unreachable?
				TryPrint("The current path is \"" + host.getPath() + "\".");
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
	
	public String getHelp()
	{
		StringBuilder build = new StringBuilder();
		build.append("Usage:\r\n");
		build.append("\tcd (path)\r\n\r\n");
		build.append("\tChanges the path of RITA to the relative (path).\r\n");
		build.append("\tNote: a (path) of \"..\" moves up a directory.");
		return build.toString();
	}

	@Override
	public String[] getCommandName()
	{
		return new String[] {"cd"};
	}

	@Override
	public void Prepare(Host host, List<Parameter> params)
	{
		this.host = host;
		this.params = params;
	}

	
	@Override
	public PipedReader getInput()
	{
		return input;
	}

	
	@Override
	public DataFormats startInput(PipedWriter in, EnumSet<DataFormats> inTypes)
			throws IOException
	{
		if (inTypes.contains(DataFormats.FILE))
		{
			input = new PipedReader(in);
			return DataFormats.FILE;
		}
		else
			return DataFormats.NULL;
	}
	

	@Override
	public PipedWriter getOutput()
	{
		return output;
	}
	

	@Override
	public void startOutput(DataFormats outForm)
	{
		outputFormat = outForm;
	}



	@Override
	public EnumSet<DataFormats> getInputFormats()
	{
		return EnumSet.of(DataFormats.FILE);
	}

	
	@Override
	public EnumSet<DataFormats> getOutputFormats()
	{
		return EnumSet.of(DataFormats.USER, DataFormats.FILE);
	}

	@Override
	public DataFormats getCurrentFormat()
	{
		return outputFormat;
	}
}