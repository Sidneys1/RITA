package RITA;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.EnumSet;
import java.util.List;

/**
 * Describes a command structure
 * @author sborne1
 *
 */
public interface ICommand extends Runnable 
{
	/**
	 * Prepare the ICommand by passing the Host controller and parameters
	 * @param host The host object calling this ICommand
	 * @param params The parameters passed to this ICommand
	 */
	public void Prepare(Host host, List<Parameter> params);
	
	/**
	 * Retrieves the input stream for this ICommand
	 * @return The input stream
	 */
	public PipedReader getInput();
	
	/**
	 * Initializes the input stream to read from the specified output stream
	 * @param in The output stream to read from
	 * @param inTypes The formats the output can be in
	 * @throws IOException Possible errors
	 */
	public DataFormats startInput(PipedWriter in, EnumSet<DataFormats> inTypes) throws IOException;
	
	/**
	 * Gets the output for this ICommand
	 * @return the output stream
	 */
	public PipedWriter getOutput();
	
	/**
	 * Initialize the output
	 * @param outForm the requested format
	 */
	public void startOutput(DataFormats outForm);
	
	/**
	 * Retrieves this command's help information
	 */
	public String getHelp();
	
	/**
	 * Used to recognize this command. Must be unique
	 * @return the call name(s) of this command
	 */
	public String[] getCommandName();
	
	/**
	 * Used to understand acceptable piping inputs
	 * @return an EnumSet of the output
	 */
	public EnumSet<DataFormats> getInputFormats();
	
	/**
	 * Used to understand available output formats
	 * @return
	 */
	public EnumSet<DataFormats> getOutputFormats();
	
	public DataFormats getCurrentFormat();
}
