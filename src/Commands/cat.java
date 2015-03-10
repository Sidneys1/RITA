package Commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

import RITA.DataFormats;
import RITA.Host;
import RITA.ICommand;
import RITA.Parameter;

public class cat implements ICommand
{
	Host host;
	List<Parameter> params;

	PipedReader input;
	PipedWriter output = new PipedWriter();
	BufferedWriter bufOut = new BufferedWriter(output); // Since we're reading
														// files, let's buffer
														// the output...

	DataFormats currOut = DataFormats.USER;
	DataFormats currIn = DataFormats.NULL;

	@Override
	public void run()
	{
		File file = null;
		FileReader fread = null;
		BufferedReader bufFil = null;

		// If there is no input, we need to use arguments
		if (currIn == DataFormats.NULL)
			file = openFromParams(file);
		// If the output is a single path, we can just read it and continue...
		else if (currIn == DataFormats.FILE)
		{
			StringBuilder b = new StringBuilder();
			int read = 0;
			try
			{
				do
				{
					read = input.read();
					if (read > 0)
					{
						b.append((char)read);
					}
				} while (read != -1);
				file = new File(b.toString());
				
				if (!verifyFile(file))
					file = null;
			} catch (Exception e){}
		} else if (currIn == DataFormats.FILE_M)
		{
			//ArrayList<String> = new ArrayList<String>();
		}

		// OK, now we can output!

		if (file != null)
		{
			if (currOut == DataFormats.USER)
				outputUser(file, fread, bufFil);
		}
		//else
		
		try
		{
			output.close();
			bufOut.close();
		} catch (IOException e)
		{
			
		}
	}

	private void outputUser(File file, FileReader fread, BufferedReader bufFil)
	{
		// Catch any errors...
		try
		{
			fread = new FileReader(file);
			bufFil = new BufferedReader(fread);
			String read = null;
			do
			{
				read = bufFil.readLine();
				if (read != null)
					TryPrintln(read);
			} while (read != null);
			TryPrint("[End of File]");
		} catch (FileNotFoundException e)
		{
			PrintError("File does not exist!", "File does not exist!");
		} catch (IOException e)
		{
			PrintError("An unknown error occurred.", "An error occurred: "+ e.getMessage());
		} finally
		{
			try
			{
				if (fread != null)
					fread.close();
				if (bufFil != null)
					bufFil.close();
			} catch (IOException ex)
			{
			}
		}
	}

	/**
	 * Selects the parameter specified file and determines if it's readable.
	 * 
	 * @param file
	 *            The file to return
	 * @returns null if error
	 */
	private File openFromParams(File file)
	{
		// Check if default parameter exist
		if (!params.isEmpty() && params.get(0).getIdentifier() == "-default")
		{
			// Set file to parameter specified path
			file = new File(params.get(0).getArgument());

			if (!verifyFile(file))
				return null;

			// File is readable! We can cat!
			return file;
		} else
		// if no default parameter exists
		{
			PrintError("No file specified!", "File not specified, exiting CAT.");

			// Return a null, signifying errors exist.
			return null;
		}
	}

	/**
	 * @param file The file to verify
	 */
	private boolean verifyFile(File file)
	{
		// Check if the file is unreadable
		if (!(file.exists() && file.isFile() && file.canRead()))
		{
			// Choose output based on current setting
			PrintError("File is not readable or does not exist.", "File is not readable or does not exist.");

			// Return false, signifying errors exist.
			return false;
		}
		return true;
	}

	private void PrintError(String userMessage, String errorMessage)
	{
		if (currOut == DataFormats.USER && userMessage != null)
			TryPrint(userMessage);
		if (host.isDebug() && errorMessage != null)
			System.err.println(errorMessage);
	}
	
	private void TryPrintln(String read)
	{
		try
		{
			bufOut.write(read);
			bufOut.newLine();
			bufOut.flush();
			synchronized (output)
			{
				output.notify();
			}
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println("Error writing Command output!");
		}
	}

	public void TryPrint(String out)
	{
		try
		{
			bufOut.write(out);
			bufOut.flush();
			synchronized (output)
			{
				output.notify();
			}
		} catch (IOException e)
		{
			if (host.isDebug())
				System.err.println("Error writing Command output!");
		}
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
		input = new PipedReader(in);
		if (inTypes.contains(DataFormats.FILE))
			currIn = DataFormats.FILE;
		else if (inTypes.contains(DataFormats.FILE_M))
			currIn = DataFormats.FILE_M;
		return currIn;
	}

	@Override
	public PipedWriter getOutput()
	{
		return output;
	}

	@Override
	public void startOutput(DataFormats outForm)
	{
		currOut = outForm;
	}

	@Override
	public String getHelp()
	{
		StringBuilder builder = new StringBuilder();
		// TODO Add text
		return builder.toString();
	}

	@Override
	public String[] getCommandName()
	{
		return new String[] { "cat", "concat" };
	}

	@Override
	public EnumSet<DataFormats> getInputFormats()
	{
		return EnumSet.of(DataFormats.NULL, DataFormats.FILE,
				DataFormats.FILE_M);
	}

	@Override
	public EnumSet<DataFormats> getOutputFormats()
	{
		return EnumSet.of(DataFormats.NULL, DataFormats.USER,
				DataFormats.TEXT_M);
	}

	@Override
	public DataFormats getCurrentFormat()
	{
		return currOut;
	}
}