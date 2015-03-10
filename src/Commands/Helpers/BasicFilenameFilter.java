package Commands.Helpers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BasicFilenameFilter implements FilenameFilter
{
	Pattern regexPattern = null;
	boolean recursive = false;
	
	public BasicFilenameFilter(String filter, boolean caseSensitive, boolean recursive)
	{
		regexPattern = Pattern.compile(filter, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		this.recursive = recursive;
	}
	
	@Override
	public boolean accept(File arg0, String arg1)
	{
		try
		{
			if (recursive && new File(arg0.getCanonicalPath() + "\\" + arg1).isDirectory())
				return true;
		} catch (IOException e)
		{
			System.out.println("Error: " + e.getLocalizedMessage());
		}
		Matcher matcher = regexPattern.matcher(arg1);
		return matcher.matches();
	}
}
