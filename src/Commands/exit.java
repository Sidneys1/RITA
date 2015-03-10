package Commands;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

import RITA.*;

public class exit implements ICommand
{
	List<Parameter> params = null;
	Host host = null;
	
	PipedWriter output = new PipedWriter();
	BufferedWriter bufOut = new BufferedWriter(output);
	boolean doOutput = true;
	
	@Override
	public void run()
	{
		if (doOutput)
			TryPrint("Terminating session...");
		host.setRunning(false);
		
		try
		{
			output.close();
			bufOut.close();
		} catch (IOException e)
		{
			
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
			if(host.isDebug())
				System.err.println("Error writing Command output!");
		}
	}

	@Override
	public String[] getCommandName()
	{
		return new String[] {"exit"};
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
		return null;
	}

	@Override
	public DataFormats startInput(PipedWriter in, EnumSet<DataFormats> inTypes)
			throws IOException
	{
		return null;
	}

	@Override
	public PipedWriter getOutput()
	{
		return output;
	}

	@Override
	public void startOutput(DataFormats outForm)
	{
		doOutput = outForm == DataFormats.USER;
	}

	@Override
	public String getHelp()
	{
		StringBuilder build = new StringBuilder();
		build.append("Usage:\r\n");
		build.append("\texit\r\n\r\n");
		build.append("\tFlags the RITA host to terminate.\r\n");
		return build.toString();
	}

	@Override
	public EnumSet<DataFormats> getInputFormats()
	{
		return EnumSet.of(DataFormats.NULL);
	}

	@Override
	public EnumSet<DataFormats> getOutputFormats()
	{
		return EnumSet.of(DataFormats.NULL, DataFormats.USER);
	}

	@Override
	public DataFormats getCurrentFormat()
	{
		return doOutput ? DataFormats.USER : DataFormats.NULL;
	}
}