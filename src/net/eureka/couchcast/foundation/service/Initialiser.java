package net.eureka.couchcast.foundation.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.InitialiseFoundation;
import net.eureka.couchcast.foundation.logging.Logger;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;

/**
 * Called by InitialiseFoundation at server start up. Responsible for creating the start up script which allows the server to run on user login.
 * Similar to the Logger by design. It only checks on server start up if the script exists so the server has to be run once before it can start
 * during user login. 
 * 
 * The script is located at "SYSTEMDRIVE\APPDATA\Microsoft\Windows\Start Menu\Programs\Startup\ANDROID_CAST_START_UP.bat".
 * 
 * @author Owen McMonagle.
 *
 * @see Logger 
 * @see InitialiseFoundation
 */
public final class Initialiser 
{
	
	/**
	 * Starts the verification process.
	 */
	public Initialiser() 
	{
		createStartUpFile();
	}
	
	/**
	 * Searches for the start up script at "SYSTEMDRIVE\APPDATA\Microsoft\Windows\Start Menu\Programs\Startup\PC_CAST_START_UP.bat". If found,
	 * a new file is not created and the log is notified of the scripts existence. If not, a new file is created then populated with the location
	 * of the server jar to be used on start up.
	 */
	private static void createStartUpFile()
	{
		// Create the start up script path.
		String start_up_path = System.getenv("APPDATA")+File.separator+"Microsoft"+File.separator+"Windows"+File.separator+"Start Menu"+File.separator+"Programs"+File.separator+"Startup"+File.separator+"ANDROID_CAST_START_UP.bat",
			   // Set up script command and join with jar path.
			   start_up_batch_command = "start \"\" \""+ApplicationGlobals.getApplicationDirectory()+"Android Cast Media Server.exe\"";
		
		// Create file object with start up script path.
		File start_up_launcher = new File(start_up_path);
		// Verify start up scripts existence... if doesn't exist...
		if(!start_up_launcher.exists())
		{
			try 
			{
				// Create new script file.
				start_up_launcher.createNewFile();
				// Set up buffered stream to writer, with UTF-8 encoding.
				BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(start_up_launcher, true), ApplicationGlobals.getEncoding()));
				// Write batch command to file.
				
				buffered_writer.append("@echo off");
				buffered_writer.newLine();
				buffered_writer.append(start_up_batch_command);
				buffered_writer.newLine();
				buffered_writer.append("exit");
				// Flush bytes down stream.
				buffered_writer.flush();
				// Close file stream.
				buffered_writer.close();
				// Append to logger that start up script has been created.
				Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_SERVICE_LAUNCHER_CREATED)));
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		// If does exist...
		else
			// Append to file script already exists.
			Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_SERVICE_LAUNCHER_EXISTS)));
	}
}
