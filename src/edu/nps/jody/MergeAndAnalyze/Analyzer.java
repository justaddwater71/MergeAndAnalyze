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
	public static final String ANALYSIS_DIR_NAME 			= "analysis";
	private static final int FILE_PATH_PARAMS_COUNT 	= 7;
	private static final int PATH_TAIL									= 2;
	private static final int CROSSVAL_INDEX 					= 0;
	private static final int GROUP_SIZE_INDEX 					= 1;
	private static final int GROUP_TYPE_INDEX 				= 2;
	private static final int METHOD_INDEX 						= 3;
	private static final int MODEL_INDEX 							= 4;
	private static final int FEATURE_TYPE_INDEX 			= 5;
	private static final int CORPUS_INDEX 						= 6;
	
	private static final String FILENAME_TAG						= "<mergeFileName>";
	private static final String FILENAME_AND_PATH_TAG	= "<mergeFileNameAndPath>";
	private static final String DATA_SIZE_TAG					= "<originalDataSize>";
	private static final String FILE_DATE_STAMP_TAG		= "<mergeFileDateStamp>";
	private static final String FILE_DATE_TAG					= "<mergeFileDate>";
	private static final String CORPUS_TAG						= "<corpus>";
	private static final String FEATURE_TYPE_TAG			= "<featureType>";
	private static final String MODEL_TAG							= "<model>";
	private static final String METHOD_TAG						= "<method>";
	private static final String GROUP_TYPE_TAG				= "<groupType>";
	private static final String GROUP_SIZE_TAG					= "<groupSize>";
	private static final String CROSSVAL_TAG					= "<crossval>";
	private static final String CONFUSION_MATRIX_TAG	= "<confusionMatrix>";
	private static final String AUTHOR_TAG						= "<authors>";
	private static final String TRUTH_TAG							= "<truthUtterances>";
	private static final String LABEL_TAG							= "<labelUtterances>";
	private static final String TOTAL_TAG							= "<totalUtterances>";
	
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
		
		printWriter.println(FILENAME_TAG							+ " " + mergeFile.getName());
		printWriter.println(FILENAME_AND_PATH_TAG 	+ " " + mergeFile.getAbsolutePath());
		printWriter.println(DATA_SIZE_TAG 						+ " " + size);
		printWriter.println(FILE_DATE_STAMP_TAG 			+ " " + mergeFile.lastModified());

		Date date = new Date(mergeFile.lastModified());
		
		printWriter.println(FILE_DATE_TAG 						+ " " +  date.toString());
		
		String[] resultCharacteristics = analyzeFilePath(mergeFile.getAbsolutePath());
		
		//This is a lot of typing for clarity sake.  A loop could pull this off with enumators, but
		//that might be hard to read later.
		
		printWriter.println(CORPUS_TAG 							+ " " + resultCharacteristics[CORPUS_INDEX]);
		printWriter.println(FEATURE_TYPE_TAG 				+ " " + resultCharacteristics[FEATURE_TYPE_INDEX]);
		printWriter.println(MODEL_TAG 								+ " " + resultCharacteristics[MODEL_INDEX]);
		printWriter.println(METHOD_TAG 							+ " " + resultCharacteristics[METHOD_INDEX]);
		printWriter.println(GROUP_TYPE_TAG 					+ " " + resultCharacteristics[GROUP_TYPE_INDEX]);
		printWriter.println(GROUP_SIZE_TAG 					+ " " + resultCharacteristics[GROUP_SIZE_INDEX]);
		printWriter.println(CROSSVAL_TAG 						+ " " + resultCharacteristics[CROSSVAL_INDEX]);
		
		printWriter.print(CONFUSION_MATRIX_TAG 			+ " ");
		
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
		
		printWriter.print(AUTHOR_TAG	+ " ");
		iterator = authorSortedSet.iterator();
		while (iterator.hasNext())
		{
			printWriter.print(iterator.next() + " ");
		}
		//Finish off author line
		printWriter.println();
		
		printWriter.print(TRUTH_TAG 		+ " ");
		int totalCount = printMap(printWriter, truthHashMap);
				
		printWriter.print(LABEL_TAG 		+ " ");
		printMap(printWriter, labelHashMap);
		
		printWriter.println(TOTAL_TAG	+ " " + totalCount);
		
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
	
	public static String[] analyzeFilePath(String filePath)
	{
		String[] result		= new String[FILE_PATH_PARAMS_COUNT];
		String[] splitPath = filePath.split("/");
		
		int length = splitPath.length;
		int resultIndex = 0;
		
		for (int i = length - PATH_TAIL; i > length - PATH_TAIL - FILE_PATH_PARAMS_COUNT; i--)
		{
			result[resultIndex] = splitPath[i];
		}
		
		return result;
	}
}
