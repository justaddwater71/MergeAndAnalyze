/**
 * 
 */
package edu.nps.jody.MergeAndAnalyze;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author jody
 *
 */
public class MergeAndAnalyze 
{
	//Data Members
	public static final String 	SUFFIX_DELIM = ".";
	
	//Constructors
	
	
	//Methods
	public static List<List<File>> buildCrossValidationLists(File predictDirectory)
	{
		List<List<File>> crossValidationLists = new Vector<List<File>>();
		File[] fileArray = predictDirectory.listFiles();
		SortedSet<File> sortedFileSet = new TreeSet<File>();
		Iterator<File> iterator;
		
		//load sorted file set
		for (int i = 0; i < fileArray.length; i++)
		{
			sortedFileSet.add(fileArray[i]);
		}
		
		File currentFile;
		String listSuffix = "";
		String currentFileSuffix;
		iterator = sortedFileSet.iterator();
		List<File> currentList = new Vector<File>();
		
		while (iterator.hasNext())
		{
			currentFile = iterator.next();
			//Get filename before the first extension (before the first period)
			currentFileSuffix = currentFile.getName().split(SUFFIX_DELIM)[0];
			
			if (listSuffix.equals(currentFileSuffix))
			{
				//Do nothing
			}
			else
			{
				crossValidationLists.add(currentList = new Vector<File>());
				listSuffix = currentFile.getName().split(SUFFIX_DELIM)[0];
			}
			
			currentList.add(currentFile);
		}
		
		return crossValidationLists;
	}
}
