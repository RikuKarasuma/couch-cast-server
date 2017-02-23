package net.eureka.androidcast.foundation.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.eureka.androidcast.Static;
import net.eureka.androidcast.foundation.init.ApplicationGlobals;
import net.eureka.androidcast.foundation.init.InitialiseFoundation;
import net.eureka.androidcast.foundation.init.NetworkGlobals;
import net.eureka.androidcast.foundation.logging.Logger;
import net.eureka.androidcast.gui.tray.Tray;

/**
 * Handles the creation, writing and reading of the configuration file located within the application directory. The configuration file holds
 * values such as the server name, download directory and process ID of the current or last session.
 * 
 * @author Owen McMonagle.
 *
 * @see InitialiseFoundation 
 * @see ApplicationGlobals
 * @see NetworkGlobals
 * @see Logger
 */
public final class Configuration
{
	/**
	 * Name of the configuration file.
	 */
	private static final String CONFIG_FILE = "config.txt";
	
	/**
	 * First line of the configuration file.
	 */
	private static final String STARTING_CONTENT = "[COUCH CAST MEDIA SERVER CONFIGURATION FILE]";
	
	/**
	 * Flag for indicating the first read of the server.
	 */
	private static boolean firstRead = true;
	
	/**
	 * Configuration file constructor has two main functions. Setting to_update to true will signal
	 * to update the configuration file with the new server data available. The false setting signals that
	 * the constructor should read from the configuration file and set the loaded variables accordingly.
	 * Finally there is a method called checkFirstRead(), this is for writing the process ID on server start up
	 * to the configuration file.
	 * 
	 * @param Boolean to_update - True to update configuration file, false to read from it.
	 */
	public Configuration(final boolean to_update) 
	{
		// Retrieve configuration file.
		File configuration_file = getConfigurationFile();
		// If not update...
		if(!to_update)
			// read from configuration file.
			checkConfigurationFile();
		// If update...
		else
			// create, write to the configuration file.
			writeConfigurationFile(configuration_file);
		
		// Check first time read.
		checkFirstRead();
	}
	
	/**
	 * Verifies it is the first time the configuration file is read and if so updates the file. This is so the
	 * process id from the last session is overwritten.
	 */
	private static void checkFirstRead()
	{
		// If first time reading...
		if(firstRead)
		{
			// Update configuration file.
			writeConfigurationFile(getConfigurationFile());
			// Set first time reading false.
			firstRead = false;
		}
	}
	
	/**
	 * Attempts to retrieve and verify the existence of the configuration file. If the configuration file exists, then
	 * the file is read from and data updated. If not the file is created and data is written to it. 
	 */
	private static void checkConfigurationFile()
	{
		// Retrieve configuration file.
		File configuration_file = getConfigurationFile();
		// If configuration file exists...
		if(configuration_file.exists())
			// read from it.
			readFromConfigurationFile();
		// If configuration file does not exist...
		else
		{
			System.out.println("Does not exist");
			// Create new configuration file.
			createConfigurationFile(configuration_file);
			// Write data to configuration file.
			writeConfigurationFile(configuration_file);
		}
	}
	
