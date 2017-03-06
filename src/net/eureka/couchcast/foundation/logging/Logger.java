package net.eureka.couchcast.foundation.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.Bootstrap;
import net.eureka.utils.IO;

/**
 * Server logger. Only needs to be instantiated once. Attempts to find the log file via application directory, if not found creates a new one.
 * After the log is set up, the only method needed to be called to write to it is append(StringBuffer... text) below.
 * 
 * @author Owen McMonagle.
 *
 * @see ApplicationGlobals
 * @see Bootstrap
 * 
 * @version 0.1
 */
public final class Logger 
{
	/**
	 * Name of the log file.
	 */
	private static final StringBuffer LOGGING_FILE = new StringBuffer("log.txt");
			
	/**
	 * Starting content of the log file.
	 */
	private static final StringBuffer STARTING_CONTENT = new StringBuffer("[COUCH CAST MEDIA SERVER LOG FILE]");
	
	/**
	 * Log file object.
	 */
	private static File file = null;
	
	/**
	 * Creates the log file path from the file name and application directory. Then attempts to instantiate a new file object. It then verifies
	 * that the File object does exist, if not a new log is created.
	 */
	public Logger()
	{
		// Create log file path.
		StringBuffer log_path = new StringBuffer(ApplicationGlobals.getApplicationDirectory().toString()+LOGGING_FILE.toString());
		// Instantiate file object with path. 
		file = new File(log_path.toString());
		// Verify file object does exist.
		final boolean does_log_exist = file.isFile();
		// DEBUG.
		System.out.println("Found log file:\t"+does_log_exist);
		// If file does not exist...
		if(!does_log_exist)
			// Create new file.
			IO.createContentFile(log_path, STARTING_CONTENT);
	}
	
	/**
	 * Appends new text log. Can accept any amount of StringBuffer parameters. Printing each object as a new line in the log.
	 * @param StringBuffer... text - Each StringBuffer is a line to print in log.
	 */
	public static void append(StringBuffer... text)
	{
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
