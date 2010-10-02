/**
 * 
 */
package edu.nps.jody.MergeAndAnalyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * This class provides tools to convert a file where truth/label pair have been merged into an
 * analysis file containing merge file name, merge file path, size of original predict file slices 
 * (combined), a confusion matrix, a list of source files by hash id (not actual filename), a by 
 * source truth count, and a by source label count. 
 * 
 * @author Jody Grady, Masters student, Naval Postgraduate School
 *
 */
public abstract class Analyzer 
{
	//Data Members
	public static final String PAIR_DELIM = ":";
	public static final String ANALYSIS_DIR_NAME = "analysis";
	
	//Constructors
	
	
	//Methods
	/**
	 * reads a merge file of truth/label pairs and outputs an analysis file comprised of merge file name,
	 * merge file path, size of original predict file slices (combined), a confusion matrix, a list of source
	 * files by hash id (not actual filename), a by source truth count, and a by source label count. 
	 * 
	 * @param mergeFile file of merged truth/label pairs to be converted into an analysis report
	 * @param size total size of the original formatted machine learning file - NOT the original content!
	 * @param analysisDirectory directory where analysis files are stored
	 * @throws FileNotFoundException if the mergeFile is not found, throw exception
	 * @throws IOException if permissions or directory structure prevent access to mergeFile or analysisFile, throw exception
	 */
	public static void analyzeLists(File mergeFile, long size, File analysisDirectory) throws FileNotFoundException, IOException
	{
		HashMap<String, Integer> confusionMap = createConfusionMatrix(mergeFile);
		
		File analysisFile = new File(analysisDirectory, mergeFile.getName());
		
		try
		{
		analysisFile.createNewFile();
		}
		catch(IOException i)
		{
			analysisDirectory.mkdirs();
			analysisFile.createNewFile();
		}
		
		PrintWriter mergePrintWriter = new PrintWriter(analysisFile);
		
		printAnalysis(mergeFile, size, confusionMap, mergePrintWriter);
	}
	
	public static void analyzeLists(File mergeFile, long size) throws FileNotFoundException, IOException
	{
		File analysisDirectory = new File(mergeFile.getParentFile().getParentFile(), ANALYSIS_DIR_NAME);
		
		analyzeLists(mergeFile, size, analysisDirectory);
	}
	
	/**
	 * reads a merge file of truth/label pairs and outputs an analysis file comprised of merge file name,
	 * merge file path, size of original predict file slices (combined), a confusion matrix, a list of source
	 * files by hash id (not actual filename), a by source truth count, and a by source label count. 
	 * This variant of printAnalysis files used a preconfigured sibling directory to store analysis files.
	 * 
	 * @param mergeFile file of merged truth/label pairs to be converted into an analysis report
	 * @param size total size of the original formatted machine learning file - NOT the original content!
	 * @throws FileNotFoundException if the mergeFile is not found, throw exception
	 * @throws IOException if permissions or directory structure prevent access to mergeFile or analysisFile, throw exception
	 */
	public static void printAnalysis(File mergeFile, long size, HashMap<String, Integer> confusionMap, PrintWriter printWriter)
	{
		SortedSet<String> truthLabelSortedSet = new TreeSet<String>(confusionMap.keySet());
		SortedSet<String> authorSortedSet = new TreeSet<String>();
		
		HashMap<String, Integer> truthHashMap = new HashMap<String, Integer>();
		HashMap<String, Integer> labelHashMap = new HashMap<String, Integer>();
		
		Iterator<String> iterator = truthLabelSortedSet.iterator();
		
		String truthLabelPair;
		Integer count;
		
		StringTokenizer stringTokenizer;
		String truth;
		String label;
		
		printWriter.println("<mergeFileName> " + mergeFile.getName());
		printWriter.println("<mergeFileNameAndPath> "	+ mergeFile.getAbsolutePath());
		printWriter.println("<originalDataSize> " 	+ size);
		printWriter.println("<mergeFileDateStamp> " 	+ mergeFile.lastModified());

		Date date = new Date(mergeFile.lastModified());
		
		printWriter.println("<mergeFileDate> " +  date.toString());
		
		
		printWriter.print("<confusionMatrix> ");
		
		//FIXME This is a confused, long method.  Need to really rethink from scratch.
		while (iterator.hasNext())
		{
			truthLabelPair = iterator.next();
			count = confusionMap.get(truthLabelPair);
			printWriter.print(truthLabelPair + PAIR_DELIM + count + " ");
			
			stringTokenizer 	= new StringTokenizer(truthLabelPair, PAIR_DELIM);
			truth						= stringTokenizer.nextToken();
			label						= stringTokenizer.nextToken();
			
			if (truthHashMap.containsKey(truth))
			{
				truthHashMap.put(truth, truthHashMap.get(truth) + count);
			}
			else
			{
				//FIXME This is a nasty side effect way to code the author piece, but it will do for now.
				authorSortedSet.add(truth);
				truthHashMap.put(truth, count);
			}
			
			if (labelHashMap.containsKey(label))
			{
				labelHashMap.put(label, labelHashMap.get(label) + count);
			}
			else
			{
				labelHashMap.put(label, count);
			}
		}
		
		//Finish off confusion matrix line
		printWriter.println();
		printWriter.flush();//Put here to stop the confusion matrix from just disapearing.
		
		printWriter.print("<authors> ");
		iterator = authorSortedSet.iterator();
		while (iterator.hasNext())
		{
			printWriter.print(iterator.next() + " ");
		}
		//Finish off author line
		printWriter.println();
		
		printWriter.print("<truthUtterances>");
		int totalCount = printMap(printWriter, truthHashMap);
				
		printWriter.print("<labelUtterances>");
		printMap(printWriter, labelHashMap);
		
		printWriter.println("<totalUtteranes> " + totalCount);
		
		printWriter.flush();
	}
	
