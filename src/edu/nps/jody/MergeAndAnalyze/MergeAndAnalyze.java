/**
 * 
 */
package edu.nps.jody.MergeAndAnalyze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	public static final String 	SUFFIX_DELIM 			= ".";
	public static final String MERGE_DIR_NAME 	= "mergeFiles";
	public static final String RESULTS_DIR_NAME = "results";
	public static final String PREDICT_DIR_NAME 	= "predict";
	
	//Constructors
	
	
	//Methods
	public static String getFileSuffix(File file)
	{
		return file.getName().split(SUFFIX_DELIM)[0];
	}
	
	
	public static List<List<File>> buildCrossValidationLists(File directory)
	{
		//Quick sanity check that predictDirectory is valid, exists, and is a directory
		if ( ! directory.isDirectory())
		{
			return null;
		}
		
		List<List<File>> crossValidationLists = new Vector<List<File>>();
		File[] fileArray = directory.listFiles();
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
			currentFileSuffix = getFileSuffix(currentFile);
			
			if (listSuffix.equals(currentFileSuffix))
			{
				//Do nothing
			}
			else
			{
				crossValidationLists.add(currentList = new Vector<File>());
				listSuffix = getFileSuffix(currentFile);
			}
			
			currentList.add(currentFile);
		}
		
		return crossValidationLists;
	}
	
	public static List<List<File>> buildCrossValidationLists(String directoryName)
	{
		File directory = new File(directoryName);
		return buildCrossValidationLists(directory);
	}
	
	public static void makeMergeAndAnalysisFiles (File mergeDirectory, File resultsDirectory, File predictDirectory) throws FileNotFoundException, IOException
	{
		mergeDirectory.mkdirs();
		
		List<List<File>> predictLists = buildCrossValidationLists(predictDirectory);
		
		List<File> 	currentList;
		File				currentFile;
		String			currentFileSuffix;
		File				mergeFile;
		
		Iterator<List<File>> listsIterator = predictLists.iterator();
		Iterator<File> fileIterator;
		long fileSize;
		
		while (listsIterator.hasNext())
		{
			//Prep to conduct merge of all predict truths and results labels
			currentList 			= listsIterator.next();
			currentFile 			= currentList.get(0);
			currentFileSuffix	= getFileSuffix(currentFile);
			mergeFile				= new File(mergeDirectory, currentFileSuffix);
			
			fileIterator = currentList.iterator();
			
			//clear fileSize to accrue file size for analysis tools
			fileSize = 0;
			
			while (fileIterator.hasNext())
			{
				fileSize += fileIterator.next().length();
			}
			
			Merger.mergeInAndOutLists(currentList, resultsDirectory, mergeFile);
			Analyzer.analyzeLists(mergeFile, fileSize);
		}
	}
	
	public static void makeMergeAndAnalysisFiles (File parentDirectory) throws FileNotFoundException, IOException
	{
		File mergeDirectory		= new File(parentDirectory, MERGE_DIR_NAME);
		File resultsDirectory		= new File(parentDirectory, RESULTS_DIR_NAME);
		File predictDirectory	= new File(parentDirectory, PREDICT_DIR_NAME);
		
		makeMergeAndAnalysisFiles (mergeDirectory, resultsDirectory, predictDirectory);
	}
	
}
