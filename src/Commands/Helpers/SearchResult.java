package Commands.Helpers;

import java.io.File;
import java.util.ArrayList;

import RITA.Host;


public class SearchResult
{
	ArrayList<SearchResult> subSearches = null;
	File file = null;
	int results = 0;
	int level = 0;
	
	public ArrayList<SearchResult> getSubSearches()
	{
		return subSearches;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public int getResults()
	{
		return results;
	}

	public int getLevel()
	{
		return level;
	}
	
	public SearchResult(File file, BasicFilenameFilter filter, int level, Host host)
	{
		subSearches = new ArrayList<SearchResult>();
		this.file = file;
		this.level = level;
		
		if (level != 0 && this.file.isDirectory())
		{
			File[] fresults = (filter != null ? file.listFiles(filter) : file.listFiles());
			if (fresults != null)
				subSearches = new ArrayList<SearchResult>(fresults.length);
			try
			{
				for (File sub: fresults)
				{
					SearchResult subSearch = new SearchResult(sub, filter, level - 1, host);
					subSearches.add(subSearch);
					results += subSearch.getResults();
				}
			} catch (Exception e)
			{
				if (host.isDebug())
				{
					System.err.println("An error occurred: " + e.getLocalizedMessage()+ "... Continuing.");
				}
			}
		}
		else
			results = 1;
	}
}