	/**
	 * Reads from the configuration file then updates the data retrieved from it.
	 */
	private static void readFromConfigurationFile()
	{
		// Retrieve configuration file.
		File configuration_file = getConfigurationFile();
		try
		{
			// Open a buffered stream to file, with encoding of UTF-8.
			BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(new FileInputStream(configuration_file), ApplicationGlobals.getEncoding()));
			// Ignore first line.
			buffered_reader.readLine();
			// Read server name.
			String server_name = buffered_reader.readLine(),
					// Read download directory.
					download_directories = buffered_reader.readLine();
			// Ignore process id line.
			buffered_reader.readLine();
			boolean is_minimized = Boolean.parseBoolean(buffered_reader.readLine()),
					is_deep_search = Boolean.parseBoolean(buffered_reader.readLine()),
					is_music_mode = Boolean.parseBoolean(buffered_reader.readLine());
			
			String search_delay_str = buffered_reader.readLine(), update_delay_str = buffered_reader.readLine(),
					dhcp_network_str = buffered_reader.readLine();
			
			int search_delay = 500, update_delay = 2000;
			if(search_delay_str != null && !search_delay_str.isEmpty())
				search_delay = Integer.parseInt(search_delay_str);
			if(update_delay_str != null && !update_delay_str.isEmpty())
			{
				update_delay = Integer.parseInt(update_delay_str);
				if(update_delay < 1000)
					update_delay = 1000;
			}
			
			// Set new server name.
			NetworkGlobals.setServerName(server_name);
			// Set new download directory.
			ApplicationGlobals.setMonitoredList(download_directories);
			// Set minimised.
			ApplicationGlobals.setMinimizeWindows(is_minimized);
			// Set deep search.
			ApplicationGlobals.setDeepSearch(is_deep_search);
			// Set music mode.
			ApplicationGlobals.setMusicMode(is_music_mode);
			// Set search delay.
			ApplicationGlobals.setSearchDelay(search_delay);
			// Set update delay.
			ApplicationGlobals.setUpdateDelay(update_delay);
			// Set DHCP network interface.
			NetworkGlobals.setDhcpNetwork(Static.getInetAddressFromName(dhcp_network_str));
			// Set DHCP interface face.
			NetworkGlobals.setDhcpNetworkName(dhcp_network_str);
			// Update ToolTip Title on the Tray.
			Tray.updateToolTipTitle();
			// Close file stream.
			buffered_reader.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			// Append to log if error.
			Logger.append(new StringBuffer(e.getMessage()));
		}
	}
	
	/**
	 * Creates the configuration file in the application directory.
	 * @param File file - Configuration File to create.
	 */
	private static void createConfigurationFile(File file)
	{
		try 
		{
			// Create new file.
			file.createNewFile();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			// Append to log if error.
			Logger.append(new StringBuffer(e.getMessage()));
		}
	}
	
	/**
	 * Writes the server name, download directory and process id to the configuration file as to be loaded in later sessions.
	 * @param File file - Configuration file to write to.
	 */
	private static void writeConfigurationFile(File file)
	{
		// Retrieve data to be saved in file.
		String[] configurations = new String[] 
		{
				// Line to ignore, config declaration.
				STARTING_CONTENT,
				// Server name.
				NetworkGlobals.getServerName(),
				// Download directory.
				ApplicationGlobals.getCombinedDirectoryList(),
				// Process ID.
				ApplicationGlobals.getProcessID(),
				// Minimised.
				String.valueOf(ApplicationGlobals.isMinimizeWindows()),
				// Deep search.
				String.valueOf(ApplicationGlobals.isDeepSearch()),
				// Music Mode.
				String.valueOf(ApplicationGlobals.isMusicMode()),
				// Search Delay.
				String.valueOf(ApplicationGlobals.getSearchDelay()),
				// Update Delay.
				String.valueOf(ApplicationGlobals.getUpdateDelay()),
				// Interface name.
				NetworkGlobals.getDhcpNetworkName()
		};
		try
		{
			// Delete the old file.
			file.delete();
			// Create a new file.
			file.createNewFile();
			// Open up a buffered stream to the configuration file, with UTF-8 encoding.
			BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), ApplicationGlobals.getEncoding()));
			// Iterate through each String to save...
			for(String line_of_text : configurations)
			{
				// Write to configuration file.
				buffered_writer.write(line_of_text.toString());
				// New line in file.
				buffered_writer.newLine();
			}
			// Flush bytes down stream.
			buffered_writer.flush();
			// Close file stream.
			buffered_writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			// Append to log if error.
			Logger.append(new StringBuffer(e.getMessage()));
		}
	}
	
	/**
	 * Retrieves the configuration file from the application directory.
	 * @return File - Configuration file of the server.
	 */
	private static File getConfigurationFile()
	{
		return new File(ApplicationGlobals.getApplicationDirectory()+CONFIG_FILE.toString());
	}
}
