package net.eureka.couchcast.foundation.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.Bootstrap;
import net.eureka.utils.IO;

/**
 * Server logger. Only needs to be instantiated once. Attempts to find the log file via application directory, if not 
 * found creates a new one. If file exceeds 10 megabytes big, a new file is created. After the log is set up, the only
 * method needed to be called to write to it is append(StringBuffer... text) below.
 * 
 * @author Owen McMonagle.
 *
 * @see ApplicationGlobals
 * @see Bootstrap
 * 
 * @version 0.2
 */
public final class Logger 
{
	/**
	 * Name of the log file.
	 */
	private static final String LOGGING_FILE_NAME = "log";
	
			
	/**
	 * Starting content of the log file.
	 */
	private static final StringBuffer STARTING_CONTENT = new StringBuffer("[COUCH CAST MEDIA SERVER LOG FILE]");
	
	/**
	 * Log file object.
	 */
	private static File file = null;
	
	/**
	 * Creates the log file path from the file name and application directory. Then attempts to instantiate a new file
	 * object. It then verifies that the File object does exist, if not a new log is created.
	 */
	public Logger()
	{
		// Create valid log file
		createValidLog();
		// Verify file object does exist.
		final boolean does_log_exist = (file != null);
		// DEBUG.
		System.out.println("Found log file:\t"+does_log_exist);
		// If file does not exist...
	}
	
	/**
	 * Checks if the current log file is valid. A log is valid if it exists and isn't over 10 Megabyte big. If a 
	 * file is not valid, a new log is created with a increased postfix of 1. i.e log_1.txt.
	 */
	private static void createValidLog()
	{
		// Counter for log counting.
		int counter = 0;
		// Check validity flag.
		boolean valid = false;
		// File extension.
		final String extension = ".txt";
		// Loop until file is valid...
		while(!valid)
		{
			// Create log path. Increase file postfix if counter is greater than zero.
			String path = new String(ApplicationGlobals.getApplicationDirectory() + LOGGING_FILE_NAME + ( (counter > 0) ? ( "_" + counter ) : "" )  + extension);
			// Assign new file to path.
			file = new File(path);
			// Check if file exists.
			valid = file.exists();
			// Get length of file.
			final long length_of_file = file.length();
			// if file exists and length is over 10MB....
			if(valid && length_of_file > 1000000)
			{
				// File is not valid.
				valid = false;
				// Increase file postfix.
				counter++;
			}
			// If file doesn't exist..
			else if(!valid)
			{
				// Create new file...
				IO.createContentFile(new StringBuffer(path), STARTING_CONTENT);
				// Found valid file.
				valid = true;
			}
		}
	}
	
	/**
	 * Appends new text log. Can accept any amount of StringBuffer parameters. Printing each object as a new line in 
	 * the log. 
	 * @param StringBuffer... text - Each StringBuffer is a line to print in log.
	 */
	public static void append(StringBuffer... text)
	{
		if(file == null)
			createValidLog();
		try 
		{
			// Open up a buffered stream to the log file, set encoding to UTF-8.
			BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), ApplicationGlobals.getEncoding()));
			// Iterate through each line to print...
			for(StringBuffer line_of_text : text)
			{
				// Signal new line. \n
				buffered_writer.newLine();
				// Write new log text.
				buffered_writer.write(line_of_text.toString());
			}
			// Flush bytes down stream.
			buffered_writer.flush();
			// Close buffered stream.
			buffered_writer.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