	/**
	 * prints a map of truth/count pairs or label/count pairs into the analysis file
	 * point to by the provided PrintWriter
	 * 
	 * @param printWriter printWriter pointing at the analysis file to be written
	 * @param hashMap hash map of truth/label pairs and their respective counts
	 * @return an integer representaion of the total number of entries for all authors.
	 */
	public static int printMap(PrintWriter printWriter, HashMap<String, Integer> hashMap)
	{
		SortedSet<String> sortedSet = new TreeSet<String>(hashMap.keySet());
		
		Iterator<String> iterator = sortedSet.iterator();
		
		String key;
		Integer value;
		
		int totalCount = 0;
		
		while (iterator.hasNext())
		{
			key = iterator.next();
			value = hashMap.get(key);
			printWriter.print(key + PAIR_DELIM + value + " ");
			totalCount += value;
		}
		
		//Finish off line
		printWriter.println();
		
		return totalCount;
	}
	
	/**
	 * @param mergeFileName String containing path and name to merge file to be converted into a confusion matrix hashmap
	 * @return a hash map containing truth/label pairs as the key and counts as the values.
	 * @throws FileNotFoundException if the mergeFile is not found, throw exception
	 * @throws IOException if permissions or directory structure prevent access to mergeFile or analysisFile, throw exception
	 */
	public static HashMap<String, Integer> createConfusionMatrix(String mergeFileName) throws FileNotFoundException, IOException
	{
		File mergeFile = new File(mergeFileName);
		
		return createConfusionMatrix(mergeFile);
	}
	
	/**
	 * @param mergeFile merge file to be converted into a confusion matrix hashmap
	 * @return a hash map containing truth/label pairs as the key and counts as the values.
	 * @throws FileNotFoundException if the mergeFile is not found, throw exception
	 * @throws IOException if permissions or directory structure prevent access to mergeFile or analysisFile, throw exception
	 */
	public static HashMap<String, Integer> createConfusionMatrix(File mergeFile) throws FileNotFoundException, IOException
	{
		BufferedReader mergeBufferedReader = new BufferedReader( new FileReader(mergeFile) );
		
		String mergeString;
		String truthString;
		String labelString;
		StringTokenizer mergeTokenizer;
		HashMap<String, Integer> confusionMap = new HashMap<String, Integer>();
		
		SortedSet<String> keySet	= new TreeSet<String>();
		
		Iterator<String> iterator; 
		
		while ((mergeString = mergeBufferedReader.readLine()) != null)
		{
			mergeTokenizer	= new StringTokenizer(mergeString, PAIR_DELIM);
			truthString 			= mergeTokenizer.nextToken();
			
			//FIXME THIS CAN'T be the only way to do this -- extremely clunky
			//Load up HashMap with zeroes for all possible instances of truth:label and label:truth
			if (!keySet.contains(truthString))
			{
				keySet.add(truthString);
				
				iterator	= keySet.iterator();
				
				while (iterator.hasNext())
				{
					labelString = iterator.next();
					//FIXME Good for non matches, waste of cycle for matches -- think on this...
					confusionMap.put(truthString + PAIR_DELIM + labelString, 0);
					confusionMap.put(labelString + PAIR_DELIM + truthString, 0);
				}
			}

			labelString			= mergeTokenizer.nextToken();
			
			if (!keySet.contains(labelString))
			{
				keySet.add(labelString);
				iterator		= keySet.iterator();
				
				while (iterator.hasNext())
				{
					truthString = iterator.next();
					confusionMap.put(truthString + PAIR_DELIM + labelString, 0);
					confusionMap.put(labelString + PAIR_DELIM + truthString, 0);
				}
			}
			
			//New or not, increment the value of the found truth:label pair
			confusionMap.put(mergeString, confusionMap.get(mergeString) +1);
		}
		
		mergeBufferedReader.close();
		
		return confusionMap;
	}
}
