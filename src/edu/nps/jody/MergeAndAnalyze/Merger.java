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
import java.util.Vector;

import java.util.Iterator;

public abstract class Merger 
{
	//Data Members
	final static String PAIR_DELIM = ":";
	
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
	 * merges the leading labels of an infile with the lines of a result file.  this creates a merged file
	 * of truth/label pairs to be used in machine learning effectiveness.  The distinction between
	 * inFile and outFile is important: infile expects a label at the front of a line of data.  outFile
	 * expects only a label.  If these two files are reversed, the merged file will not be usable.
	 * Although the nature of building a List of files from File.List creates an ordered list of Files,
	 * this method does NOT assume that files in inFileList and files in outFileList are in the same
	 * order.  As such, files are iterated over in inFileList and are searched for in outFileList.  If a 
	 * matching file is not found, then an error statement is written into the merge file.
	 * This function depends on prediction files and result files having the same filename
	 * but not the same file path.
	 * 
	 * @param inFileList list of files used to generate results (ie libSVM formatted prediction slice files).
	 * @param outFileList list of files generated (result files from machine learning that match prediction slice files)
	 * @param mergeFile outputted file of merged inFiles and outFiles
	 * @throws FileNotFoundException if one of the inFiles or outFIles does not exist
	 * @throws IOException thrown if a permissions or directory issue prevents accessing a file
	 */
	public static void mergeInAndOutLists(List<File> inFileList, File outFileDirectory, File mergeFile) throws FileNotFoundException, IOException
	{
		Iterator<File> inFileListIterator = inFileList.iterator();
		File inFile;
		File outFile;
		
		try
		{
			mergeFile.createNewFile();
		}
		catch (IOException i)
		{
			mergeFile.getParentFile().mkdirs();
			mergeFile.createNewFile();
		}
		
		PrintWriter mergePrintWriter = new PrintWriter(mergeFile);
		
		while (inFileListIterator.hasNext())
		{
			inFile = inFileListIterator.next();
			outFile = new File(outFileDirectory, inFile.getName());
			
			if (outFile.isFile())//FIXME This is better handled with an exception
			{
				mergeInAndOut(inFile, outFile, mergePrintWriter);
			}
			else
			{
				mergePrintWriter.println("MATCHING FILE ERROR: " + inFile.getName() + " has no match found in outFile directory, " + outFileDirectory + "!");
			}
		}
		
		mergePrintWriter.close();
	}
	
	/**
	 * 	Merges the leading labels of an infile with the lines of a result file.  this creates a merged file
	 * of truth/label pairs to be used in machine learning effectiveness. The distinction between
	 * inFile and outFile is important: infile expects a label at the front of a line of data.  outFile
	 * expects only a label.  If these two files are reversed, the merged file will not be usable.
	 * Although the nature of building a List of files from File.List creates an ordered list of Files,
	 * this method does NOT assume that files in inFileList and files in outFileList are in the same
	 * order.  As such, files are iterated over in inFileList and are searched for in outFileList.  If a 
	 * matching file is not found, then an error statement is written into the merge file.
	 * This function depends on prediction files and result files having the same filename
	 * but not the same file path.
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
			mergePrintWriter.println(inString + PAIR_DELIM + outString);//FIXME Replace with PAIR_DELIM
		}
		mergePrintWriter.flush();
		
		inBufferedReader.close();
		outBufferedReader.close();
		mergePrintWriter.flush();
	}

	/**
	 * Merges the leading labels of an infile with the lines of a result file.  this creates a merged file
	 * of truth/label pairs to be used in machine learning effectiveness. The distinction between
	 * inFile and outFile is important: infile expects a label at the front of a line of data.  outFile
	 * expects only a label.  If these two files are reversed, the merged file will not be usable.
	 * Although the nature of building a List of files from File.List creates an ordered list of Files,
	 * this method does NOT assume that files in inFileList and files in outFileList are in the same
	 * order.  As such, files are iterated over in inFileList and are searched for in outFileList.  If a 
	 * matching file is not found, then an error statement is written into the merge file.
	 * This function depends on prediction files and result files having the same filename
	 * but not the same file path.
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
		
		try
		{
			mergeFile.createNewFile();
		}
		catch (IOException i)
		{
			mergeFile.getParentFile().mkdirs();
			mergeFile.createNewFile();
		}
		
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

	public static long sumFileSizes(String baseFileName, int startExt, int endExt, File directory)
	{
		long size = 0;
		File file;
		
		for (int i = startExt; i <= endExt; i++)
		{
			file = new File(directory, baseFileName + "." + i);
			try
			{
				size += file.length();
			}
			catch (Exception e)
			{
				//Do nothing
			}
		}
		
		return size;
	}
	
	public static void mergeInAndOut(String baseFileName, int startExt, int endExt, File inFileDirectory, File outFileDirectory, File mergeFile) throws FileNotFoundException, IOException
	{
		List<File> fileList = new Vector<File>();
		
		for (int i = startExt; i <= endExt; i++)
		{
			fileList.add(new File(inFileDirectory, baseFileName + "." + i));
		}
		
		mergeInAndOutLists(fileList, outFileDirectory, mergeFile);
	}
}
