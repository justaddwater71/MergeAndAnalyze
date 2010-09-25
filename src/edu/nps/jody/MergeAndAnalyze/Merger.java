package edu.nps.jody.MergeAndAnalyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import java.util.Iterator;

public class Merger 
{
	//Data Members
	
	
	//Constructors
	
	
	//Methods
	public static HashMap<String, File> loadFileNameMap(List<File> fileList)
	{
		HashMap<String, File> fileNameMap = new HashMap<String, File>();
		
		Iterator<File> iterator = fileList.iterator();
		
		File file;
		
		while (iterator.hasNext())
		{
			file = iterator.next();
			fileNameMap.put(file.getName(), file);
		}
		
		return fileNameMap;
	}
	
	/**
	 * ...
	 * Although the nature of building a List of files from File.List creates an ordered list of Files,
	 * this method does NOT assume that files in inFileList and files in outFileList are in the same
	 * order.  As such, files are iterated over in inFileList and are searched for in outFileList.  If a 
	 * matching file is not found, then an error statement is written into the merge file.
	 * This function depends on prediction files and result files having the same filename
	 * but not the same file path.
	 * @param inFileList list of files used to generate results (ie libSVM formatted prediction slice files).
	 * @param outFileList list of files generated (result files from machine learning that match prediction slice files)
	 * @param mergeFile outputted file of merged inFiles and outFiles
	 * @throws FileNotFoundException if one of the inFiles or outFIles does not exist
	 * @throws IOException thrown if a permissions or directory issue prevents accessing a file
	 */
	public static void mergeInAndOutLists(List<File> inFileList, List<File> outFileList, File mergeFile) throws FileNotFoundException, IOException
	{
		Iterator<File> inFileListIterator = inFileList.iterator();
		HashMap<String, File> outFileNameMap = loadFileNameMap(outFileList);
		File inFile;
		File outFile;
		PrintWriter mergePrintWriter = new PrintWriter(mergeFile);
		
		while (inFileListIterator.hasNext())
		{
			inFile = inFileListIterator.next();
			
			if (outFileNameMap.containsKey(inFile.getName()))
			{
				outFile = outFileNameMap.get(inFile.getName());
				
				mergeInAndOut(inFile, outFile, mergePrintWriter);
			}
			else
			{
				mergePrintWriter.println("MATCHING FILE ERROR: " + inFile.getName() + " has no match found in outFile directory!");
			}
		}
		
		mergePrintWriter.close();
	}
	
	/**
	 * ...
	 * Since this variant of mergeInAndOut is provided with a PrintWriter from an outside calling
	 * method, this variant does not close the PrintWriter.
	 * @param inFile
	 * @param outFile
	 * @param mergePrintWriter
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void mergeInAndOut(File inFile, File outFile, PrintWriter mergePrintWriter) throws FileNotFoundException, IOException
	{
		Reader inReader = new FileReader(inFile);
		Reader outReader = new FileReader(outFile);
		
		BufferedReader inBufferedReader = new BufferedReader(inReader);
		BufferedReader outBufferedReader = new BufferedReader(outReader);
		
		StringTokenizer inTokenizer;
		
		String inString;
		String outString;
		
		while ((inString = inBufferedReader.readLine()) != null)
		{
			inTokenizer = new StringTokenizer(inString);
			inString = inTokenizer.nextToken();
			outString = outBufferedReader.readLine();
			mergePrintWriter.println(inString + ":" + outString);
			mergePrintWriter.flush();
		}
		
		inBufferedReader.close();
		outBufferedReader.close();
		mergePrintWriter.flush();
	}
	
	/**
	 * .......
	 * This variant of mergeInAndOut creates and closes its own PrintWriter from the provided mergeFile.
	 * @param inFile
	 * @param outFile
	 * @param mergeFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void mergeInAndOut(File inFile, File outFile, File mergeFile) throws FileNotFoundException, IOException
	{
		Reader inReader = new FileReader(inFile);
		Reader outReader = new FileReader(outFile);
		
		BufferedReader inBufferedReader = new BufferedReader(inReader);
		BufferedReader outBufferedReader = new BufferedReader(outReader);
		PrintWriter mergePrintWriter = new PrintWriter(mergeFile);
		
		StringTokenizer inTokenizer;
		
		String inString;
		String outString;
		
		while ((inString = inBufferedReader.readLine()) != null)
		{
			inTokenizer = new StringTokenizer(inString);
			inString = inTokenizer.nextToken();
			outString = outBufferedReader.readLine();
			mergePrintWriter.println(inString + ":" + outString);
			mergePrintWriter.flush();
		}
		
		inBufferedReader.close();
		outBufferedReader.close();
		mergePrintWriter.flush();
		mergePrintWriter.close();
	}
}
