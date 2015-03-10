package Commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

import Commands.Helpers.BasicFilenameFilter;
import Commands.Helpers.SearchResult;
import RITA.DataFormats;
import RITA.Host;
import RITA.ICommand;
import RITA.Parameter;

public class ls implements ICommand
{
	List<Parameter> params = null;
	Host host = null;
	
	PipedReader input = null;
	BufferedReader bufIn = null;
	PipedWriter output = new PipedWriter();
	PipedWriter listener = null;
	//BufferedWriter bufOut = new BufferedWriter(output);
	boolean fileOut = false;
	boolean doInput = false;
	
	@Override
	public void run()
	{
		String search = null;
		boolean verbose = false;
		int recursion = 1;
		BasicFilenameFilter filter = null;
		boolean caseSensitive = true;
		boolean files = true;
		boolean dirs = true;
		
		for(Parameter p: params)
		{
			switch (p.getIdentifier())
			{
				case "-default":
					search = p.getArgument();
					break;
				case "--verbose":
				case "-v":
					verbose = true;
					break;
				case "--recursive":
				case "-r":
					if (p.getArgument() != null)
						recursion += Integer.parseInt(p.getArgument());
					else
						recursion = -1;
					break;
				case "--files":
				case "-f":
					dirs = false;
					break;
				case "--directories":
				case "-d":
					files = false;
					break;
				case "--caseinsensitive":
				case "-c":
					caseSensitive = false;
					break;
			}
		}
		
		if (search != null)
		{
			search = search.replace("*", ".*");
			search = search.replace("?", ".?");
			
			filter = new BasicFilenameFilter(search, caseSensitive, recursion != 1);
		}
		File root = null;
		if (!doInput)
		{
			root = new File(host.getPath());
		}
		else
		{
			// TODO rewrite
			synchronized(listener)
			{
				try
				{
					listener.wait();
					while (input.ready())
					{
						root = new File(bufIn.readLine());
					}
				}
				catch (Exception ex)
				{
					if (host.isDebug())
						System.err.println(ex.getMessage());
				}
			}
		}
		
		if (!fileOut)
			TryPrint("Please wait while ls searches...\r\n");
		
		SearchResult results = new SearchResult(root, filter, recursion, host);
		
		if (!fileOut)
		{
			try
			{
				if (results.getResults() > 0)
					TryPrint("Found " + results.getResults() + " results in \"" + (verbose ? root.getCanonicalPath() : root.getName()) + "\"\r\n");
				else
					TryPrint("No results found!\r\n");
			} catch (IOException e)
			{
				if (host.isDebug())
					System.err.println(e.getMessage());
			}
			
			PrintLs(results, recursion, verbose, files, dirs);
		}
		else
		{
			OutLs(results, files, dirs);
		}
		
		try
		{
			output.close();
		} catch (IOException e)
		{
			
		}
	}

	public void TryPrint(String out)
	{
		try
		{
			output.write(out);
			output.flush();
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println("Error writing Command output!");
		}
	}

	private void OutLs(SearchResult results, boolean files, boolean dirs)
	{
		try
		{
			for (SearchResult result: results.getSubSearches())
			{
				if (result.getFile().isFile() && files)
				{
					TryPrint(result.getFile().getCanonicalPath()+"\r\n");
				}
				else
				{
					if (dirs)
						TryPrint(result.getFile().getCanonicalPath()+"\r\n");
					OutLs(result, files, dirs);
				}
			}
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println(e.getMessage());
		}
	}

	public void PrintLs(SearchResult results, int recursion, boolean verbose, boolean files, boolean dirs)
	{
		for(SearchResult result: results.getSubSearches())
		{
			try
			{
				String lead = "";
				for (int i = 0; i < recursion - result.getLevel(); i++)
					lead += "|";
						
				if(result.getLevel() != 0 && result.getFile().isDirectory())
					lead += "+ (" + result.getResults() + ") ";
				else
					lead += "-";
				
				if (result.getResults() > 0)
				{
					if (result.getFile().isDirectory())
					{
						if(dirs)
							TryPrint(lead + (verbose ? result.getFile().getCanonicalPath() : result.getFile().getName()) + "\r\n");
						
						if(result.getLevel() != 0)
							PrintLs(result, recursion, verbose, files, dirs);
					}
					else if (!result.getFile().isDirectory() && files)
					{
						TryPrint(lead + (verbose ? result.getFile().getCanonicalPath() : result.getFile().getName())+ "\r\n");
					}
				}
			} catch(IOException e)
			{
				if (host.isDebug())
					System.err.println("Unexpected error: " + e.getLocalizedMessage());
			}
		}
	}

	@Override
	public String getHelp()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Usage:\r\n");
		builder.append("\tls [search [-c]]  [-v] [-r [#x]] [-f or -d]\r\n\r\n");
		builder.append("\tLists the items in the current path, optionally name-matching [search].\r\n");
		builder.append("\t[search [-c][--caseinsensitive]]: Search for the specified text, and optionally search [-c]ase insensitively.\r\n");
		builder.append("\t[-v][--verbose]: Verbose output (full path names).\r\n");
		builder.append("\t[-r][--recursive]: Recursive list [#x] folders deep. Not specifying [#x] lists infinite levels.\r\n");
		builder.append("\t[-f or -d][--files or --directories]: List only [-f]iles or [-d]irectories.\r\n");
		return builder.toString();
	}

	@Override
	public String[] getCommandName()
	{
		return new String[] {"ls", "list"};
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
	public DataFormats startInput(PipedWriter in, EnumSet<DataFormats> inTypes)
			throws IOException
	{
		listener = in;
		input = new PipedReader(in);
		bufIn = new BufferedReader(input);
		
		if (inTypes.contains(DataFormats.FILE))
		{
			doInput = true;
			return DataFormats.FILE;
		}
		//else if (inTypes.contains(DataFormats.FILE_M))
			//return DataFormats.FILE_M;
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
		if (outForm == DataFormats.FILE_M)
			fileOut = true;
	}

	@Override
	public EnumSet<DataFormats> getInputFormats()
	{
		return EnumSet.of(DataFormats.FILE/*, DataFormats.FILE_M*/, DataFormats.NULL);
	}

	@Override
	public EnumSet<DataFormats> getOutputFormats()
	{
		return EnumSet.of(DataFormats.FILE_M, DataFormats.USER);
	}

	@Override
	public DataFormats getCurrentFormat()
	{
		return fileOut ? DataFormats.FILE_M : DataFormats.USER; 
	}
}


