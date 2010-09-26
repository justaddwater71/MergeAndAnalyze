/**
 * 
 */
package edu.nps.jody.MergeAndAnalyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author jody
 *
 */
public class Analyzer 
{
	//Data Members
	public static final String PAIR_DELIM = ":";
	
	//Constructors
	
	
	//Methods
	public static HashMap<String, Integer> createConfusionMatrix(String mergeFileName) throws FileNotFoundException, IOException
	{
		File mergeFile = new File(mergeFileName);
		
		return createConfusionMatrix(mergeFile);
	}
	
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
